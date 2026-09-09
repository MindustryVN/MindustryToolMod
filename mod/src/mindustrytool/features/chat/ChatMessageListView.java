package mindustrytool.features.chat;

import static solim.ui.Ui.*;

import arc.Core;
import arc.graphics.Color;
import arc.scene.Element;
import arc.util.Nullable;
import arc.util.Scaling;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import mindustry.Vars;
import mindustry.game.Schematic;
import mindustry.game.Schematics;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.SchematicsDialog.SchematicImage;
import mindustrytool.models.response.ChatMessage;
import mindustrytool.models.response.UserData;
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
                            return forEach(store.activeMessages(), ChatMessage::getId,
                                    msg -> new MessageItem(msg, store));
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
            Readable<UserData> user = store.user(message.getCreatedBy());
            Readable<String> authorName = user.map(u -> (u != null && u.getName() != null && !u.getName().isEmpty())
                    ? u.getName()
                    : (message.getCreatedBy() != null ? message.getCreatedBy() : "Unknown"));

            Readable<Color> authorColor = user.map(u -> {
                if (u != null && u.getHighestRole().isPresent()) {
                    try {
                        String hex = u.getHighestRole().get().getColor();
                        if (hex != null && !hex.isEmpty()) {
                            return Color.valueOf(hex);
                        }
                    } catch (Exception ignored) {
                    }
                }
                return Pal.accent;
            });

            String timeStr = formatTime(message.getCreatedAt());
            String rawContent = message.getContent() != null ? message.getContent() : "";

            return card().growX().children(() -> {
                column().growX().padding(unit(1)).gap(unit(1)).children(() -> {
                    // Header: Author name, timestamp, action buttons (reply, copy)
                    row().growX().gap(unit(1)).children(() -> {
                        text(authorName)
                                .color(authorColor)
                                .fontScale(0.95f)
                                .left();

                        if (!timeStr.isEmpty()) {
                            text(timeStr)
                                    .color(Color.gray)
                                    .fontScale(0.8f)
                                    .left();
                        }

                        spacer();

                        button(() -> store.setReplyTarget(message))
                                .style(Styles.clearNonei)
                                .size(unit(6), unit(6))
                                .tooltip(Core.bundle.get("feature.chat.ui.reply", "Reply"))
                                .children(() -> image(Icon.leftSmall).size(unit(4), unit(4)));

                        button(() -> {
                            try {
                                Core.app.setClipboardText(rawContent);
                                Vars.ui.showInfoFade(Core.bundle.get("feature.chat.ui.copied", "Copied to clipboard!"));
                            } catch (Exception ignored) {
                            }
                        })
                                .style(Styles.clearNonei)
                                .size(unit(6), unit(6))
                                .tooltip(Core.bundle.get("feature.chat.ui.copy", "Copy"))
                                .children(() -> image(Icon.copy).size(unit(4), unit(4)));
                    });

                    // Reply target preview (if replyTo is set)
                    if (message.getReplyTo() != null && !message.getReplyTo().isEmpty()) {
                        buildReplyPreview(message.getReplyTo());
                    }

                    // Content rendering (Schematic or text)
                    buildMessageBody(rawContent);
                });
            }).element();
        }

        private void buildReplyPreview(String replyToId) {
            ChatMessage target = null;
            var list = store.activeMessages().peek();
            if (list != null) {
                for (ChatMessage m : list) {
                    if (Objects.equals(m.getId(), replyToId)) {
                        target = m;
                        break;
                    }
                }
            }

            String targetSnippet;
            if (target != null && target.getContent() != null) {
                String clean = target.getContent().replace('\n', ' ').trim();
                targetSnippet = clean.length() > 40 ? clean.substring(0, 37) + "..." : clean;
            } else {
                targetSnippet = replyToId;
            }

            final String displaySnippet = targetSnippet;
            row().growX().gap(unit(1)).padding(unit(0.5f)).children(() -> {
                image(Icon.rightSmall).size(unit(4), unit(4)).color(Color.gray);
                text(displaySnippet)
                        .color(Color.gray)
                        .fontScale(0.8f)
                        .ellipsis()
                        .left();
            });
        }

        private void buildMessageBody(String content) {
            content = content.trim();
            int schemPos = content.indexOf(Vars.schematicBaseStart);

            if (schemPos != -1) {
                int endPos = content.indexOf(" ", schemPos);
                if (endPos == -1) {
                    endPos = content.length();
                }

                String prev = content.substring(0, schemPos).trim();
                String base64 = content.substring(schemPos, endPos).trim();
                String after = content.substring(endPos).trim();

                Schematic schematic = null;
                try {
                    schematic = Schematics.readBase64(base64);
                } catch (Exception ignored) {
                }

                if (schematic != null) {
                    if (!prev.isEmpty()) {
                        final String prevText = prev;
                        row().growX().children(() -> {
                            text(prevText).color(Color.white).wrap().left().growX();
                        });
                    }

                    final Schematic finalSchem = schematic;
                    row().growX().children(() -> {
                        buildSchematicCard(finalSchem);
                    });

                    if (!after.isEmpty()) {
                        final String afterText = after;
                        row().growX().children(() -> {
                            text(afterText).color(Color.white).wrap().left().growX();
                        });
                    }
                    return;
                }
            }

            // Standard text message
            final String text = content;
            row().growX().children(() -> {
                text(text)
                        .color(Color.white)
                        .wrap()
                        .left()
                        .growX();
            });
        }

        private void buildSchematicCard(Schematic schematic) {
            card().growX().children(() -> {
                column().growX().padding(unit(1.5f)).gap(unit(1)).children(() -> {
                    row().growX().gap(unit(1)).children(() -> {
                        text(schematic.name())
                                .color(Pal.accent)
                                .fontScale(0.95f)
                                .ellipsis()
                                .left()
                                .growX();

                        button(() -> Vars.ui.schematics.showInfo(schematic))
                                .style(Styles.clearNonei)
                                .size(unit(6), unit(6))
                                .tooltip(Core.bundle.get("info.title", "Info"))
                                .children(() -> image(Icon.info).size(unit(4), unit(4)));

                        button(() -> Vars.ui.schematics.showExport(schematic))
                                .style(Styles.clearNonei)
                                .size(unit(6), unit(6))
                                .tooltip(Core.bundle.get("editor.export", "Export"))
                                .children(() -> image(Icon.upload).size(unit(4), unit(4)));

                        button(() -> useSchematic(schematic))
                                .style(Styles.clearNonei)
                                .size(unit(6), unit(6))
                                .tooltip(Core.bundle.get("feature.chat.ui.schematic.use", "Use"))
                                .children(() -> image(Icon.play).size(unit(4), unit(4)));
                    });

                    button(() -> useSchematic(schematic))
                            .style(Styles.flatt)
                            .height(unit(35))
                            .children(() -> {
                                add(new SchematicImage(schematic).setScaling(Scaling.fit));
                            });
                });
            });
        }

        private void useSchematic(Schematic schematic) {
            if (Vars.state.isMenu()) {
                Vars.ui.schematics.showInfo(schematic);
            } else {
                if (!Vars.state.rules.schematicsAllowed) {
                    Vars.ui.showInfo(Core.bundle.get("schematic.disabled", "Schematics are disabled."));
                } else {
                    Vars.control.input.useSchematic(schematic);
                }
            }
        }
    }

    private static String formatTime(@Nullable String createdAt) {
        if (createdAt == null || createdAt.isEmpty()) {
            return "";
        }
        try {
            Instant instant = Instant.parse(createdAt);
            return DateTimeFormatter.ofPattern("HH:mm")
                    .withZone(ZoneId.systemDefault())
                    .format(instant);
        } catch (Throwable ignored) {
            return "";
        }
    }
}
