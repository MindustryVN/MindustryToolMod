package main.data;

import arc.Core;
import arc.func.Cons;
import arc.struct.Seq;
import arc.util.Http;
import arc.util.Log;
import arc.util.Http.HttpResponse;
import main.config.Config;
import mindustry.io.JsonIO;

public class Tag {
    public String name;
    public String[] value;
    public String color;

    private static Runnable onUpdate = () -> {
    };
    private static Seq<Tag> schematic = new Seq<>();

    public static void schematic(Cons<Seq<Tag>> listener) {
        if (schematic.isEmpty()) {
            getTag(TagName.schematic, (tags) -> {
                schematic = tags;
                listener.get(tags);
            });
        } else {
            listener.get(schematic);
        }
    }

    private static void getTag(TagName tag, Cons<Seq<Tag>> listener) {
        Http.get(Config.API_URL + "tag/" + tag.value())
                .timeout(1200000)
                .error(error -> handleError(listener, error))
                .submit(response -> handleResult(response, listener));
    }

    public static void handleError(Cons<Seq<Tag>> listener, Throwable error) {
        Log.err(error);
        listener.get(new Seq<>());
    }

    @SuppressWarnings("unchecked")
    private static void handleResult(HttpResponse response, Cons<Seq<Tag>> listener) {
        String data = response.getResultAsString();
        Core.app.post(() -> {
            var tags = JsonIO.json.fromJson(Seq.class, Tag.class, data);
            listener.get(tags);
            onUpdate.run();
        });
    }

    public static void onUpdate(Runnable callback) {
        onUpdate = callback;
    }
}
