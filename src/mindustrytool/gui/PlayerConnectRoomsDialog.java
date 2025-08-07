package mindustrytool.gui;

import java.util.concurrent.TimeUnit;

import arc.scene.ui.TextField;
import arc.scene.ui.layout.Table;
import arc.util.Log;
import arc.util.Strings;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.gen.Iconc;
import mindustrytool.config.Debouncer;
import mindustrytool.net.Api;
import mindustrytool.playerconnect.PlayerConnect;
import mindustrytool.playerconnect.PlayerConnectLink;

public class PlayerConnectRoomsDialog extends mindustry.ui.dialogs.BaseDialog {
    private final Table playerConnect = new Table();
    private final Debouncer debouncer = new Debouncer(500, TimeUnit.MILLISECONDS);
    private TextField searchField;
    private String searchTerm = "";

    public PlayerConnectRoomsDialog() {
        super("@message.room-list.title");
        addCloseButton();

        cont.defaults().width(Vars.mobile ? 350f : 550f);

        try {
            searchField = cont.field(searchTerm, (result) -> {
                searchTerm = result;
                debouncer.debounce(this::setupPlayerConnect);
            })//
                    .growX()//
                    .get();

            searchField.setMessageText("@map.search");

            cont.row();

            cont.table(container -> container.add(playerConnect))
                    .fill()
                    .expand();

            setupPlayerConnect();

            if (!Vars.steam && !Vars.mobile) {
                Vars.ui.join.buttons.button("@message.room-list.title", mindustry.gen.Icon.play, this::show).row();

                Vars.ui.join.buttons.getCells()
                        .swap(Vars.ui.join.buttons.getCells().size - 1/* 6 */, 4);
            } else {
                Vars.ui.join.buttons.row().add().growX().width(-1);
                Vars.ui.join.buttons.button("@message.room-list.title", mindustry.gen.Icon.play, this::show).row();
            }
        } catch (Throwable e) {
            Log.err(e);
        }
    }

    public void setupPlayerConnect() {
        playerConnect.clear();

        Api.findPlayerConnectRooms(searchTerm, rooms -> {
            playerConnect.clear();
            playerConnect.table(table -> {
                table.button(Icon.refresh, () -> setupPlayerConnect()).size(40);
                table.row();

                for (var room : rooms) {
                    table.button(builder -> {
                        builder.add(room.data().name() + (room.data().isSecured() ? Iconc.lock : Iconc.lockOpen))
                                .fontScale(1.5f);
                        builder.row();
                        builder.add(Iconc.map + " " + room.data().mapName());
                        builder.row();
                        builder.add(Iconc.players + " " + String.valueOf(room.data().players().size));
                        builder.row();
                        builder.add(Iconc.play + " " + room.data().gamemode());

                        if (room.data().mods().size > 0) {
                            builder.row();
                            builder.add(Iconc.book + " " + Strings.join(",", room.data().mods()));
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
                            });
                    table.row();
                }
            });
        });
    }
}
