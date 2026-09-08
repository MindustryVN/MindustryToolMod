package mindustrytool.features.chat;

import static solim.ui.Ui.*;

import arc.Core;
import arc.graphics.Color;
import arc.scene.Element;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustrytool.models.response.ChatMessage;
import solim.core.BaseComponent;
import solim.layout.Scroll;
import solim.signal.Readable;

public class ChatMessageListView extends BaseComponent {

    private final ChatStore store;
    private Scroll scrollPane;

    public ChatMessageListView(ChatStore store) {
        this.store = store;
    }

    @Override
    protected Element build() {
        Readable<Boolean> hasMessages = store.activeMessages().map(list -> list != null && !list.isEmpty());

        return column().grow().gap(unit(1)).children(() -> {
            scrollPane = scroll().grow().children(() -> {
                column().growX().gap(unit(1)).children(() -> {
                    dynamic(hasMessages, available -> {
                        if (Boolean.TRUE.equals(available)) {
                            return forEach(store.activeMessages(), ChatMessage::getId, msg -> new MessageItem(msg, store));
                        } else {
                            return column().padding(unit(4)).children(() -> {
                                text(Core.bundle.get("feature.chat.ui.empty-messages", "No messages yet."))
                                        .color(Color.gray)
                                        .fontScale(0.9f);
                            });
                        }
                    });
                });
            });
        }).element();
    }

    public void scrollToBottom() {
        if (scrollPane != null && scrollPane.pane() != null) {
            Core.app.post(() -> {
                if (scrollPane != null && scrollPane.pane() != null) {
                    scrollPane.pane().setScrollPercentY(1f);
                }
            });
        }
    }

    private static class MessageItem extends BaseComponent {
        private final ChatMessage message;
        private final ChatStore store;

        public MessageItem(ChatMessage message, ChatStore store) {
            this.message = message;
            this.store = store;
        }

        @Override
        protected Element build() {
            String author = message.getCreatedBy() != null ? message.getCreatedBy() : "Unknown";
            String textContent = message.getContent() != null ? message.getContent() : "";

            return card().growX().padding(unit(1)).children(() -> {
                column().growX().gap(unit(1)).children(() -> {
                    row().growX().gap(unit(1)).children(() -> {
                        text(author)
                                .color(Pal.accent)
                                .fontScale(0.95f)
                                .left();

                        spacer();

                        button(() -> store.setReplyTarget(message))
                                .style(Styles.clearNonei)
                                .size(unit(4), unit(4))
                                .children(() -> image(Icon.leftSmall).size(unit(3), unit(3)));
                    });

                    if (message.getReplyTo() != null && !message.getReplyTo().isEmpty()) {
                        row().growX().padding(unit(1)).children(() -> {
                            text(Core.bundle.format("feature.chat.ui.replying", message.getReplyTo()))
                                    .color(Color.gray)
                                    .fontScale(0.8f)
                                    .left();
                        });
                    }

                    row().growX().children(() -> {
                        text(textContent)
                                .color(Color.white)
                                .wrap()
                                .left()
                                .growX();
                    });
                });
            }).element();
        }
    }
}
