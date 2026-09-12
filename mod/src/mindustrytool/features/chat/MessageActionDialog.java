package mindustrytool.features.chat;

import arc.Core;
import arc.graphics.Color;
import arc.util.Nullable;
import java.util.concurrent.CompletableFuture;
import mindustry.Vars;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustrytool.features.FeatureManager;
import mindustrytool.features.translation.TranslationFeature;
import mindustrytool.models.response.ChatMessage;
import mindustrytool.services.MindustryTool;
import solim.UI;
import solim.overlay.SolimDialog;
import solim.signal.Signal;

/**
 * Modal action dialog triggered by the ellipsis button on a chat message,
 * providing Copy, Reply, and modal Translation without altering the message list's layout height.
 */
public class MessageActionDialog extends SolimDialog {

    private final ChatMessage message;
    private final ChatStore store;
    private final @Nullable ChatService service;

    private final Signal<Boolean> translating = Signal.of(false);
    private final Signal<String> translatedText = Signal.of(null);

    public MessageActionDialog(ChatMessage message, ChatStore store, @Nullable ChatService service) {
        super(Core.bundle.get("feature.chat.ui.actions", "Message Actions"));
        this.message = message;
        this.store = store;
        this.service = service;

        String existingTranslation = store.translation(message.getId()).peek();
        if (existingTranslation != null) {
            this.translatedText.set(existingTranslation);
        }

        children(() -> {
            UI.column().growX().padding(UI.unit(3)).gap(UI.unit(2)).children(() -> {
                // Action: Copy
                UI.button(Core.bundle.get("feature.chat.ui.copy", "Copy"), () -> {
                    try {
                        String content = message.getContent() != null ? message.getContent() : "";
                        Core.app.setClipboardText(content);
                        Vars.ui.showInfoFade(Core.bundle.get("feature.chat.ui.copied", "Copied to clipboard!"));
                    } catch (Throwable ignored) {
                    }
                    hide();
                }).style(Styles.defaultb).growX().height(UI.unit(10));

                // Action: Reply
                UI.button(Core.bundle.get("feature.chat.ui.reply", "Reply"), () -> {
                    store.setReplyTarget(message);
                    hide();
                }).style(Styles.defaultb).growX().height(UI.unit(10));

                // Action: Translate
                UI.button(Core.bundle.get("feature.chat.ui.translate", "Translate"), this::performTranslate)
                        .style(Styles.defaultb)
                        .growX()
                        .height(UI.unit(10));

                // Translation result area
                UI.dynamic(translating, isTranslating -> {
                    if (Boolean.TRUE.equals(isTranslating)) {
                        return UI.row().growX().center().padding(UI.unit(2)).children(() -> {
                            UI.text(Core.bundle.get("feature.chat.ui.translating", "Translating..."))
                                    .color(Color.gray)
                                    .fontScale(0.9f);
                        });
                    }
                    return null;
                });

                UI.dynamic(translatedText, text -> {
                    if (text != null && !text.isEmpty()) {
                        return UI.card(Styles.black3, () -> {
                            UI.column().growX().padding(UI.unit(2)).gap(UI.unit(1.5f)).left().children(() -> {
                                UI.row().growX().gap(UI.unit(1)).left().children(() -> {
                                    UI.text("[#58a6ff]🌐 " + Core.bundle.get("feature.chat.ui.translated-badge", "Translated"))
                                            .fontScale(0.8f)
                                            .color(Pal.accent);
                                });

                                UI.text(text)
                                        .color(Color.white)
                                        .fontScale(0.95f)
                                        .wrap()
                                        .left()
                                        .growX();

                                UI.button(Core.bundle.get("feature.chat.ui.copy-translation", "Copy Translation"), () -> {
                                    try {
                                        Core.app.setClipboardText(text);
                                        Vars.ui.showInfoFade(Core.bundle.get("feature.chat.ui.copied", "Copied to clipboard!"));
                                    } catch (Throwable ignored) {
                                    }
                                }).style(Styles.defaultt).height(UI.unit(7));
                            });
                        }).growX();
                    }
                    return null;
                });
            });
        });

        addCloseButton();
    }

    private void performTranslate() {
        if (Boolean.TRUE.equals(translating.get())) {
            return;
        }

        String current = translatedText.get();
        if (current != null) {
            translatedText.set(null);
            store.setTranslation(message.getId(), null);
            return;
        }

        String targetLocale = "en";
        if (Vars.ui != null && Vars.ui.language != null && Vars.ui.language.getLocale() != null) {
            targetLocale = Vars.ui.language.getLocale().getLanguage();
        }

        translating.set(true);

        TranslationFeature tf = FeatureManager.getFeature(TranslationFeature.class);
        CompletableFuture<String> future;
        if (tf != null && tf.isEnabled() && tf.getActiveProvider().isConfigured()) {
            future = tf.translate(message.getContent(), tf.getTargetLanguage());
        } else {
            future = MindustryTool.translate(message.getContent(), targetLocale);
        }

        future.whenComplete((res, err) -> {
            Core.app.post(() -> {
                translating.set(false);
                if (err != null || res == null) {
                    Vars.ui.showInfoToast(Core.bundle.get("feature.chat.ui.translate-failed", "Translation failed"), 2f);
                } else {
                    translatedText.set(res);
                    store.setTranslation(message.getId(), res);
                }
            });
        });
    }
}
