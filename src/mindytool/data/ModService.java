package mindytool.data;

import arc.Core;
import arc.func.Cons;
import arc.struct.Seq;
import arc.util.Http;
import arc.util.Http.HttpResponse;
import arc.util.Log;
import mindustry.io.JsonIO;
import mindytool.config.Config;

public class ModService {

    private static Runnable onUpdate = () -> {
    };
    private static Seq<ModData> mods = new Seq<>();

    public static void getMod(Cons<Seq<ModData>> listener) {
        if (mods.isEmpty()) {
            getModData((modsData) -> {
                mods = modsData;
                listener.get(mods);
            });
        } else {
            listener.get(mods);
        }
    }

    private static void getModData(Cons<Seq<ModData>> listener) {
        Http.get(Config.API_URL + "mods").timeout(1200000)
                .error(error -> handleError(listener, error, Config.API_URL + "mods"))
                .submit(response -> handleResult(response, listener));
    }

    public static void handleError(Cons<Seq<ModData>> listener, Throwable error, String url) {
        Log.err(url, error);
        listener.get(mods);
    }

    private static void handleResult(HttpResponse response, Cons<Seq<ModData>> listener) {
        String data = response.getResultAsString();
        Core.app.post(() -> {
            @SuppressWarnings("unchecked")
            Seq<ModData> mods = JsonIO.json.fromJson(Seq.class, ModData.class, data);
            listener.get(mods);
            onUpdate.run();
        });
    }

    public static void onUpdate(Runnable callback) {
        onUpdate = callback;
    }

}
