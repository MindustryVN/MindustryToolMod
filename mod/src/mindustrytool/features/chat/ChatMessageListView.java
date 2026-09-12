package mindustrytool.features.chat;

import static solim.UI.*;

import arc.Core;
import arc.graphics.Color;
import arc.scene.Element;
import arc.util.Nullable;
import arc.util.Scaling;
import arc.util.Timer;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import mindustry.Vars;
import mindustry.game.Schematic;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.SchematicsDialog.SchematicImage;
import mindustrytool.components.FileIcon;
import mindustrytool.features.chat.models.MessageGroup;
import mindustrytool.features.chat.models.ParsedChatMessage;
import mindustrytool.features.chat.models.ParsedChatMessage.ImageMessage;
import mindustrytool.features.chat.models.ParsedChatMessage.MindustryToolLinkMessage;
import mindustrytool.features.chat.models.ParsedChatMessage.RoomInviteMessage;
import mindustrytool.features.chat.models.ParsedChatMessage.SchematicMessage;
import mindustrytool.features.chat.models.ParsedChatMessage.TextMessage;
import mindustrytool.models.response.ChatMessage;
import mindustrytool.models.response.UserData;
import solim.core.BaseComponent;
import solim.layout.Direction;
import solim.layout.VirtualList;
import solim.signal.Computed;
import solim.signal.Effect;
import solim.signal.Readable;

public class ChatMessageListView extends BaseComponent {

    private final ChatStore store;
    private final @Nullable ChatService service;
    private @Nullable VirtualList<MessageGroup, String> virtualList;

    private @Nullable String lastChannelId = null;
    private int lastMessageCount = 0;
    private @Nullable String lastFirstMessageId = null;

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
        Readable<Boolean> showEndOfHistory = new Computed<>(() -> {
            Boolean fully = store.activeChannelFullyLoaded().get();
            Boolean has = hasMessages.get();
            return Boolean.TRUE.equals(fully) && Boolean.TRUE.equals(has);
        });

        Readable<List<MessageGroup>> groupedMessages = new Computed<>(() -> {
            List<ChatMessage> msgs = store.activeMessages().get();
            if (msgs == null || msgs.isEmpty()) {
                return Collections.emptyList();
            }
            return ChatMessageGrouper.groupRaw(msgs);
        });

        Effect.of(() -> {
            String chanId = store.activeChannelId().get();
            if (!Objects.equals(chanId, lastChannelId)) {
                lastChannelId = chanId;
                lastMessageCount = 0;
                lastFirstMessageId = null;
                scrollToBottom();
            }
        });

        Effect.of(() -> {
            List<ChatMessage> msgs = store.activeMessages().get();
            if (msgs == null || msgs.isEmpty()) {
                lastMessageCount = 0;
                lastFirstMessageId = null;
                return;
            }

            int count = msgs.size();
            String firstId = msgs.get(0).getId();
            boolean isOlderPrepended = lastMessageCount > 0 && count > lastMessageCount
                    && !Objects.equals(firstId, lastFirstMessageId);
            boolean isNewAppended = lastMessageCount > 0 && count > lastMessageCount
                    && Objects.equals(firstId, lastFirstMessageId);

            if (isOlderPrepended) {
                float prevContentHeight = (virtualList != null) ? virtualList.getTotalHeight() : 0f;
                float prevScrollY = (virtualList != null && virtualList.pane() != null)
                        ? virtualList.pane().getScrollY()
                        : 0f;

                Core.app.post(() -> {
                    if (virtualList != null && virtualList.pane() != null) {
                        var pane = virtualList.pane();
                        pane.layout();
                        float newContentHeight = virtualList.getTotalHeight();
                        float heightDelta = newContentHeight - prevContentHeight;
                        if (heightDelta > 0) {
                            pane.setScrollYForce(prevScrollY + heightDelta);
                            pane.updateVisualScroll();
                        }
                    }
                });
            } else if (isNewAppended) {
                if (virtualList != null && virtualList.pane() != null) {
                    var pane = virtualList.pane();
                    boolean wasNearBottom = (pane.getMaxY() - pane.getScrollY()) <= 150f;
                    if (wasNearBottom) {
                        scrollToBottom();
                    }
                }
            } else if (lastMessageCount == 0) {
                scrollToBottom();
            }

            lastMessageCount = count;
            lastFirstMessageId = firstId;
        });

        Timer.schedule(this::scrollToBottom, 1);

