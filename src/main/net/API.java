package main.net;

import arc.struct.ObjectMap;
import arc.util.serialization.Base64Coder;
import mindustry.game.Schematic;
import mindustry.game.Schematics;

public class API {

    public static ObjectMap<String, Schematic> schematicData = new ObjectMap<>();

    public static synchronized Schematic readSchematic(String data) {
        return schematicData.get(data, () -> Schematics.readBase64(new String(Base64Coder.decode(data))));
    }
}
