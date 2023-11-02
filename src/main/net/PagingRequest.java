package main.net;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.utils.URIBuilder;

import arc.Core;
import arc.func.Cons;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Http;
import arc.util.Log;
import arc.util.Http.HttpResponse;
import mindustry.io.JsonIO;

public class PagingRequest<T> {

    private boolean hasMore = true;
    private boolean isLoading = true;
    private boolean isError = false;

    private int itemPerPage = 30;

    private int page = 0;

    private final String url;
    private final Class<T> clazz;

    private ObjectMap<String, String> options = new ObjectMap<>();

    public PagingRequest(Class<T> clazz, String url) {
        this.url = url;
        this.clazz = clazz;
    }

    public void getPage(Cons<Seq<T>> listener) {

        isError = false;
        isLoading = true;

        try {
            URIBuilder builder = new URIBuilder(url)
                    .setParameter("page", String.valueOf(page))
                    .setParameter("items", String.valueOf(itemPerPage));

            options.forEach(entry -> builder.setParameter(entry.key, entry.value));

            URI uri = builder.build();

            listener.get(null);
            Http.get(uri.toString())
                    .timeout(1200000)
                    .error(error -> {
                        isLoading = false;
                        isError = true;
                        listener.get(null);
                    })
                    .submit(response -> handleResult(response,itemPerPage, listener));
        } catch (URISyntaxException e) {
            Log.err(e);

            isLoading = false;
            isError = true;
        }
    }

    public void setOptions(ObjectMap<String, String> options) {
        this.options = options;
    }

    public int getItemPerPage() {
        return itemPerPage;
    }

    public void setItemPerPage(int itemPerPage) {
        this.itemPerPage = itemPerPage;
    }

    public boolean hasMore() {
        return hasMore;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public boolean isError() {
        return isError;
    }

    public int getPage() {
        return page;
    }

    public void nextPage(Cons<Seq<T>> listener) {
        if (isLoading)
            return;

        if (hasMore) {
            page++;
        }

        getPage(listener);
    }

    public void previousPage(Cons<Seq<T>> listener) {
        if (isLoading)
            return;

        if (page > 0) {
            page--;
        }

        getPage(listener);
    }

    @SuppressWarnings("unchecked")
    private void handleResult(HttpResponse response,int itemPerPage, Cons<Seq<T>> listener) {
        isLoading = false;
        isError = false;

        String data = response.getResultAsString();
        Core.app.post(() -> {
            var schems = JsonIO.json.fromJson(Seq.class, clazz, data);

            if (schems.size == itemPerPage)
                hasMore = true;
            else
                hasMore = false;

            listener.get(schems);
        });
    }
}
