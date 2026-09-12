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

        listen(ResizeEvent.class, e -> {
            keepInScreen();
            Core.app.post(this::keepInScreen);
        });

        effect(() -> {
            isCollapsed.get();
            Core.app.post(this::keepInScreen);
        });

        Core.app.post(() -> {
            if (hud != null) {
                keepInScreen();
                hud.root().toFront();
            }
        });

        return hud.element();
    }

    private Component buildCollapsedBadge() {
        Readable<Boolean> hasUnread = store.unreadCount().map(count -> count != null && count > 0);

        return card()
                .rounded(10, new Color(0f, 0f, 0f, 0.6f))
                .margin(unit(4))
                .children(() -> {
                    button(() -> {
                        feature.collapsedConfig.set(false);
                        store.clearUnread();
                    })
                            .style(Styles.clearNonei)
                            .size(unit(14), unit(14))
                            .draggable(hud, feature.xSignal, feature.ySignal)
                            .children(() -> {
                                stack()
                                        .grow()
                                        .center()
                                        .layer(() -> icon(Icon.chat).size(unit(10)).center().grow().color(Color.white))
                                        .layer(() -> row().top().right().grow().visible(hasUnread).children(() -> {
                                            image(circle())
                                                    .margin(unit(1))
                                                    .size(unit(3))
                                                    .color(Color.white);
                                        }));
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

        return card(Styles.black8)
                .width(winWidth)
                .height(winHeight)
                .children(() -> {
                    column().grow().children(() -> {
                        // Window Action Bar (draggable bar wrapping title & action buttons)
                        row().growX()
                                .background(colored(new Color(0.45f, 0.35f, 0.9f, 0.3f), Tex.whiteui))
                                .padding(unit(1), unit(2), unit(1), unit(2))
                                .gap(unit(2))
                                .draggable(hud, feature.xSignal, feature.ySignal)
                                .children(() -> {
                                    image(circle())
                                            .size(unit(3))
                                            .color(isConnected.map(c -> c ? Pal.heal : Color.scarlet));

                                    spacer();

                                    button(() -> {
                                        var dialog = feature.getSettingDialog();
                                        if (dialog != null) {
                                            dialog.show();
                                        }
                                    })
                                            .style(Styles.clearNonei)
                                            .size(unit(10), unit(10))
                                            .tooltip(Core.bundle.get("feature.chat.ui.settings", "Settings"))
                                            .children(() -> icon(Icon.settings).size(unit(5)));

                                    button(() -> feature.collapsedConfig.set(true))
                                            .style(Styles.clearNonei)
                                            .size(unit(10), unit(10))
                                            .tooltip(Core.bundle.get("feature.chat.ui.collapse", "Collapse"))
                                            .children(() -> icon(Icon.cancel).size(unit(5)));
                                });

                        divider();

                        // Inverted to test mobile layout in desktop
                        if (Vars.mobile) {
                            buildMobileBody();
                        } else {
                            buildDesktopBody();
                        }
                    });
                });
    }

    private void buildDesktopBody() {
        row().grow().children(() -> {
            // Channel List
            row().width(unit(60)).growY().children(() -> {
                new ChatChannelListView(store);
            });

            divider(Direction.Y);

            // Message Area & Input
            column().grow().children(() -> {
                new ChatMessageListView(store, service);
                divider();
                new ChatInputView(store, service);
            });

            divider(Direction.Y);

            // User List
            row().width(unit(60)).growY().children(() -> {
                new ChatUserListView(store);
            });
        });
    }

    private Component buildMobileBody() {
        return tabs(mobileTab)
                .grow()
                .tab(Core.bundle.get("feature.chat.ui.channels", "Channels"), () -> {
                    new ChatChannelListView(store);
                })
                .tab(Core.bundle.get("feature.chat.ui.messages", "Messages"), () -> {
                    column().grow().gap(unit(1)).children(() -> {
                        new ChatMessageListView(store, service);
                        divider();
                        new ChatInputView(store, service);
                    });
                })
                .tab(Core.bundle.get("feature.chat.ui.members", "Members"), () -> {
                    new ChatUserListView(store);
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
