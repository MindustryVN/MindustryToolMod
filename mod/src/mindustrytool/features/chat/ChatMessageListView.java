package mindustrytool.features.chat;

import static solim.UI.*;

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
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mindustry.Vars;
import mindustry.game.Schematic;
import mindustry.game.Schematics;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.SchematicsDialog.SchematicImage;
import mindustrytool.features.FeatureManager;
import mindustrytool.features.translation.TranslationFeature;
import mindustrytool.models.response.ChatMessage;
import mindustrytool.models.response.UserData;
import mindustrytool.services.MindustryTool;
import solim.core.BaseComponent;
import solim.layout.Scroll;
import solim.signal.Computed;
import solim.signal.Effect;
import solim.signal.Readable;

public class ChatMessageListView extends BaseComponent {

    private static final Pattern MINDUSTRY_TOOL_LINK_PATTERN = Pattern
            .compile("^https?://[^/]+/(?:[^/]+/)?(schematics|maps)/([a-zA-Z0-9_-]+)");
    private static final Pattern IMAGE_URL_PATTERN = Pattern
            .compile("^https?://.*\\.(?:png|jpg|jpeg|gif|webp)(?:\\?.*)?$", Pattern.CASE_INSENSITIVE);

    private final ChatStore store;
    private final @Nullable ChatService service;
    private @Nullable Scroll scrollPane;

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
                float prevContentHeight = (scrollPane != null && scrollPane.content() != null)
                        ? scrollPane.content().getPrefHeight()
                        : 0f;
                float prevScrollY = (scrollPane != null && scrollPane.pane() != null) ? scrollPane.pane().getScrollY()
                        : 0f;

