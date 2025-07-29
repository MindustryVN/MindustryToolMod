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
