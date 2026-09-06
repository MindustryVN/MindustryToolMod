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
import mindustry.game.EventType.Trigger;
import mindustry.gen.Call;
import mindustry.gen.Icon;
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
    private TextField lastHookedField = null;

    @Override
    public FeatureMetadata getMetadata() {
        return FeatureMetadata.builder()
                .name("@feature.chat-translation")
                .description("@feature.chat-translation.description")
                .icon(Icon.chat)
                .order(2)
                .quickAccess(true)
                .enabledByDefault(true)
                .build();
    }

    public class SendTranslatedMessageCallPacket extends SendMessageCallPacket {
        @Override
        public void handleClient() {
            handleIncomingMessage(this.message, translated -> {
                NetClient.sendMessage(translated);
            });
        }
    }

    public class SendTranslatedMessageCallPacket2 extends SendMessageCallPacket2 {
        @Override
        public void handleClient() {
            if (Vars.player != this.playersender && this.unformatted != null) {
                handleIncomingContent(this.message, this.unformatted, formatted -> {
                    NetClient.sendMessage(formatted, this.unformatted, this.playersender);
                });
            } else {
                NetClient.sendMessage(this.message, this.unformatted, this.playersender);
            }
        }
    }

    @Override
    public void init() {
        Main.registerPacketPlacement(SendMessageCallPacket.class, SendTranslatedMessageCallPacket::new);
        Main.registerPacketPlacement(SendMessageCallPacket2.class, SendTranslatedMessageCallPacket2::new);

        providers.clear();
        providers.add(defaultTranslationProvider);
        providers.add(new GeminiTranslationProvider());
        providers.add(new DeepLTranslationProvider());

        providers.each(TranslationProvider::init);
        loadProvider();

        Events.on(ClientLoadEvent.class, e -> setupChatFieldHook());
        Events.run(Trigger.update, this::setupChatFieldHook);
    }

    private void setupChatFieldHook() {
        if (Vars.ui == null || Vars.ui.chatfrag == null) return;

        try {
            TextField chatfield = Reflect.get(Vars.ui.chatfrag, "chatfield");
            if (chatfield == null || chatfield == lastHookedField) return;

            lastHookedField = chatfield;
            chatfield.addCaptureListener(createChatInputListener(chatfield));
        } catch (Exception e) {
            Log.err("Failed to hook chatfield for ChatTranslationFeature", e);
        }
    }

    private InputListener createChatInputListener(TextField chatfield) {
        return new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, KeyCode keycode) {
                if (!isEnabled()) return false;

                if (keycode == KeyCode.t && Core.input.ctrl()) {
                    return handleInlineTranslateHotkey(chatfield);
                }

                if (keycode == KeyCode.enter && !Core.input.keyDown(KeyCode.shiftLeft)) {
                    return handleOutgoingEnter(event, chatfield);
                }

                return false;
            }
        };
    }

    private boolean handleInlineTranslateHotkey(TextField chatfield) {
        String text = chatfield.getText().trim();
        if (text.isEmpty()) return false;

        translateOutgoing(text).thenAccept(result -> {
            if (result != null && !result.trim().isEmpty()) {
                Core.app.post(() -> {
                    chatfield.setText(result.trim());
                    chatfield.setCursorPosition(chatfield.getText().length());
                });
            }
        });
        return true;
    }

    private boolean handleOutgoingEnter(InputEvent event, TextField chatfield) {
        String raw = chatfield.getText().trim();
        if (raw.isEmpty()) return false;

        boolean isExplicitCommand = raw.startsWith("/tr ");
        boolean isAutoReverse = ChatTranslationConfig.isReverseEnabled() && !raw.startsWith("/");

        if (!isExplicitCommand && !isAutoReverse) return false;

        String textToTranslate = isExplicitCommand ? raw.substring(4).trim() : raw;
        if (textToTranslate.isEmpty()) return false;

        event.stop();
        event.cancel();
        chatfield.clearText();
        Vars.ui.chatfrag.clearChatInput();
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

    public void handleIncomingMessage(String fullMessage, Cons<String> callback) {
        if (!shouldTranslateIncoming(fullMessage)) {
            deliverMessage(fullMessage, callback);
            return;
        }

        translateContent(fullMessage).thenAccept(translated -> {
            String result = buildTranslatedDisplay(fullMessage, fullMessage, translated);
            deliverMessage(result, callback);
        }).exceptionally(err -> {
            deliverMessage(fullMessage, callback);
            return null;
        });
    }

    public void handleIncomingContent(String fullMessage, String rawContent, Cons<String> callback) {
        if (!shouldTranslateIncoming(rawContent)) {
            deliverMessage(fullMessage, callback);
            return;
        }

        translateContent(rawContent).thenAccept(translated -> {
            String result = buildTranslatedDisplay(fullMessage, rawContent, translated);
            deliverMessage(result, callback);
        }).exceptionally(err -> {
            deliverMessage(fullMessage, callback);
            return null;
        });
    }

    private boolean shouldTranslateIncoming(String text) {
        if (!isEnabled() || !ChatTranslationConfig.isAutoTranslateIncoming()) return false;
        if (text == null) return false;
        String clean = text.trim();
        return !clean.isEmpty() && !clean.startsWith("/");
    }

    private String buildTranslatedDisplay(String fullMessage, String originalContent, String translated) {
        if (translated == null || translated.trim().isEmpty()) return fullMessage;
        String cleanOrig = Strings.stripColors(originalContent).trim();
        String cleanTrans = Strings.stripColors(translated).trim();

        if (cleanTrans.equalsIgnoreCase(cleanOrig) || cleanTrans.isEmpty()) {
            return fullMessage;
        }
        return fullMessage + " [gold](" + translated.trim() + ")[white]";
    }

    private void deliverMessage(String text, Cons<String> callback) {
        Core.app.post(() -> callback.get(text));
    }

    public CompletableFuture<String> translateContent(String message) {
        if (!isEnabled()) {
            return CompletableFuture.completedFuture(message);
        }

        return getActiveProvider().translate(Strings.stripColors(message))
                .whenComplete((translated, error) -> {
                    if (error != null) {
                        Throwable cause = error.getCause() != null ? error.getCause() : error;
                        lastError = cause.getMessage();
                        Log.err("Translation error", cause);
                    }
                });
    }

    public CompletableFuture<String> translateOutgoing(String message) {
        if (!isEnabled()) {
            return CompletableFuture.completedFuture(message);
        }

        String targetLang = ChatTranslationConfig.getReverseTargetLang();
        return getActiveProvider().translate(Strings.stripColors(message), "auto", targetLang)
                .whenComplete((translated, error) -> {
                    if (error != null) {
                        Throwable cause = error.getCause() != null ? error.getCause() : error;
                        lastError = cause.getMessage();
                        Log.err("Outgoing translation error", cause);
                    }
                });
    }

    private TranslationProvider getActiveProvider() {
        if (currentProvider == null) {
            loadProvider();
        }
        return currentProvider != null ? currentProvider : defaultTranslationProvider;
    }

    private void loadProvider() {
        String id = ChatTranslationConfig.getProviderId();
        TranslationProvider found = providers.find(p -> p.getId().equalsIgnoreCase(id));

        if (found instanceof GeminiTranslationProvider gemini && !hasConfiguredKey(gemini)) {
            found = defaultTranslationProvider;
            ChatTranslationConfig.setProviderId("devx");
        } else if (found instanceof DeepLTranslationProvider deepl && !hasConfiguredKey(deepl)) {
            found = defaultTranslationProvider;
            ChatTranslationConfig.setProviderId("devx");
        }

        currentProvider = found != null ? found : defaultTranslationProvider;
    }

    private boolean hasConfiguredKey(TranslationProvider provider) {
        if (provider instanceof GeminiTranslationProvider) {
            String k = Core.settings.getString(ChatTranslationConfig.GEMINI_API_KEY, "");
            return !k.trim().isEmpty();
        }
        if (provider instanceof DeepLTranslationProvider) {
            String k = Core.settings.getString(ChatTranslationConfig.DEEPL_API_KEY, "");
            return !k.trim().isEmpty();
        }
        return true;
    }

    @Override
    public Optional<Dialog> setting() {
        return Optional.of(new ChatTranslationSettingsDialog(this));
    }

    Seq<TranslationProvider> getProviders() {
        return providers;
    }

    TranslationProvider getCurrentProvider() {
        return currentProvider != null ? currentProvider : defaultTranslationProvider;
    }

    void setCurrentProvider(TranslationProvider currentProvider) {
        this.currentProvider = currentProvider;
    }

    String getLastError() {
        return lastError;
    }
}
