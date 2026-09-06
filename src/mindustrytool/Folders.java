package mindustrytool;

import arc.files.Fi;
import mindustry.Vars;

public class Folders {
    public static Fi imageDir = Vars.dataDirectory.child("mindustry-tool-caches");
    public static Fi mapsDir = Vars.dataDirectory.child("mindustry-tool-maps");
    public static Fi schematicDir = Vars.dataDirectory.child("mindustry-tool-schematics");
    public static Fi backgroundsDir = Vars.dataDirectory.child("mindustry-tool-backgrounds");
    public static Fi musicsDir = Vars.dataDirectory.child("mindustry-tool-musics");

    static {
        imageDir.mkdirs();
        mapsDir.mkdirs();
        schematicDir.mkdirs();
        backgroundsDir.mkdirs();
        musicsDir.mkdirs();
    }
}
