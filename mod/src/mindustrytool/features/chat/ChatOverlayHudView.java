package mindustrytool.features.chat;

import static solim.UI.*;

import arc.Core;
import arc.Events;
import arc.func.Cons;
import arc.graphics.Color;
import arc.scene.Element;
import mindustry.Vars;
import mindustry.game.EventType.ResizeEvent;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import solim.core.BaseComponent;
import solim.core.Component;
import solim.layout.Direction;
import solim.layout.SolimStack;
import solim.overlay.Hud;
import solim.signal.Effect;
import solim.signal.Readable;
import solim.signal.Signal;
import solim.ui.Units;

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
        hud.position(feature.xSignal, feature.ySignal);
        hud.toFrontOnTouch();

        Cons<ResizeEvent> resizeListener = e -> {
            keepInScreen();
            Core.app.post(this::keepInScreen);
        };
        Events.on(ResizeEvent.class, resizeListener);
        own(() -> Events.remove(ResizeEvent.class, resizeListener));

        own(Effect.of(() -> {
            isCollapsed.get();
            Core.app.post(this::keepInScreen);
        }));

        Core.app.post(() -> {
            if (hud != null) {
                keepInScreen();
                hud.root().toFront();
            }
        });

        return hud.element();
    }

    private Component buildCollapsedBadge() {
        Readable<Boolean> isConnected = store.connected();

        return card(Styles.black8)
                .children(() -> {
                    button(() -> {
                        feature.collapsedConfig.set(false);
                        store.clearUnread();
                    })
                            .style(Styles.clearNonei)
                            .size(unit(7), unit(7))
                            .draggable(hud, feature.xSignal, feature.ySignal)
                            .children(() -> {
                                component(new SolimStack()
                                        .layer(() -> image(Icon.chat).size(unit(5), unit(5)).color(Color.white))
                                        .layer(() -> row().bottom().right().children(() -> {
                                            image(Tex.whiteui)
                                                    .size(unit(1.5f), unit(1.5f))
                                                    .color(isConnected.map(c -> Boolean.TRUE.equals(c) ? Pal.heal : Color.scarlet));
                                        }))
                                        .layer(() -> row().top().right().children(() -> {
                                            badgeCount(store.unreadCount());
                                        })));
                            });
                });
    }

    private Component buildExpandedWindow() {
        Readable<Float> winWidth = feature.widthRatioConfig.signal().map(r -> {
            float sw = Units.screenWidth();
            float ratio = r != null ? r : 0.6f;
            float target = sw * Math.max(0.3f, Math.min(0.95f, ratio));
            return Math.max(320f, Math.min(sw * 0.95f, target));
        });
        Readable<Float> winHeight = feature.heightRatioConfig.signal().map(r -> {
            float sh = Units.screenHeight();
            float ratio = r != null ? r : 0.6f;
            float target = sh * Math.max(0.3f, Math.min(0.95f, ratio));
            return Math.max(240f, Math.min(sh * 0.95f, target));
        });

        Readable<Boolean> isConnected = store.connected();
        Readable<String> channelTitle = store.activeChannel().map(c -> c != null ? "# " + c.getName() : "Chat");

        return card(Styles.black8)
                .width(winWidth)
                .height(winHeight)
                .children(() -> {
                    column().grow().padding(unit(2)).children(() -> {
                        // Window Action Bar (draggable bar wrapping title & action buttons)
                        row().growX()
                                .background(Styles.black6)
                                .padding(unit(1), unit(2), unit(1), unit(2))
                                .gap(unit(1))
                                .draggable(hud, feature.xSignal, feature.ySignal)
                                .children(() -> {
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
            row().width(unit(60)).growY().children(() -> {
                component(new ChatChannelListView(store));
            });

            divider(Direction.Y);

            // Message Area & Input
            column().grow().gap(unit(1)).children(() -> {
                component(new ChatMessageListView(store, service));
                divider();
                component(new ChatInputView(store, service));
            });

            divider(Direction.Y);

            // User List
            row().width(unit(60)).growY().children(() -> {
                component(new ChatUserListView(store));
            });
        });
    }

    private Component buildMobileBody() {
        return tabs(mobileTab)
                .grow()
                .tab(Core.bundle.get("feature.chat.ui.channels", "Channels"), () -> {
                    component(new ChatChannelListView(store));
                })
                .tab(Core.bundle.get("feature.chat.ui.messages", "Messages"), () -> {
                    column().grow().gap(unit(1)).children(() -> {
                        component(new ChatMessageListView(store, service));
                        divider();
                        component(new ChatInputView(store, service));
                    });
                })
                .tab(Core.bundle.get("feature.chat.ui.members", "Members"), () -> {
                    component(new ChatUserListView(store));
                });
    }

    public void keepInScreen() {
        if (hud != null) {
            hud.root().invalidateHierarchy();
            hud.pack();
            hud.keepInScreen();
        }
    }
}
