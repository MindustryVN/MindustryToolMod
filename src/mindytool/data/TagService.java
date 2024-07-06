package mindytool.data;

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
    private static TagGroup group = new TagGroup();

    public static void getTag(Cons<TagGroup> listener) {
        if (group.schematic.isEmpty()) {
            getTagData((tags) -> {
                group = tags;
                listener.get(tags);
            });
        } else {
            listener.get(group);
        }
    }

    private static void getTagData(Cons<TagGroup> listener) {
        Http.get(Config.API_URL + "tags").timeout(1200000)
                .error(error -> handleError(listener, error, Config.API_URL + "tags"))
                .submit(response -> handleResult(response, listener));
    }

    public static void handleError(Cons<TagGroup> listener, Throwable error, String url) {
        Log.err(url, error);
        listener.get(group);
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
