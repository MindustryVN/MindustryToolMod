package mindustrytool.gui;

import arc.scene.ui.layout.Table;
import arc.util.Log;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustrytool.net.Api;
import mindustrytool.playerconnect.PlayerConnect;
import mindustrytool.playerconnect.PlayerConnectLink;

public class PlayerConnectRoomsDialog extends mindustry.ui.dialogs.BaseDialog {
    Table playerConnect = new Table();

    public PlayerConnectRoomsDialog() {
        super("@message.room-list.title");

        cont.defaults().width(Vars.mobile ? 350f : 550f);

        try {
            cont.table(container -> container.add(playerConnect));
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

        Api.findPlayerConnectRooms(rooms -> {
            playerConnect.clear();
            playerConnect.table(table -> {
                table.button(Icon.refresh, () -> setupPlayerConnect()).size(40);
                table.row();

                for (var room : rooms) {
                    table.button(room.roomId(),
                            () -> PlayerConnect.joinRoom(PlayerConnectLink.fromString(room.address()), () -> {
                            }));
                    table.row();
                }
            });
        });

    }
}
