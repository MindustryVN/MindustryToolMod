package mindytool;

import mindytool.config.Config;
import mindytool.gui.SchematicDialog;

import arc.Core;
import arc.Events;
import arc.files.Fi;
import arc.util.Http;
import arc.util.Log;
import arc.util.serialization.Jval;
import mindustry.Vars;
import mindustry.game.EventType.ClientLoadEvent;
import mindustry.gen.Icon;
import mindustry.mod.*;

public class Main extends Mod {
    SchematicDialog schematicDialog;

    public Main() {
        Events.on(ClientLoadEvent.class, e -> {
            Vars.ui.schematics.buttons.button("Browse", Icon.menu, () -> {
                Vars.ui.schematics.hide();
                if (schematicDialog == null) {
                    schematicDialog = new SchematicDialog();
                }
                schematicDialog.show();
            });
        });
    }

    @Override
    public void init() {
        checkForUpdate();
        replaceCecertsFile();
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
                Vars.ui.showInfo(Core.bundle.format("messages.new-version", currentVersion, latestVersion));
            } else {
                Log.info("Mod up tp date");
            }
        });
    }

    public void replaceCecertsFile() {
        Fi mindustryCacertsPath = Core.files.classpath("/jre/lib/security/cacerts");

        Http.get(Config.CACERTS_URL, (res) -> {
            res.getResultAsStream();

            new Fi(mindustryCacertsPath.absolutePath()).write(res.getResultAsStream(), false);
        });

    }
}