        return column().grow().top().left().gap(unit(1)).children(() -> {
            dynamic(showEndOfHistory, show -> {
                if (Boolean.TRUE.equals(show)) {
                    return row().top().center().growX().padding(unit(2)).children(() -> {
                        text(Core.bundle.get("feature.chat.ui.end-of-history", "Beginning of chat history"))
                                .color(Color.gray)
                                .fontScale(0.85f);
                    });
                }
                return null;
            });

            dynamic(store.loadingOlder(), loading -> {
                if (Boolean.TRUE.equals(loading)) {
                    return row().top().left().padding(unit(2)).children(() -> {
                        text(Core.bundle.get("feature.chat.ui.loading-older", "Loading older messages..."))
                                .color(Color.gray)
                                .fontScale(0.85f)
                                .left();
                    });
                }
                return null;
            });

            dynamic(hasMessages, available -> {
                if (Boolean.TRUE.equals(available)) {
                    virtualList = virtualList(
                            groupedMessages,
                            MessageGroup::getKey,
                            ChatMessageHeightCalculator::calculateHeight,
                            item -> new MessageGroupView(item, store, service))
                                    .grow()
                                    .gap(unit(0.75f))
                                    .overscan(3)
                                    .onReachTop(50f, () -> {
                                        String activeId = store.activeChannelId().peek();
                                        var msgs = store.activeMessages().peek();
                                        if (activeId != null && !activeId.isEmpty() && service != null && msgs != null
                                                && !msgs.isEmpty() && !Boolean.TRUE.equals(store.loadingOlder().peek())
                                                && !store.isFullyLoaded(activeId)) {
                                            service.fetchOlderMessages(activeId);
                                        }
                                    });
                    return virtualList;
                } else {
                    return column().padding(unit(4)).top().left().children(() -> {
                        text(Core.bundle.get("feature.chat.ui.empty-messages", "No messages yet."))
                                .color(Color.gray)
                                .fontScale(0.9f)
                                .left();
                    });
                }
            }).grow();
        }).element();
    }

    public void scrollToBottom() {
        if (virtualList != null) {
            Core.app.post(() -> {
                if (virtualList != null && virtualList.pane() != null) {
                    var pane = virtualList.pane();
                    pane.layout();
                    pane.setScrollYForce(pane.getMaxY());
                    pane.updateVisualScroll();
                    Core.app.post(() -> {
                        if (virtualList != null && virtualList.pane() != null) {
                            var p = virtualList.pane();
                            p.layout();
                            p.setScrollYForce(p.getMaxY());
                            p.updateVisualScroll();
                        }
                    });
                }
            });
        }
    }

    private static class MessageGroupView extends BaseComponent {
        private final MessageGroup group;
        private final ChatStore store;
        private final @Nullable ChatService service;

        public MessageGroupView(MessageGroup group, ChatStore store, @Nullable ChatService service) {
            this.group = group;
            this.store = store;
            this.service = service;
        }

        @Override
        protected Element build() {
            ParsedChatMessage first = group.getMessage(0);
            ChatMessage firstRaw = first.getRaw();
            String authorId = group.getAuthorId();

            Readable<UserData> user = store.user(authorId);
            Readable<String> authorName = user.map(u -> (u != null && u.getName() != null && !u.getName().isEmpty())
                    ? u.getName()
                    : (authorId != null ? authorId : "Unknown"));

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

            Readable<String> avatarUrl = user
                    .map(u -> (u != null && u.getImageUrl() != null && !u.getImageUrl().isEmpty())
                            ? u.getImageUrl()
                            : null);

            String timeStr = formatTime(group.getCreatedAt());

            return card()
                    .growX()
                    .top().left()
                    .children(() -> {
                        row().growX().top().left()
                                .padding(unit(1))
                                .gap(unit(1.5f))
                                .children(() -> {
                                    // Shared group avatar on the left, pinned to the top
                                    new ChatAvatar(authorName, avatarUrl, authorId, unit(12)).top();

                                    // Right column: header followed by stacked messages
                                    column().growX().top().left().gap(unit(0.5f)).children(() -> {
                                        // Author and timestamp header + action button
                                        row().growX().top().left().gap(unit(1)).children(() -> {
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

                                            // Action ellipsis / menu button
                                            button(() -> openActions(firstRaw))
                                                    .style(Styles.clearNonei)
                                                    .size(unit(6), unit(6))
                                                    .children(() -> icon(FileIcon.of("ellipsis-vertical.png"))
                                                            .size(unit(6), unit(6)));
                                        });

                                        // Stacked message rows with a tight gap
                                        column().growX().top().left().gap(unit(0.75f)).children(() -> {
                                            for (ParsedChatMessage parsed : group.getMessages()) {
                                                buildMessageRow(parsed);
                                            }
                                        });
                                    });
                                });
                    }).element();
        }

        private void buildMessageRow(ParsedChatMessage parsed) {
            ChatMessage raw = parsed.getRaw();
            String msgId = raw.getId();
            boolean mentioned = (parsed instanceof TextMessage) && ((TextMessage) parsed).isMentionsCurrentUser();

            Readable<Set<String>> pendingIds = store.pendingMessageIds();
            Readable<Set<String>> failedIds = store.failedMessageIds();
            Readable<Boolean> isPending = pendingIds.map(set -> set != null && set.contains(msgId));
            Readable<Boolean> isFailed = failedIds.map(set -> set != null && set.contains(msgId));

            card().growX().top().left()
                    .onClick(() -> openActions(raw))
                    .children(() -> {
                        row().growX().top().left().gap(unit(1)).children(() -> {
                            if (mentioned) {
                                divider(Direction.Y).color(Pal.accent).width(unit(1));
                            }

                            column().growX().top().left().gap(unit(0.5f)).children(() -> {
                                if (raw.getReplyTo() != null && !raw.getReplyTo().isEmpty()) {
                                    buildReplyPreview(raw.getReplyTo());
                                }

                                buildMessageBody(parsed, isPending, isFailed);

                                dynamic(isFailed, failed -> {
                                    if (Boolean.TRUE.equals(failed)) {
                                        return row().top().left().gap(unit(1)).padding(unit(0.5f)).children(() -> {
                                            button(Core.bundle.get("feature.chat.ui.retry", "Retry"), () -> {
                                                String tempId = "temp_" + UUID.randomUUID();
                                                String content = raw.getContent();
                                                String activeChId = store.activeChannelId().peek();

                                                ChatMessage retryMsg = new ChatMessage();
                                                retryMsg.setId(tempId);
                                                retryMsg.setCreatedBy(raw.getCreatedBy());
                                                retryMsg.setCreatedAt(Instant.now().toString());
                                                retryMsg.setContent(content);
                                                retryMsg.setReplyTo(raw.getReplyTo());
                                                retryMsg.setChannelId(activeChId);

                                                store.removeFailedMessage(msgId);
                                                store.addPendingMessage(tempId);
                                                store.replaceMessage(msgId, retryMsg);

                                                if (service != null) {
                                                    service.sendMessage(content, raw.getReplyTo())
                                                            .whenComplete((realMsg, err) -> {
                                                                Core.app.post(() -> {
                                                                    if (err != null) {
                                                                        store.removePendingMessage(tempId);
                                                                        store.addFailedMessage(tempId);
                                                                    } else {
                                                                        store.removePendingMessage(tempId);
                                                                        boolean realExists = store
                                                                                .hasMessage(realMsg.getId());
                                                                        if (realExists) {
                                                                            store.removeMessage(tempId);
                                                                        } else {
                                                                            store.replaceMessage(tempId, realMsg);
                                                                        }
                                                                    }
                                                                });
                                                            });
                                                }
                                            }).style(Styles.defaultt).height(unit(6))
                                                    .color(Color.scarlet);
                                        });
                                    }
                                    return null;
                                });
                            });
                        });
                    });
        }

        private void openActions(ChatMessage message) {
            new MessageActionDialog(message, store, service).show();
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
            row().growX().top().left().gap(unit(1)).padding(unit(0.5f)).children(() -> {
                icon(Icon.rightSmall).size(unit(4), unit(4)).color(Color.gray);
                text(displaySnippet)
                        .color(Color.gray)
                        .fontScale(0.8f)
                        .ellipsis()
                        .left();
            });
        }

        private void buildMessageBody(ParsedChatMessage parsed, Readable<Boolean> isPending,
                Readable<Boolean> isFailed) {
            Readable<Color> bodyColor = new Computed<>(() -> {
                if (Boolean.TRUE.equals(isFailed.get())) {
                    return Color.scarlet;
                }
                if (Boolean.TRUE.equals(isPending.get())) {
                    return new Color(1f, 1f, 1f, 0.5f);
                }
                return Color.white;
            });

            if (parsed instanceof RoomInviteMessage) {
                RoomInviteMessage invite = (RoomInviteMessage) parsed;
                final String link = invite.getConnectLink();
                card().growX().top().left().children(() -> {
                    column().growX().top().left().padding(unit(1.5f)).gap(unit(1)).children(() -> {
                        row().growX().top().left().gap(unit(1)).children(() -> {
                            icon(Icon.host).size(unit(5), unit(5)).color(Pal.accent);
                            text(Core.bundle.get("feature.chat.ui.room-invite", "Room Invite"))
                                    .color(Pal.accent)
                                    .fontScale(0.95f)
                                    .left();
                        });
                        text(link).color(Color.lightGray).fontScale(0.85f).ellipsis().left();
                        row().top().left().gap(unit(1)).children(() -> {
                            button(Core.bundle.get("button.copy", "Copy Link"), () -> {
                                Core.app.setClipboardText(link);
                                Vars.ui.showInfoFade(Core.bundle.get("feature.chat.ui.copied", "Copied!"));
                            }).style(Styles.defaultt).height(unit(7));
                        });
                    });
                });
                return;
            }

            if (parsed instanceof ImageMessage) {
                ImageMessage img = (ImageMessage) parsed;
                final String imageUrl = img.getImageUrl();
                column().growX().top().left().gap(unit(1)).children(() -> {
                    text(imageUrl).color(Color.lightGray).fontScale(0.85f).wrap().left().growX();
                    networkImage(imageUrl)
                            .placeholder(Icon.image)
                            .fallback(Icon.cancel)
                            .size(unit(40), unit(30))
                            .top().left();
                });
                return;
            }

            if (parsed instanceof MindustryToolLinkMessage) {
                MindustryToolLinkMessage toolLink = (MindustryToolLinkMessage) parsed;
                final String fullUrl = toolLink.getFullUrl();
                final String type = toolLink.getType();
                final String itemId = toolLink.getItemId();

                card().growX().top().left().children(() -> {
                    column().growX().top().left().padding(unit(1.5f)).gap(unit(1)).children(() -> {
                        row().growX().top().left().gap(unit(1)).children(() -> {
                            icon("maps".equals(type) ? Icon.map : Icon.paste).size(unit(5), unit(5)).color(Pal.accent);
                            text(("maps".equals(type) ? "Map: " : "Schematic: ") + itemId)
                                    .color(Pal.accent)
                                    .fontScale(0.95f)
                                    .ellipsis()
                                    .left();
                        });
                        row().top().left().gap(unit(1)).children(() -> {
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

            if (parsed instanceof SchematicMessage) {
                SchematicMessage schemMsg = (SchematicMessage) parsed;
                if (schemMsg.getPrefixText() != null && !schemMsg.getPrefixText().isEmpty()) {
                    text(schemMsg.getPrefixText()).color(bodyColor).wrap().left().growX();
                }

                buildSchematicCard(schemMsg.getSchematic());

                if (schemMsg.getSuffixText() != null && !schemMsg.getSuffixText().isEmpty()) {
                    text(schemMsg.getSuffixText()).color(bodyColor).wrap().left().growX();
                }
                return;
            }

            if (parsed instanceof TextMessage) {
                TextMessage txt = (TextMessage) parsed;
                text(txt.getText())
                        .color(bodyColor)
                        .left()
                        .wrap()
                        .growX();
                return;
            }

            // Fallback
            String fallback = parsed.getContent() != null ? parsed.getContent() : "";
            text(fallback)
                    .color(bodyColor)
                    .left()
                    .wrap()
                    .growX();
        }

        private void buildSchematicCard(Schematic schematic) {
            card().top().left().children(() -> {
                column().top().left().gap(unit(1)).children(() -> {
                    row().growX().top().left().gap(unit(1)).children(() -> {
                        text(schematic.name())
                                .color(Pal.accent)
                                .fontScale(0.95f)
                                .ellipsis()
                                .left()
                                .growX();

                        spacer();

                        button(() -> Vars.ui.schematics.showInfo(schematic))
                                .style(Styles.clearNonei)
                                .size(unit(6), unit(6))
                                .tooltip(Core.bundle.get("info.title", "Info"))
                                .children(() -> icon(Icon.infoSmall).size(unit(5), unit(5)));

                        button(() -> Vars.ui.schematics.showExport(schematic))
                                .style(Styles.clearNonei)
                                .size(unit(6), unit(6))
                                .tooltip(Core.bundle.get("editor.export", "Export"))
                                .children(() -> icon(Icon.upload).size(unit(5), unit(5)));

                        button(() -> Vars.ui.schematics.showEdit(schematic))
                                .style(Styles.clearNonei)
                                .size(unit(6), unit(6))
                                .tooltip(Core.bundle.get("schematic.edit", "Edit"))
                                .children(() -> icon(Icon.pencil).size(unit(5), unit(5)));

                        button(() -> useSchematic(schematic))
                                .style(Styles.clearNonei)
                                .size(unit(6), unit(6))
                                .tooltip(Core.bundle.get("feature.chat.ui.schematic.use", "Use"))
                                .children(() -> icon(Icon.play).size(unit(5), unit(5)));
                    });

                    float width = schematic.width / schematic.height * unit(35);

                    button(() -> useSchematic(schematic))
                            .style(Styles.flatt)
                            .height(unit(35))
                            .width(width)
                            .children(() -> {
                                arc(new SchematicImage(schematic).setScaling(Scaling.fit));
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
