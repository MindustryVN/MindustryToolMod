package mindustrytool.features.chat;

import static solim.ui.Ui.*;

import arc.Core;
import arc.graphics.Color;
import arc.input.KeyCode;
import arc.scene.Element;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustrytool.models.response.ChatMessage;
import solim.core.BaseComponent;
import solim.input.SolimTextField;
import solim.signal.Readable;
import solim.signal.Signal;

public class ChatInputView extends BaseComponent {

    private final ChatStore store;
    private final ChatService service;
    private final Signal<String> messageText = Signal.of("");

    public ChatInputView(ChatStore store, ChatService service) {
        this.store = store;
        this.service = service;
    }

    @Override
    protected Element build() {
        Readable<Boolean> hasReply = store.replyTarget().map(t -> t != null);

        return column().growX().gap(unit(1)).children(() -> {
            dynamic(hasReply, replying -> {
                if (Boolean.TRUE.equals(replying)) {
                    ChatMessage target = store.replyTarget().peek();
                    String targetName = target != null && target.getCreatedBy() != null ? target.getCreatedBy() : "message";
                    return row().growX().padding(unit(1)).gap(unit(1)).children(() -> {
                        image(Icon.leftSmall).size(unit(3), unit(3)).color(Pal.accent);
                        text(Core.bundle.format("feature.chat.ui.replying", targetName)).color(Color.lightGray).fontScale(0.85f).left();
                        spacer();
                        button(() -> store.setReplyTarget(null))
                                .style(Styles.clearNonei)
                                .size(unit(4), unit(4))
                                .children(() -> image(Icon.cancel).size(unit(3), unit(3)));
                    });
                }
                return row();
            });

            row().growX().gap(unit(1)).children(() -> {
                SolimTextField tf = textField(messageText)
                        .placeholder(Core.bundle.get("feature.chat.ui.placeholder", "Message..."))
                        .growX();

                tf.field().keyDown(key -> {
                    if (key == KeyCode.enter) {
                        onSend();
                    }
                });

                button(this::onSend)
                        .style(Styles.defaultb)
                        .width(unit(10))
                        .children(() -> {
                            image(Icon.play).size(unit(5), unit(5));
                        });
            });
        }).element();
    }

    private void onSend() {
        String content = messageText.peek();
        if (content == null || content.trim().isEmpty()) {
            return;
        }
        ChatMessage replyTarget = store.replyTarget().peek();
        String replyToId = replyTarget != null ? replyTarget.getId() : null;

        messageText.set("");
        service.sendMessage(content.trim(), replyToId).exceptionally(e -> {
            Core.app.post(() -> {
                Vars.ui.showInfo(Core.bundle.get("feature.chat.ui.send-failed", "Message failed to send."));
            });
            return null;
        });
    }
}
