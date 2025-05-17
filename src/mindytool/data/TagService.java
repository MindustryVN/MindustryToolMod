package mindytool.data;

import java.util.HashMap;

import arc.Core;
import arc.func.Cons;
import arc.util.Http;
import arc.util.Http.HttpResponse;
import arc.util.Log;
import mindustry.io.JsonIO;
import mindytool.config.Config;

public class TagService {

    private static Runnable onUpdate = () -> {
    };
    private static HashMap<String, TagGroup> group = new HashMap<>();

    private static String modId = "";

    public static void setModId(String id) {
        modId = id == null ? "" : id;
        onUpdate.run();
    }

    public static void getTag(Cons<TagGroup> listener) {
        var item = group.get(modId);
        if (item != null) {
            listener.get(item);
            return;
        }

        getTagData((tags) -> {
            group.put(modId, tags);
            listener.get(tags);
        });

    }

    private static void getTagData(Cons<TagGroup> listener) {
        Http.get(Config.API_URL + "tags" + (modId != null && !modId.isBlank() ? "?modId=" + modId : ""))
                .error(error -> handleError(listener, error, Config.API_URL + "tags"))
                .submit(response -> handleResult(response, listener));
    }

    public static void handleError(Cons<TagGroup> listener, Throwable error, String url) {
        Log.err(url, error);
        listener.get(new TagGroup());
    }

    private static void handleResult(HttpResponse response, Cons<TagGroup> listener) {
        String data = response.getResultAsString();
        Core.app.post(() -> {
            var tags = JsonIO.json.fromJson(TagGroup.class, data);
            listener.get(tags);
            onUpdate.run();
        });
    }

    public static void onUpdate(Runnable callback) {
        onUpdate = callback;
    }
}
