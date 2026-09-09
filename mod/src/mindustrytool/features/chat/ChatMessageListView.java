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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mindustry.Vars;
import mindustry.game.Schematic;
import mindustry.game.Schematics;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.SchematicsDialog.SchematicImage;
import mindustrytool.models.response.ChatMessage;
import mindustrytool.models.response.UserData;
import mindustrytool.services.MindustryTool;
import solim.core.BaseComponent;
import solim.layout.Scroll;
import solim.signal.Computed;
import solim.signal.Readable;

public class ChatMessageListView extends BaseComponent {

    private static final Pattern MINDUSTRY_TOOL_LINK_PATTERN = Pattern.compile("^https?://[^/]+/(?:[^/]+/)?(schematics|maps)/([a-zA-Z0-9_-]+)");
    private static final Pattern IMAGE_URL_PATTERN = Pattern.compile("^https?://.*\\.(?:png|jpg|jpeg|gif|webp)(?:\\?.*)?$", Pattern.CASE_INSENSITIVE);

    private final ChatStore store;
    private final @Nullable ChatService service;
    private @Nullable Scroll scrollPane;

    public ChatMessageListView(ChatStore store) {
        this(store, null);
    }

    public ChatMessageListView(ChatStore store, @Nullable ChatService service) {
        this.store = store;
        this.service = service;
    }

