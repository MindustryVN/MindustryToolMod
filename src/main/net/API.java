package main.net;

import java.util.function.Consumer;

import arc.Core;
import arc.struct.ObjectMap;
import arc.util.Http;
import arc.util.Log;
import main.config.Config;
import mindustry.game.Schematic;
import mindustry.game.Schematics;

public class API {

    public static ObjectMap<String, Schematic> schematicData = new ObjectMap<>();

    public static void getSchematicData(String id, Consumer<Schematic> consumer) {
        if (schematicData.containsKey(id)) {
            consumer.accept(schematicData.get(id));
            return;
        }

        Core.app.post(() -> Http.get(Config.API_URL + String.format("schematics/%s/data", id))//
                .timeout(120000)//
                .error(error -> Log.err(error))
                .submit(result -> {
                    String data = result.getResultAsString();
                    var schematic = readSchematic(data);
                    schematicData.put(id, schematic);
                    consumer.accept(schematic);
                }));
    }

    private static synchronized Schematic readSchematic(String data){
        return Schematics.readBase64(data);
    }
}
