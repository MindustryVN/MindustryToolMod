package mindustrytool;

import mindustry.Vars;
import mindustry.editor.MapResizeDialog;
import mindustry.mod.Mods.LoadedMod;
import mindustry.mod.Mod;

public class Main extends Mod {
    public static LoadedMod self;

    public Main() {
        Vars.maxSchematicSize = 4000;
        MapResizeDialog.maxSize = 4000;
    }

    @Override
    public void init() {
        self = Vars.mods.getMod(Main.class);

        if (self == null) {
            Vars.ui.showErrorMessage("Mod cant find itself, please contact admin on Discord to fix the problem");
            return;
        }
    }
}
