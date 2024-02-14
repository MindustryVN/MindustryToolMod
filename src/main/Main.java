package main;

import arc.Events;
import main.gui.SchematicDialog;
import mindustry.Vars;
import mindustry.game.EventType.ClientLoadEvent;
import mindustry.gen.Icon;
import mindustry.mod.*;

public class Main extends Mod {
    SchematicDialog schematicDialog;

    public Main() {
        Events.on(ClientLoadEvent.class, e -> {
            Vars.ui.schematics.buttons.button("Browse", Icon.menu, () -> {
                if (schematicDialog == null) {
                    schematicDialog = new SchematicDialog();
                }
                schematicDialog.show();
            });
        });
    }
}
