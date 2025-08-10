package mindustrytool.gui;

import java.util.concurrent.TimeUnit;

import arc.Core;
import arc.scene.ui.layout.Table;
import arc.util.Align;
import arc.util.Log;
import arc.util.Strings;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.gen.Iconc;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustrytool.config.Debouncer;
import mindustrytool.data.PlayerConnectRoom;
import mindustrytool.net.Api;
import mindustrytool.playerconnect.PlayerConnect;
import mindustrytool.playerconnect.PlayerConnectLink;

public class PlayerConnectRoomsDialog extends mindustry.ui.dialogs.BaseDialog {
    private final Table roomList = new Table();
    private final Debouncer debouncer = new Debouncer(250, TimeUnit.MILLISECONDS);
    private String searchTerm = "";

    public PlayerConnectRoomsDialog() {
        super("@message.room-list.title");
        addCloseButton();

        try {
            cont.table(container -> {
                container.table(topBar -> {
                    topBar.field(searchTerm, (result) -> {
                        searchTerm = result;
                        debouncer.debounce(this::setupPlayerConnect);
                    })//
                            .left()
                            .growX()
                            .get()
                            .setMessageText(Core.bundle.format("@map.search"));

                })
                        .top()
                        .left()
                        .growX();

                container.row();
                container.add(roomList)
                        .grow()
                        .top()
                        .left();

                container.row();
            })
                    .top()
                    .left()
                    .growX();
            cont
                    .top()
                    .left();

            buttons
                    .button(Icon.refresh, Styles.squarei, () -> setupPlayerConnect())
                    .size(64)
                    .padRight(8);

            shown(() -> {
                setupPlayerConnect();
            });
        } catch (Throwable e) {
            Log.err(e);
        }
    }

    public void setupPlayerConnect() {
        roomList.clear();
        roomList.labelWrap(Core.bundle.format("message.loading"))
                .center()
                .labelAlign(0)
                .expand()
                .fill();

        Api.findPlayerConnectRooms(searchTerm, rooms -> {
            roomList.clear();

            roomList.pane(pane -> {
                if (rooms.isEmpty()) {
                    pane.labelWrap(Core.bundle.format("message.no-rooms-found"))
                            .center()
                            .labelAlign(0)
                            .expand()
                            .fill();
                    return;
                }

                pane.table(list -> {
                    for (PlayerConnectRoom room : rooms) {
                        list.table(card -> {
                            card.table(left -> {
                                left.add(
                                        room.data().name() + " [white]" + (room.data().isSecured() ? Iconc.lock : ""))
                                        .fontScale(1.5f)
                                        .align(Align.left)
                                        .left();

                                left.row();
                                left.add(Iconc.map + " " + Core.bundle.format("save.map", room.data().mapName())
                                        + "[lightgray] / " + room.data().gamemode())
                                        .align(Align.left).left();

                                left.row();
                                left.add(
                                        Iconc.players + " " + Core.bundle.format("players", room.data().players().size))
                                        .align(Align.left)
                                        .left();

                                if (room.data().mods().size > 0) {
                                    left.row();
                                    left.add(Iconc.book + " " + Strings.join(",", room.data().mods())).align(Align.left)
                                            .left();
                                }
                            })
                                    .growX()
                                    .top()
                                    .left();
                            card.table(right -> {
                                right.button(Iconc.play + " " + Core.bundle.format("join"), () -> {
                                    if (!room.data().isSecured()) {
                                        try {
                                            PlayerConnect.joinRoom(
                                                    PlayerConnectLink.fromString(room.link()), "",
                                                    () -> hide());
                                        } catch (Throwable e) {
                                            hide();
                                            setupPlayerConnect();
                                            Vars.ui.showException("@message.connect.fail", e);
                                        }

                                        return;
                                    }

                                    BaseDialog connect = new BaseDialog("@message.type-password.title");
                                    String[] password = { "" };

                                    connect.cont.table(table -> {
                                        table.add("@message.password")
                                                .padRight(5f)
                                                .right();

                                        table.field(password[0], text -> password[0] = text)
                                                .size(320f, 54f)
                                                .valid(t -> t.length() > 0 && t.length() <= 100)
                                                .maxTextLength(100)
                                                .left()
                                                .get();
                                        table.row().add();
                                    }).row();

                                    connect.buttons.button("@cancel", () -> {
                                        connect.hide();
                                    }).minWidth(210);

                                    connect.buttons.button("@ok", () -> {
                                        try {
                                            PlayerConnect.joinRoom(
                                                    PlayerConnectLink.fromString(room.link()),
                                                    password[0],
                                                    () -> {
                                                        hide();
                                                        connect.hide();
                                                    });
                                        } catch (Throwable e) {
                                            hide();
                                            connect.hide();
                                            setupPlayerConnect();
                                            Vars.ui.showException("@message.connect.fail", e);
                                        }
                                    }).minWidth(210);

                                    connect.show();
                                })
                                        .minWidth(150)
                                        .padLeft(8);
                            });
                        })
                                .growX()
                                .left()
                                .top()
                                .padTop(8);

                        list.row();
                    }
                })
                        .top()
                        .left()
                        .fill();
            })
                    .top()
                    .left()
                    .fill()
                    .scrollX(false)
                    .scrollY(true);

            roomList
                    .top()
                    .left()
                    .marginTop(8)
                    .marginBottom(8);

        });
    }
}
