package main;

import arc.*;
import main.ui.SchematicDialog;
import mindustry.Vars;
import mindustry.game.EventType.*;
import mindustry.gen.Icon;
import mindustry.mod.*;

public class Main extends Mod {

    public Main() {
        Events.on(ClientLoadEvent.class, e -> {
            SchematicDialog schematicDialog = new SchematicDialog();
            Vars.ui.menufrag.addButton("Schematics", Icon.infoCircle, () -> schematicDialog.show());
        });
    }

    @Override
    public void loadContent() {

    }
}
