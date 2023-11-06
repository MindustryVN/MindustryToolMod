package main;

import arc.Events;
import main.gui.SchematicDialog;
import mindustry.Vars;
import mindustry.game.EventType.ClientLoadEvent;
import mindustry.gen.Icon;
import mindustry.mod.*;

public class Main extends Mod {

    public Main() {
        Events.on(ClientLoadEvent.class, e -> {
            SchematicDialog schematicDialog = new SchematicDialog();
            Vars.ui.schematics.buttons.button("Browse", Icon.menu, () -> schematicDialog.show());
        });
    }
}