                Core.app.post(() -> {
                    if (scrollPane != null && scrollPane.pane() != null && scrollPane.content() != null) {
                        var pane = scrollPane.pane();
                        pane.layout();
                        float newContentHeight = scrollPane.content().getPrefHeight();
                        float heightDelta = newContentHeight - prevContentHeight;
                        if (heightDelta > 0) {
                            pane.setScrollYForce(prevScrollY + heightDelta);
                            pane.updateVisualScroll();
                        }
                    }
                });
            } else if (isNewAppended) {
                if (scrollPane != null && scrollPane.pane() != null) {
                    var pane = scrollPane.pane();
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

        Core.app.post(this::scrollToBottom);

        return column().grow().top().left().gap(unit(1)).children(() -> {
            scrollPane = scroll()
                    .grow()
                    .left()
                    .onReachTop(50f, () -> {
                        String activeId = store.activeChannelId().peek();
                        var msgs = store.activeMessages().peek();
                        if (activeId != null && !activeId.isEmpty() && service != null && msgs != null
                                && !msgs.isEmpty() && !Boolean.TRUE.equals(store.loadingOlder().peek())
                                && !store.isFullyLoaded(activeId)) {
                            service.fetchOlderMessages(activeId);
                        }
                    })
                    .children(() -> {
                        column().growX().top().left().gap(unit(1)).children(() -> {
                            dynamic(showEndOfHistory, show -> {
                                if (Boolean.TRUE.equals(show)) {
                                    return row().top().center().growX().padding(unit(2)).children(() -> {
                                        text(Core.bundle.get("feature.chat.ui.end-of-history",
                                                "Beginning of chat history"))
                                                .color(Color.gray)
                                                .fontScale(0.85f);
                                    });
                                }
                                return row();
                            });

                            dynamic(store.loadingOlder(), loading -> {
                                if (Boolean.TRUE.equals(loading)) {
                                    return row().top().left().padding(unit(2)).children(() -> {
                                        text(Core.bundle.get("feature.chat.ui.loading-older",
                                                "Loading older messages..."))
                                                        .color(Color.gray)
                                                        .fontScale(0.85f)
                                                        .left();
                                    });
                                }
                                return row();
                            });

                            dynamic(hasMessages, available -> {
                                if (Boolean.TRUE.equals(available)) {
                                    return forEach(displayMessages, dm -> dm.message.getId(),
                                            dm -> new MessageItem(dm, store, service)).growX();
                                } else {
                                    return column().padding(unit(4)).top().left().children(() -> {
                                        text(Core.bundle.get("feature.chat.ui.empty-messages", "No messages yet."))
                                                .color(Color.gray)
                                                .fontScale(0.9f)
                                                .left();
                                    });
                                }
                            }).growX();
                        });
                    });
        }).element();
    }

    public void scrollToBottom() {
        if (scrollPane != null) {
            Core.app.post(() -> {
                if (scrollPane != null && scrollPane.pane() != null) {
                    var pane = scrollPane.pane();
                    pane.layout();
                    pane.setScrollYForce(pane.getMaxY());
                    pane.updateVisualScroll();
                    Core.app.post(() -> {
                        if (scrollPane != null && scrollPane.pane() != null) {
                            var p = scrollPane.pane();
                            p.layout();
                            p.setScrollYForce(p.getMaxY());
                            p.updateVisualScroll();
                        }
                    });
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

            Readable<String> avatarUrl = user
                    .map(u -> (u != null && u.getImageUrl() != null && !u.getImageUrl().isEmpty())
                            ? u.getImageUrl()
                            : null);

            String timeStr = formatTime(message.getCreatedAt());
            String rawContent = message.getContent() != null ? message.getContent() : "";

            return card()
                    .growX()
                    .top().left()
                    .onClick(() -> store.toggleExpanded(message.getId()))
                    .children(() -> {
                        row().growX().top().left().padding(unit(1)).gap(unit(1.5f)).children(() -> {
                            // Left Avatar or indent spacer
                            if (isFirst) {
                                networkImage(avatarUrl)
                                        .placeholder(Icon.players)
                                        .fallback(Icon.players)
                                        .size(unit(8), unit(8))
                                        .top().left();
                            } else {
                                row().width(unit(8)).top().left();
                            }

                            // Content area
                            column().growX().top().left().gap(unit(0.5f)).children(() -> {
                                // Author and timestamp header
                                if (isFirst) {
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
                                    });
                                }

                                // Reply target preview
                                if (message.getReplyTo() != null && !message.getReplyTo().isEmpty()) {
                                    buildReplyPreview(message.getReplyTo());
                                }

                                // Body
                                buildMessageBody(rawContent);

                                // Translated text card
                                dynamic(store.translation(message.getId()), trans -> {
                                    if (trans != null && !trans.isEmpty()) {
                                        return card(Styles.black3, () -> {
                                            column().growX().padding(unit(1)).gap(unit(0.5f)).left().children(() -> {
                                                row().growX().gap(unit(1)).left().children(() -> {
                                                    text("[#58a6ff]🌐 " + Core.bundle
                                                            .get("feature.chat.ui.translated-badge", "Translated"))
                                                                    .fontScale(0.75f)
                                                                    .color(Pal.accent);
                                                });
                                                text(trans)
                                                        .color(Color.white)
                                                        .fontScale(0.9f)
                                                        .wrap()
                                                        .left()
                                                        .growX();
                                            });
                                        }).growX();
                                    }
                                    return row();
                                });

                                // Interactive action bar
                                dynamic(store.expandedMessageId(), expId -> {
                                    if (Objects.equals(expId, message.getId())) {
                                        return row().growX().top().left().padding(unit(1)).gap(unit(1)).children(() -> {
                                            button(Core.bundle.get("button.copy", "Copy"), () -> {
                                                try {
                                                    Core.app.setClipboardText(rawContent);
                                                    Vars.ui.showInfoFade(Core.bundle.get("feature.chat.ui.copied",
                                                            "Copied to clipboard!"));
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

            TranslationFeature tf = FeatureManager.getFeature(TranslationFeature.class);
            CompletableFuture<String> future;
            if (tf != null && tf.isEnabled() && tf.getActiveProvider().isConfigured()) {
                future = tf.translate(message.getContent(), tf.getTargetLanguage());
            } else {
                future = MindustryTool.translate(message.getContent(), targetLocale);
            }

            future.whenComplete((res, err) -> {
                Core.app.post(() -> {
                    if (err != null || res == null) {
                        Vars.ui.showInfoToast(Core.bundle.get("feature.chat.ui.translate-failed", "Translation failed"),
                                2f);
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
            row().growX().top().left().gap(unit(1)).padding(unit(0.5f)).children(() -> {
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
                card().growX().top().left().children(() -> {
                    column().growX().top().left().padding(unit(1.5f)).gap(unit(1)).children(() -> {
                        row().growX().top().left().gap(unit(1)).children(() -> {
                            image(Icon.host).size(unit(5), unit(5)).color(Pal.accent);
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

            if (IMAGE_URL_PATTERN.matcher(content).matches()) {
                final String imageUrl = content;
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

            Matcher matcher = MINDUSTRY_TOOL_LINK_PATTERN.matcher(content);
            if (matcher.find()) {
                String fullUrl = matcher.group(0);
                String type = matcher.group(1);
                String itemId = matcher.group(2);

                card().growX().top().left().children(() -> {
                    column().growX().top().left().padding(unit(1.5f)).gap(unit(1)).children(() -> {
                        row().growX().top().left().gap(unit(1)).children(() -> {
                            image("maps".equals(type) ? Icon.map : Icon.paste).size(unit(5), unit(5)).color(Pal.accent);
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
                        text(prevText).color(Color.white).wrap().left().growX();
                    }

                    final Schematic finalSchem = schematic;
                    buildSchematicCard(finalSchem);

                    if (!after.isEmpty()) {
                        final String afterText = after;
                        text(afterText).color(Color.white).wrap().left().growX();
                    }
                    return;
                }
            }

            // Standard text message
            final String text = content;
            text(text)
                    .color(Color.white)
                    .left()
                    .wrap()
                    .growX();
        }

        private void buildSchematicCard(Schematic schematic) {
            card().growX().top().left().children(() -> {
                column().growX().top().left().padding(unit(1.5f)).gap(unit(1)).children(() -> {
                    row().growX().top().left().gap(unit(1)).children(() -> {
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
                                component(() -> new SchematicImage(schematic).setScaling(Scaling.fit));
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