    @Override
    protected Element build() {
        Readable<Boolean> hasMessages = store.activeMessages().map(list -> list != null && !list.isEmpty());

        Computed<List<DisplayMessage>> displayMessages = new Computed<>(() -> {
            List<ChatMessage> msgs = store.activeMessages().get();
            if (msgs == null || msgs.isEmpty()) {
                return Collections.emptyList();
            }
            List<DisplayMessage> result = new ArrayList<>(msgs.size());
            String lastAuthor = null;
            for (ChatMessage msg : msgs) {
                boolean isFirst = !Objects.equals(lastAuthor, msg.getCreatedBy());
                result.add(new DisplayMessage(msg, isFirst));
                lastAuthor = msg.getCreatedBy();
            }
            return result;
        });

        return column().grow().gap(unit(1)).children(() -> {
            scrollPane = scroll()
                    .grow()
                    .onReachTop(50f, () -> {
                        String activeId = store.activeChannelId().peek();
                        if (activeId != null && !activeId.isEmpty() && service != null) {
                            service.fetchOlderMessages(activeId);
                        }
                    })
                    .children(() -> {
                        column().growX().gap(unit(1)).children(() -> {
                            dynamic(store.loadingOlder(), loading -> {
                                if (Boolean.TRUE.equals(loading)) {
                                    return row().center().padding(unit(2)).children(() -> {
                                        text(Core.bundle.get("feature.chat.ui.loading-older", "Loading older messages..."))
                                                .color(Color.gray)
                                                .fontScale(0.85f);
                                    });
                                }
                                return row();
                            });

                            dynamic(hasMessages, available -> {
                                if (Boolean.TRUE.equals(available)) {
                                    return forEach(displayMessages, dm -> dm.message.getId(),
                                            dm -> new MessageItem(dm, store, service));
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

    public static class DisplayMessage {
        public final ChatMessage message;
        public final boolean isFirstInGroup;

        public DisplayMessage(ChatMessage message, boolean isFirstInGroup) {
            this.message = message;
            this.isFirstInGroup = isFirstInGroup;
        }
    }

    private static class MessageItem extends BaseComponent {
        private final DisplayMessage displayMessage;
        private final ChatStore store;
        private final @Nullable ChatService service;

        public MessageItem(DisplayMessage displayMessage, ChatStore store, @Nullable ChatService service) {
            this.displayMessage = displayMessage;
            this.store = store;
            this.service = service;
        }

        @Override
        protected Element build() {
            ChatMessage message = displayMessage.message;
            boolean isFirst = displayMessage.isFirstInGroup;

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

            Readable<String> avatarUrl = user.map(u -> (u != null && u.getImageUrl() != null && !u.getImageUrl().isEmpty())
                    ? u.getImageUrl()
                    : null);

            String timeStr = formatTime(message.getCreatedAt());
            String rawContent = message.getContent() != null ? message.getContent() : "";

            return card()
                    .growX()
                    .onClick(() -> store.toggleExpanded(message.getId()))
                    .children(() -> {
                        row().growX().padding(unit(1)).gap(unit(1.5f)).children(() -> {
                            // Left Avatar or indent spacer
                            if (isFirst) {
                                networkImage(avatarUrl)
                                        .placeholder(Icon.players)
                                        .fallback(Icon.players)
                                        .size(unit(8), unit(8));
                            } else {
                                row().width(unit(8));
                            }

                            // Content area
                            column().growX().gap(unit(0.5f)).children(() -> {
                                // Author and timestamp header
                                if (isFirst) {
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
                                    });
                                }

                                // Reply target preview
                                if (message.getReplyTo() != null && !message.getReplyTo().isEmpty()) {
                                    buildReplyPreview(message.getReplyTo());
                                }

                                // Body
                                buildMessageBody(rawContent);

                                // Translated text preview
                                dynamic(store.translation(message.getId()), trans -> {
                                    if (trans != null && !trans.isEmpty()) {
                                        return row().growX().padding(unit(0.5f)).children(() -> {
                                            text(trans)
                                                    .color(Color.lightGray)
                                                    .fontScale(0.9f)
                                                    .wrap()
                                                    .left()
                                                    .growX();
                                        });
                                    }
                                    return row();
                                });

                                // Interactive action bar
                                dynamic(store.expandedMessageId(), expId -> {
                                    if (Objects.equals(expId, message.getId())) {
                                        return row().growX().padding(unit(1)).gap(unit(1)).children(() -> {
                                            button(Core.bundle.get("button.copy", "Copy"), () -> {
                                                try {
                                                    Core.app.setClipboardText(rawContent);
                                                    Vars.ui.showInfoFade(Core.bundle.get("feature.chat.ui.copied", "Copied to clipboard!"));
                                                } catch (Exception ignored) {
                                                }
                                                store.toggleExpanded(message.getId());
                                            }).style(Styles.defaultt).height(unit(7));

                                            button(Core.bundle.get("feature.chat.ui.reply", "Reply"), () -> {
                                                store.setReplyTarget(message);
                                                store.toggleExpanded(message.getId());
                                            }).style(Styles.defaultt).height(unit(7));

                                            button(Core.bundle.get("feature.chat.ui.translate", "Translate"), () -> {
                                                handleTranslate(message);
                                            }).style(Styles.defaultt).height(unit(7));
                                        });
                                    }
                                    return row();
                                });
                            });
                        });
                    }).element();
        }

        private void handleTranslate(ChatMessage message) {
            String current = store.translation(message.getId()).peek();
            if (current != null) {
                store.setTranslation(message.getId(), null);
                store.toggleExpanded(message.getId());
                return;
            }

            String targetLocale = "en";
            if (Vars.ui != null && Vars.ui.language != null && Vars.ui.language.getLocale() != null) {
                targetLocale = Vars.ui.language.getLocale().getLanguage();
            }

            Vars.ui.showInfoToast(Core.bundle.get("feature.chat.ui.translating", "Translating..."), 2f);
            MindustryTool.translate(message.getContent(), targetLocale).whenComplete((res, err) -> {
                Core.app.post(() -> {
                    if (err != null || res == null) {
                        Vars.ui.showInfoToast(Core.bundle.get("feature.chat.ui.translate-failed", "Translation failed"), 2f);
                    } else {
                        store.setTranslation(message.getId(), res);
                    }
                    store.toggleExpanded(message.getId());
                });
            });
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

            if (content.startsWith("player-connect://")) {
                final String link = content;
                card().growX().children(() -> {
                    column().growX().padding(unit(1.5f)).gap(unit(1)).children(() -> {
                        row().growX().gap(unit(1)).children(() -> {
                            image(Icon.host).size(unit(5), unit(5)).color(Pal.accent);
                            text(Core.bundle.get("feature.chat.ui.room-invite", "Room Invite"))
                                    .color(Pal.accent)
                                    .fontScale(0.95f)
                                    .left();
                        });
                        text(link).color(Color.lightGray).fontScale(0.85f).ellipsis().left();
                        row().gap(unit(1)).children(() -> {
                            button(Core.bundle.get("button.copy", "Copy Link"), () -> {
                                Core.app.setClipboardText(link);
                                Vars.ui.showInfoFade(Core.bundle.get("feature.chat.ui.copied", "Copied!"));
                            }).style(Styles.defaultt).height(unit(7));
                        });
                    });
                });
                return;
            }

            if (IMAGE_URL_PATTERN.matcher(content).matches()) {
                final String imageUrl = content;
                column().growX().gap(unit(1)).children(() -> {
                    text(imageUrl).color(Color.lightGray).fontScale(0.85f).wrap().left().growX();
                    networkImage(imageUrl)
                            .placeholder(Icon.image)
                            .fallback(Icon.cancel)
                            .size(unit(40), unit(30));
                });
                return;
            }

            Matcher matcher = MINDUSTRY_TOOL_LINK_PATTERN.matcher(content);
            if (matcher.find()) {
                String fullUrl = matcher.group(0);
                String type = matcher.group(1);
                String itemId = matcher.group(2);

                card().growX().children(() -> {
                    column().growX().padding(unit(1.5f)).gap(unit(1)).children(() -> {
                        row().growX().gap(unit(1)).children(() -> {
                            image("maps".equals(type) ? Icon.map : Icon.paste).size(unit(5), unit(5)).color(Pal.accent);
                            text(("maps".equals(type) ? "Map: " : "Schematic: ") + itemId)
                                    .color(Pal.accent)
                                    .fontScale(0.95f)
                                    .ellipsis()
                                    .left();
                        });
                        row().gap(unit(1)).children(() -> {
                            button(Core.bundle.get("button.copy", "Copy Link"), () -> {
                                Core.app.setClipboardText(fullUrl);
                                Vars.ui.showInfoFade(Core.bundle.get("feature.chat.ui.copied", "Copied!"));
                            }).style(Styles.defaultt).height(unit(7));

                            button(Core.bundle.get("feature.chat.ui.view-web", "View in Browser"), () -> {
                                Core.app.openURI(fullUrl);
                            }).style(Styles.defaultt).height(unit(7));
                        });
                    });
                });
                return;
            }

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

                        button(() -> Vars.ui.schematics.showEdit(schematic))
                                .style(Styles.clearNonei)
                                .size(unit(6), unit(6))
                                .tooltip(Core.bundle.get("schematic.edit", "Edit"))
                                .children(() -> image(Icon.pencil).size(unit(4), unit(4)));

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

