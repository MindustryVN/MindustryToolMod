package mindustrytool.gui;

import arc.scene.ui.layout.Table;
import arc.scene.utils.Elem;
import arc.util.Reflect;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.ui.dialogs.JoinDialog;
import mindustrytool.net.Api;
import mindustrytool.playerconnect.PlayerConnect;
import mindustrytool.playerconnect.PlayerConnectLink;

public class PlayerConnectDialog {
    Table playerConnect = new Table();

    public PlayerConnectDialog() {
        Reflect.invoke(JoinDialog.class, Vars.ui.join, "section", new Object[] {
                "@message.servers.player-connect.title", playerConnect, true
        }, String.class, Table.class, Boolean.class);

        Table hosts = Reflect.get(JoinDialog.class, Vars.ui.join, "hosts");

        hosts.addChildAt(0, Elem.newImageButton(Icon.refresh, () -> setupPlayerConnect()));
        hosts.addChildAt(0, playerConnect);

        setupPlayerConnect();
    }

    public void setupPlayerConnect() {
        playerConnect.clear();

        Api.findPlayerConnectRooms(rooms -> {
            playerConnect.clear();
            for (var room : rooms) {
                playerConnect.button(room.roomId(),
                        () -> PlayerConnect.joinRoom(PlayerConnectLink.fromString(room.address()), () -> {
                        }));
            }
        });

    }
}
