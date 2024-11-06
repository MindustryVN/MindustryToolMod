package mindytool.config;

import arc.struct.ObjectMap;
import mindustry.game.Schematic;
import mindustry.game.Schematics;

public class Utils {

    public static ObjectMap<String, Schematic> schematicData = new ObjectMap<>();

    public static synchronized Schematic readSchematic(String data) {
        return schematicData.get(data, () -> Schematics.readBase64(data));
    }
}
