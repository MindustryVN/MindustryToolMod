package mindustrytool.gui;

import arc.scene.ui.layout.Table;
import arc.util.Reflect;
import mindustry.Vars;
import mindustry.ui.dialogs.JoinDialog;

public class PlayerConnectDialog {
    Table playerConnectServers = new Table();

    public PlayerConnectDialog() {
        Reflect.invoke(JoinDialog.class, Vars.ui.join, "section", new Object[] {
                "@message.servers.player-connect.title", playerConnectServers, true
        }, String.class, Table.class, Boolean.class);

        
    }
}
