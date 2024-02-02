package main.net;

import java.util.function.Consumer;

import arc.Core;
import arc.util.Http;
import arc.util.Log;
import main.config.Config;

public class API {

    public static void getSchematicData(String id, Consumer<String> consumer) {
        Core.app.post(() -> Http.get(Config.API_URL + String.format("schematics/%s/data", id))//
                .timeout(120000)//
                .error(error -> Log.err(error))
                .submit(result -> consumer.accept(result.getResultAsString())));
    }
}
