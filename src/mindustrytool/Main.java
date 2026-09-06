package mindustrytool;

import arc.Core;
import arc.Events;
import mindustry.Vars;
import mindustry.editor.MapResizeDialog;
import mindustry.game.EventType.ClientLoadEvent;
import mindustry.mod.Mods.LoadedMod;
import mindustrytool.components.FileIcon;
import mindustrytool.services.PacketReplacer;
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

        Events.on(ClientLoadEvent.class, event -> {

            PacketReplacer.replace();
        });
    }

    private void registerMindustryToolButton() {
        Core.app.post(() -> {
            try {
                Vars.ui.menufrag.addButton("Mindustry Tool", FileIcon.of("mod.png"), () -> featureSettingDialog.show());
            } catch (Exception err) {
                Vars.ui.showException(err);
            }
        });
    }
}
