package mindustrytool.features.chat.translation;

import arc.Core;
import arc.Events;
import arc.func.Cons;
import arc.input.KeyCode;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.ui.Dialog;
import arc.scene.ui.TextField;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Reflect;
import arc.util.Strings;
import mindustry.Vars;
import mindustry.core.NetClient;
import mindustry.game.EventType.ClientLoadEvent;
import mindustry.gen.Call;
import mindustry.gen.SendMessageCallPacket;
import mindustry.gen.SendMessageCallPacket2;
import mindustrytool.Main;
import mindustrytool.features.Feature;
import mindustrytool.features.FeatureMetadata;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ChatTranslationFeature implements Feature {
    private final Seq<TranslationProvider> providers = new Seq<>();
    private final TranslationProvider defaultTranslationProvider = new DevXTranslationProvider();
    private String lastError = null;
    private TranslationProvider currentProvider = defaultTranslationProvider;
    private boolean hookSetup = false;

    @Override
    public FeatureMetadata getMetadata() {
        return FeatureMetadata.builder()
                .name("@feature.chat-translation")
                .description("@feature.chat-translation.description")
                .icon(mindustry.gen.Icon.chat)
                .order(2)
                .quickAccess(true)
                .enabledByDefault(true)
                .build();
    }

    public class SendTranslatedMessageCallPacket extends SendMessageCallPacket {

        @Override
        public void handleClient() {
            handleMessage(this.message, translated -> {
                NetClient.sendMessage(translated);
            });
        }
    }

    public class SendTranslatedMessageCallPacket2 extends SendMessageCallPacket2 {
        @Override
        public void handleClient() {
            if (Vars.player != this.playersender) {
                handleMessage(this.message, translated -> {
                    NetClient.sendMessage(translated, this.unformatted,
                            this.playersender);
                });
            } else {
                NetClient.sendMessage(this.message, this.unformatted,
                        this.playersender);
            }
        }
    }

    @Override
    public void init() {
        Main.registerPacketPlacement(SendMessageCallPacket.class, SendTranslatedMessageCallPacket::new);
        Main.registerPacketPlacement(SendMessageCallPacket2.class, SendTranslatedMessageCallPacket2::new);

        providers.clear();
        providers.add(defaultTranslationProvider);
        providers.add(new DeepLTranslationProvider());
        providers.add(new GeminiTranslationProvider());

        providers.each(TranslationProvider::init);

        loadProvider();

        Events.on(ClientLoadEvent.class, e -> setupChatFragHook());
        Core.app.post(this::setupChatFragHook);
    }

    private void setupChatFragHook() {
        if (hookSetup) return;
        if (Vars.ui == null || Vars.ui.chatfrag == null) return;

        try {
            TextField chatfield = Reflect.get(Vars.ui.chatfrag, "chatfield");
            if (chatfield == null) return;
            hookSetup = true;

            chatfield.addListener(new InputListener() {
                @Override
                public boolean keyDown(InputEvent event, KeyCode keycode) {
                    if (keycode == KeyCode.t && Core.input.ctrl()) {
                        String text = chatfield.getText().trim();
                        if (!text.isEmpty()) {
                            translateOutgoing(text).thenAccept(res -> {
                                Core.app.post(() -> chatfield.setText(res.trim()));
                            });
                            return true;
                        }
                    }

                    if (keycode == KeyCode.enter && !Core.input.keyDown(KeyCode.shiftLeft)) {
                        String raw = chatfield.getText().trim();
                        if (!raw.isEmpty() && isEnabled()) {
                            boolean isExplicitTr = raw.startsWith("/tr ");
                            boolean isAutoReverse = ChatTranslationConfig.isReverseEnabled() && !raw.startsWith("/");

                            if (isExplicitTr || isAutoReverse) {
                                String textToTranslate = isExplicitTr ? raw.substring(4).trim() : raw;
                                if (!textToTranslate.isEmpty()) {
                                    event.stop();
                                    chatfield.clearText();
                                    Vars.ui.chatfrag.hide();

                                    translateOutgoing(textToTranslate).thenAccept(translated -> {
                                        String clean = (translated != null && !translated.trim().isEmpty()) ? translated.trim() : textToTranslate;
                                        String toSend = ChatTranslationConfig.isReverseIncludeOriginal()
                                                ? clean + " [lightgray](" + textToTranslate + ")"
                                                : clean;
                                        Call.sendChatMessage(toSend);
                                    }).exceptionally(err -> {
                                        Call.sendChatMessage(textToTranslate);
                                        return null;
                                    });
                                    return true;
                                }
                            }
                        }
                    }
                    return false;
                }
            });
        } catch (Exception e) {
            Log.err("ChatTranslationFeature: Failed to hook chatfrag", e);
        }
    }

    public void handleMessage(String message, Cons<String> cons) {
        if (!isEnabled() || !ChatTranslationConfig.isAutoTranslateIncoming()) {
            cons.get(message);
            return;
        }

        translateContent(message)
                .thenAccept(translated -> {
                    String cleanOrig = Strings.stripColors(message).trim();
                    String cleanTrans = translated != null ? Strings.stripColors(translated).trim() : "";
                    if (cleanTrans.isEmpty() || cleanTrans.equalsIgnoreCase(cleanOrig)) {
                        cons.get(message);
                    } else {
                        cons.get(Strings.format("@ [gold](@)[white]", message, translated.trim()));
                    }
                })
                .exceptionally(e -> {
                    lastError = e.getMessage();

                    String formated = Strings.format("@\n\n[scarlet]@[white]\n\n", message,
                            Core.bundle.get("chat-translation.error.prefix") + e.getMessage());

                    cons.get(formated);

                    Log.err(e.getMessage());
                    return null;
                });

    }

    public CompletableFuture<String> translateContent(String message) {
        if (!isEnabled()) {
            throw new IllegalArgumentException("ChatTranslationFeature is not enabled");
        }

        return currentProvider.translate(Strings.stripColors(message))
                .whenComplete((translated, error) -> {
                    if (error != null) {
                        Throwable cause = error.getCause() != null ? error.getCause() : error;
                        lastError = cause.getMessage();
                    }
                });
    }

    public CompletableFuture<String> translateOutgoing(String message) {
        if (!isEnabled()) {
            return CompletableFuture.completedFuture(message);
        }

        java.util.Locale locale = Core.bundle.getLocale();
        String sourceLang = (locale != null && locale.getLanguage() != null && !locale.getLanguage().isEmpty())
                ? locale.getLanguage().toLowerCase()
                : "vi";
        String targetLang = ChatTranslationConfig.getReverseTargetLang();

        return currentProvider.translate(Strings.stripColors(message), sourceLang, targetLang)
                .whenComplete((translated, error) -> {
                    if (error != null) {
                        Throwable cause = error.getCause() != null ? error.getCause() : error;
                        lastError = cause.getMessage();
                    }
                });
    }

    private void loadProvider() {
        String id = ChatTranslationConfig.getProviderId();
        currentProvider = providers.find(p -> p.getId().equals(id));

        if (currentProvider == null) {
            currentProvider = defaultTranslationProvider;
            ChatTranslationConfig.setProviderId(defaultTranslationProvider.getId());
        }
    }

    @Override
    public Optional<Dialog> setting() {
        return Optional.of(new ChatTranslationSettingsDialog(this));
    }

    Seq<TranslationProvider> getProviders() {
        return providers;
    }

    TranslationProvider getCurrentProvider() {
        return currentProvider;
    }

    void setCurrentProvider(TranslationProvider currentProvider) {
        this.currentProvider = currentProvider;
    }

    String getLastError() {
        return lastError;
    }
}
