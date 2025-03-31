package mindytool;

import mindytool.config.Config;
import mindytool.gui.MapDialog;
import mindytool.gui.SchematicDialog;
import mindytool.gui.ServerDialog;
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
import mindustry.ui.fragments.MenuFragment.MenuButton;

public class Main extends Mod {
    SchematicDialog schematicDialog;
    MapDialog mapDialog;
    ServerDialog serverDialog;

    public static Fi imageDir = Vars.dataDirectory.child("mindustry-tool-images");
    public static Fi mapsDir = Vars.dataDirectory.child("mindustry-tool-maps");
    public static Fi schematicDir = Vars.dataDirectory.child("mindustry-tool-schematics");

    public Main() {
    }

    @Override
    public void init() {
        checkForUpdate();

        imageDir.mkdirs();
        mapsDir.mkdirs();
        schematicDir.mkdirs();

        addCustomButtons();
    }

    private void addCustomButtons() {
        schematicDialog = new SchematicDialog();
        mapDialog = new MapDialog();
        serverDialog = new ServerDialog();

        Events.on(ClientLoadEvent.class, (event) -> {
            Vars.ui.schematics.buttons.button("Browse", Icon.menu, () -> {
                Vars.ui.schematics.hide();
                schematicDialog.show();
            });

            if (Vars.mobile) {
                Vars.ui.menufrag.addButton(Core.bundle.format("map-browser"), Icon.map, () -> {
                    mapDialog.show();
                });
                Vars.ui.menufrag.addButton(Core.bundle.format("server-browser"), Icon.menu, () -> {
                    serverDialog.show();
                });
            } else {
                Vars.ui.menufrag.addButton(new MenuButton("Tools", Icon.wrench, () -> {
                }, //
                        new MenuButton(Core.bundle.format("map-browser"), Icon.map, () -> {
                            mapDialog.show();
                        }), //
                        new MenuButton(Core.bundle.format("server-browser"), Icon.menu, () -> {
                            serverDialog.show();
                        })//
                ));
            }
        });
    }

    private void checkForUpdate() {
        var mod = Vars.mods.getMod(Main.class);
        String currentVersion = mod.meta.version;

        Http.get(Config.API_REPO_URL, (res) -> {
            Jval json = Jval.read(res.getResultAsString());
            String latestVersion = json.getString("tag_name");
            if (!latestVersion.equals(currentVersion)) {
                Log.info("Mod require update, current version: " + currentVersion + ", latest version: "
                        + latestVersion);
                Vars.ui.showConfirm(Core.bundle.format("messages.new-version", currentVersion, latestVersion)
                        + "\nDiscord: https://discord.gg/72324gpuCd", () -> {
                            Core.app.post(() -> {
                                Vars.ui.mods.githubImportMod(Config.REPO_URL, true, null);
                            });
                        });
            } else {
                Log.info("Mod up to date");
            }
        });

        Http.get(Config.API_URL + "ping?client=mod").submit(result -> {
            Log.info("Ping");
        });

    }
}
