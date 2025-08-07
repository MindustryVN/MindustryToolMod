package mindustrytool.data;

import java.util.HashMap;

import arc.Core;
import arc.func.Cons;
import arc.util.Http;
import arc.util.Http.HttpResponse;
import arc.util.Log;
import mindustry.io.JsonIO;
import mindustrytool.config.Config;

public class TagService {

    private Runnable onUpdate = () -> {
    };
    private static HashMap<String, TagGroup> group = new HashMap<>();

    private String modId = "";

    public void setModId(String id) {
        modId = id == null ? "" : id;
        Core.app.post(onUpdate);
    }

    public void getTag(Cons<TagGroup> listener) {
        var item = group.get(modId);
        if (item != null) {
            Core.app.post(() -> listener.get(item));
            return;
        }

        getTagData((tags) -> {
            group.put(modId, tags);
            Core.app.post(() -> listener.get(tags));
        });

    }

    private void getTagData(Cons<TagGroup> listener) {
        Http.get(Config.API_URL + "tags" + (modId != null && !modId.isEmpty() ? "?modId=" + modId : ""))
                .error(error -> handleError(listener, error, Config.API_URL + "tags"))
                .submit(response -> handleResult(response, listener));
    }

    public void handleError(Cons<TagGroup> listener, Throwable error, String url) {
        Log.err(url, error);
        Core.app.post(() -> listener.get(new TagGroup()));
    }

    private void handleResult(HttpResponse response, Cons<TagGroup> listener) {
        String data = response.getResultAsString();
        var tags = JsonIO.json.fromJson(TagGroup.class, data);
        Core.app.post(() -> {
            listener.get(tags);
            onUpdate.run();
        });
    }

    public void onUpdate(Runnable callback) {
        onUpdate = callback;
    }
}
