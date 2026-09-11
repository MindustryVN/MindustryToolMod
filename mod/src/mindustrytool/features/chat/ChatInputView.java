package mindustrytool.features.chat;

import static solim.UI.*;

import arc.Core;
import arc.graphics.Color;
import arc.scene.Element;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustrytool.models.response.ChatMessage;
import mindustrytool.models.response.UserData;
import mindustrytool.services.auth.AuthOverlay;
import solim.core.BaseComponent;
import solim.signal.Readable;
import solim.signal.Signal;

public class ChatInputView extends BaseComponent {

    private final ChatStore store;
    private final ChatService service;
    private final Signal<String> messageText = Signal.of("");
    private final Signal<Boolean> isSending = Signal.of(false);

    public ChatInputView(ChatStore store, ChatService service) {
        this.store = store;
        this.service = service;
    }

    @Override
    protected Element build() {
        Readable<Boolean> isLoggedIn = store.loggedIn().map(l -> Boolean.TRUE.equals(l));
        Readable<Boolean> isNotLoggedIn = isLoggedIn.map(l -> !l);
        Readable<Boolean> canSend = isSending.map(s -> !s);

        return column().growX().gap(unit(1)).children(() -> {
            // Login banner when not logged in
            row().growX().padding(unit(1)).visible(isNotLoggedIn).children(() -> {
                button(Core.bundle.get("auth.login", "Login"), () -> AuthOverlay.getInstance().startLoginUI())
                        .style(Styles.defaultt)
                        .growX()
                        .height(unit(10));
            });

            // Composer area when logged in
            column().growX().gap(unit(1)).visible(isLoggedIn).children(() -> {
                dynamic(store.replyTarget(), target -> {
                    if (target == null) {
                        return null;
                    }
                    String authorId = target.getCreatedBy();
                    UserData cachedUser = (authorId != null && store.userCache().peek() != null)
                            ? store.userCache().peek().get(authorId)
                            : null;
                    String targetName = (cachedUser != null && cachedUser.getName() != null)
                            ? cachedUser.getName()
                            : (authorId != null ? authorId : "message");
                    return row().growX().padding(unit(1)).gap(unit(1)).children(() -> {
                        image(Icon.leftSmall).size(unit(4), unit(4)).color(Pal.accent);
                        text(Core.bundle.format("feature.chat.ui.replying", targetName)).color(Color.lightGray)
                                .fontScale(0.85f).left();
                        spacer();
                        button(() -> store.setReplyTarget(null))
                                .style(Styles.clearNonei)
                                .size(unit(6), unit(6))
                                .children(() -> image(Icon.cancel).size(unit(4), unit(4)));
                    });
                });

                row().growX().gap(unit(1)).children(() -> {
                    textField(messageText)
                            .placeholder(Core.bundle.get("feature.chat.ui.placeholder", "Message..."))
                            .validator(this::isValidInput)
                            .onEnter(this::onSend)
                            .disabled(isSending)
                            .growX();

                    button(() -> new AttachContentDialog(this::handleAttachContent).show())
                            .style(Styles.defaultb)
                            .size(unit(10))
                            .children(() -> image(Icon.file).size(unit(5), unit(5)));

                    button(this::onSend)
                            .style(Styles.defaultb)
                            .enabled(canSend)
                            .width(unit(10))
                            .children(() -> image(Icon.play).size(unit(5), unit(5)));
                });
            });
        }).element();
    }

    private void handleAttachContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return;
        }
        sendDirect(content.trim());
    }

    private void onSend() {
        String content = messageText.peek();
        if (content == null || content.trim().isEmpty()) {
            Vars.ui.showInfoFade(Core.bundle.get("feature.chat.ui.empty-content", "Message cannot be empty."));
            return;
        }
        if (!isValidInput(content)) {
            return;
        }

        messageText.set("");
        sendDirect(content.trim());
    }

    private void sendDirect(String content) {
        if (Boolean.TRUE.equals(isSending.peek())) {
            return;
        }

        ChatMessage replyTarget = store.replyTarget().peek();
        String replyToId = replyTarget != null ? replyTarget.getId() : null;

        isSending.set(true);
        service.sendMessage(content, replyToId).whenComplete((msg, err) -> {
            Core.app.post(() -> {
                isSending.set(false);
                if (err != null) {
                    handleSendError(err);
                }
            });
        });
    }

    private void handleSendError(Throwable err) {
        String errStr = err.toString();
        if (errStr.contains("409") || (err.getMessage() != null && err.getMessage().contains("409"))) {
            Vars.ui.showInfoToast(Core.bundle.get("feature.chat.ui.rate-limited", "Rate limited. Please wait."), 3f);
        } else {
            Vars.ui.showInfoToast(Core.bundle.get("feature.chat.ui.send-failed", "Message failed to send."), 3f);
        }
    }

    private boolean isValidInput(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        String trimmed = text.trim();
        if (trimmed.startsWith(Vars.schematicBaseStart)) {
            return trimmed.length() <= 2056 * 12;
        }
        if (trimmed.startsWith("TVNB")) {
            return trimmed.length() <= 1024 * 1024 * 5;
        }
        return trimmed.length() <= 2056;
    }
}
