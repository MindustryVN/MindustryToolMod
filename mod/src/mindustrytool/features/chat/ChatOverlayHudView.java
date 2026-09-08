package mindustrytool.features.chat;

import static solim.ui.Ui.*;

import arc.Core;
import arc.graphics.Color;
import arc.input.KeyCode;
import arc.scene.Element;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import solim.core.BaseComponent;
import solim.core.Component;
import solim.overlay.Hud;
import solim.signal.Readable;
import solim.signal.Signal;

public class ChatOverlayHudView extends BaseComponent {

    private final ChatFeature feature;
    private final ChatStore store;
    private final ChatService service;
    private final Signal<Integer> mobileTab = Signal.of(1); // 0: Channels, 1: Messages, 2: Members

    private Hud hud;

    public ChatOverlayHudView(ChatFeature feature) {
        this.feature = feature;
        this.store = feature.getStore();
        this.service = feature.getService();
    }

    @Override
    protected Element build() {
        Readable<Boolean> isCollapsed = feature.collapsedConfig.signal();

        hud = hud(() -> {
            dynamic(isCollapsed, collapsed -> {
                if (Boolean.TRUE.equals(collapsed)) {
                    return buildCollapsedBadge();
                } else {
                    return buildExpandedWindow();
                }
            });
        });

        hud.opacity(feature.opacityConfig.signal());
        hud.scale(feature.scaleConfig.signal());
        hud.position(feature.xSignal, feature.ySignal);

        hud.root().addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
                hud.root().toFront();
                return false;
            }
        });

        Core.app.post(() -> {
            if (hud != null) {
                hud.root().invalidateHierarchy();
                hud.pack();
                hud.keepInScreen();
                hud.root().toFront();
            }
        });

        return hud.element();
    }

    private Component buildCollapsedBadge() {
        Readable<Boolean> isConnected = store.connected();
        Readable<Integer> unread = store.unreadCount();

        return card(Styles.black8)
                .padding(unit(1))
                .children(() -> {
                    row().gap(unit(1)).children(() -> {
                        // Drag handle
                        button()
                                .style(Styles.clearNonei)
                                .size(unit(7), unit(7))
                                .children(() -> image(Icon.move).size(unit(5), unit(5)).color(Color.lightGray))
                                .draggable(feature.xSignal, feature.ySignal);

                        // Clickable pill to expand
                        button(() -> {
                            feature.collapsedConfig.set(false);
                            store.clearUnread();
                        })
                                .style(Styles.flatt)
                                .height(unit(7))
                                .children(() -> {
                                    row().gap(unit(1)).children(() -> {
                                        image(Icon.chat).size(unit(5), unit(5)).color(Color.white);

                                        image(Tex.whiteui)
                                                .size(unit(2), unit(2))
                                                .color(isConnected.map(c -> c ? Pal.heal : Color.scarlet));

                                        text(unread.map(count -> (count != null && count > 0)
                                                ? String.valueOf(count)
                                                : Core.bundle.get("feature.chat.name", "Chat")))
                                                        .color(unread
                                                                .map(count -> (count != null && count > 0) ? Pal.accent
                                                                        : Color.white))
                                                        .fontScale(0.9f);
                                    });
                                });
                    });
                });
    }

    private Component buildExpandedWindow() {
        Readable<Float> winWidth = feature.widthRatioConfig.signal().map(r -> {
            float sw = Core.graphics.getWidth();
            float ratio = r != null ? r : 0.6f;
            return Math.max(320f, sw * Math.max(0.3f, Math.min(1.0f, ratio)));
        });
        Readable<Float> winHeight = feature.heightRatioConfig.signal().map(r -> {
            float sh = Core.graphics.getHeight();
            float ratio = r != null ? r : 0.6f;
            return Math.max(240f, sh * Math.max(0.3f, Math.min(1.0f, ratio)));
        });

        Readable<Boolean> isConnected = store.connected();
        Readable<String> channelTitle = store.activeChannel().map(c -> c != null ? "# " + c.getName() : "Chat");

        return card(Styles.black8)
                .width(winWidth)
                .height(winHeight)
                .padding(unit(2))
                .children(() -> {
                    column().grow().children(() -> {
                        // Header
                        row().growX().gap(unit(1)).padding(unit(1)).children(() -> {
                            button()
                                    .style(Styles.clearNonei)
                                    .size(unit(7), unit(7))
                                    .children(() -> image(Icon.move).size(unit(5), unit(5)).color(Color.lightGray))
                                    .draggable(feature.xSignal, feature.ySignal);

                            text(channelTitle)
                                    .color(Pal.accent)
                                    .fontScale(1.1f)
                                    .left();

                            image(Tex.whiteui)
                                    .size(unit(2), unit(2))
                                    .color(isConnected.map(c -> c ? Pal.heal : Color.scarlet));

                            spacer();

                            button(() -> {
                                var dialog = feature.getSettingDialog();
                                if (dialog != null) {
                                    dialog.show();
                                }
                            })
                                    .style(Styles.clearNonei)
                                    .size(unit(7), unit(7))
                                    .tooltip(Core.bundle.get("feature.chat.ui.settings", "Settings"))
                                    .children(() -> image(Icon.settingsSmall).size(unit(5), unit(5)));

                            button(() -> feature.collapsedConfig.set(true))
                                    .style(Styles.clearNonei)
                                    .size(unit(7), unit(7))
                                    .tooltip(Core.bundle.get("feature.chat.ui.collapse", "Collapse"))
                                    .children(() -> image(Icon.downOpen).size(unit(5), unit(5)));
                        });

                        divider();

                        // Responsive content body
                        if (Vars.mobile) {
                            buildMobileBody();
                        } else {
                            buildDesktopBody();
                        }
                    });
                });
    }

    private void buildDesktopBody() {
        row().grow().gap(unit(1)).children(() -> {
            // Channel List
            row().width(unit(35)).growY().children(() -> {
                new ChatChannelListView(store);
            });

            divider();

            // Message Area & Input
            column().grow().gap(unit(1)).children(() -> {
                new ChatMessageListView(store);
                divider();
                new ChatInputView(store, service);
            });

            divider();

            // User List
            row().width(unit(35)).growY().children(() -> {
                new ChatUserListView(store);
            });
        });
    }

    private void buildMobileBody() {
        column().grow().children(() -> {
            // Mobile Tabs
            row().growX().gap(unit(1)).children(() -> {
                button(() -> mobileTab.set(0))
                        .style(Styles.cleart)
                        .children(() -> {
                            text(Core.bundle.get("feature.chat.ui.channels", "Channels"))
                                    .color(mobileTab.map(t -> t == 0 ? Pal.accent : Color.white));
                        })
                        .growX();

                button(() -> mobileTab.set(1))
                        .style(Styles.cleart)
                        .children(() -> {
                            text(Core.bundle.get("feature.chat.ui.messages", "Messages"))
                                    .color(mobileTab.map(t -> t == 1 ? Pal.accent : Color.white));
                        })
                        .growX();

                button(() -> mobileTab.set(2))
                        .style(Styles.cleart)
                        .children(() -> {
                            text(Core.bundle.get("feature.chat.ui.members", "Members"))
                                    .color(mobileTab.map(t -> t == 2 ? Pal.accent : Color.white));
                        })
                        .growX();
            });

            divider();

            row().grow().children(() -> {
                dynamic(mobileTab, tab -> {
                    if (tab == 0) {
                        return new ChatChannelListView(store);
                    } else if (tab == 2) {
                        return new ChatUserListView(store);
                    } else {
                        return column().grow().gap(unit(1)).children(() -> {
                            new ChatMessageListView(store);
                            divider();
                            new ChatInputView(store, service);
                        });
                    }
                });
            });
        });
    }

    public void keepInScreen() {
        if (hud != null) {
            hud.keepInScreen();
        }
    }
}
