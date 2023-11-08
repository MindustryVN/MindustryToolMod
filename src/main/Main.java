package main;

import arc.Events;
import arc.scene.ui.Dialog;
import main.config.Config;
import main.gui.SchematicDialog;
import mindustry.Vars;
import mindustry.game.EventType.ClientLoadEvent;
import mindustry.gen.Icon;
import mindustry.mod.*;

public class Main extends Mod {

    public Main() {
        Events.on(ClientLoadEvent.class, e -> {
            if (Config.DEV == false || Vars.android == true) {
                SchematicDialog schematicDialog = new SchematicDialog();
                Vars.ui.schematics.buttons.button("Browse", Icon.menu, () -> schematicDialog.show());
            } else {
                Dialog dialog = new Dialog();
                dialog.add(
                        "Desktop and IOS is not support because of some technical reason, please turn off the mod and use the website (Mindustry tool)");
                dialog.addCloseButton();
                dialog.closeOnBack();
                dialog.fill();
                dialog.show();
            }
        });
    }
}
