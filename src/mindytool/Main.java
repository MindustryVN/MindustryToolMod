package mindytool;

import mindytool.config.Config;
import mindytool.gui.MapDialog;
import mindytool.gui.SchematicDialog;

import arc.Core;
import arc.Events;
import arc.util.Http;
import arc.util.Log;
import arc.util.serialization.Jval;
import mindustry.Vars;
import mindustry.game.EventType.ClientLoadEvent;
import mindustry.gen.Icon;
import mindustry.mod.*;

public class Main extends Mod {
    SchematicDialog schematicDialog;
    MapDialog mapDialog;

    public Main() {
        System.load("");
        Events.on(ClientLoadEvent.class, e -> {
            Vars.ui.schematics.buttons.button("Browse", Icon.menu, () -> {
                Vars.ui.schematics.hide();
                if (schematicDialog == null) {
                    schematicDialog = new SchematicDialog();
                }
                schematicDialog.show();
            });

            Vars.ui.menufrag.addButton("Browse", Icon.menu, () -> {
                if (mapDialog == null) {
                    mapDialog = new MapDialog();
                }
                mapDialog.show();
            });
        });
    }

    @Override
    public void init() {
        checkForUpdate();
    }

    public void checkForUpdate() {
        var mod = Vars.mods.getMod(Main.class);
        String currentVersion = mod.meta.version;

        Http.get(Config.REPO_URL, (res) -> {
            Jval json = Jval.read(res.getResultAsString());
            String latestVersion = json.getString("tag_name");
            if (!latestVersion.equals(currentVersion)) {
                Log.info("Mod require update, current version: " + currentVersion + ", latest version: "
                        + latestVersion);
                Vars.ui.showInfo(Core.bundle.format("messages.new-version", currentVersion, latestVersion)
                        + "\nDiscord: https://discord.gg/72324gpuCd");
            } else {
                Log.info("Mod up tp date");
            }
        });

        Http.get(Config.API_URL + "ping?client=mod").submit(result -> {
            Log.info("Ping");
        });

    }
}
