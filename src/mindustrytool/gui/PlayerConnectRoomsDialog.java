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
import mindustrytool.config.Debouncer;
import mindustrytool.net.Api;
import mindustrytool.playerconnect.PlayerConnect;
import mindustrytool.playerconnect.PlayerConnectLink;

public class PlayerConnectRoomsDialog extends mindustry.ui.dialogs.BaseDialog {
    private final Table playerConnect = new Table();
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
                container.add(playerConnect);
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

            setupPlayerConnect();
        } catch (Throwable e) {
            Log.err(e);
        }
    }

    public void setupPlayerConnect() {
        playerConnect.clear();
        playerConnect.labelWrap(Core.bundle.format("message.loading"))
                .center()
                .labelAlign(0)
                .expand()
                .fill();

        Api.findPlayerConnectRooms(searchTerm, rooms -> {
            playerConnect.clear();
            playerConnect.fill()
                    .top()
                    .left();

            playerConnect.pane(table -> {
                if (rooms.isEmpty()) {
                    table.labelWrap(Core.bundle.format("message.no-rooms-found"))
                            .center()
                            .labelAlign(0)
                            .expand()
                            .fill();
                    return;
                }

                for (var room : rooms) {
                    table.button(builder -> {
                        builder.add(
                                room.data().name() + " []" + (room.data().isSecured() ? Iconc.lock : ""))
                                .fontScale(1.5f)
                                .align(Align.left)
                                .left();

                        builder.row();
                        builder.add(Iconc.map + " " + Core.bundle.format("save.map", room.data().mapName())
                                + "[lightgray] / " + room.data().gamemode())
                                .align(Align.left).left();

                        builder.row();
                        builder.add(Iconc.players + " " + Core.bundle.format("players", room.data().players().size))
                                .align(Align.left)
                                .left();

                        if (room.data().mods().size > 0) {
                            builder.row();
                            builder.add(Iconc.book + " " + Strings.join(",", room.data().mods())).align(Align.left)
                                    .left();
                        }

                    },
                            () -> {
                                try {
                                    PlayerConnect.joinRoom(PlayerConnectLink.fromString(room.link()), () -> hide());
                                } catch (Throwable e) {
                                    hide();
                                    setupPlayerConnect();
                                    Vars.ui.showException("@message.connect.fail", e);
                                }
                            })
                            .growX()
                            .left()
                            .top();

                    table.row();
                }
            })
                    .top()
                    .left()
                    .fill()
                    .expand()
                    .scrollX(false)
                    .scrollY(true);
        });
    }
}
