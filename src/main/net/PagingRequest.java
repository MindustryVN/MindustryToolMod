package main.net;

import java.net.URI;

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

    private volatile boolean isLoading = false;
    private boolean hasMore = true;
    private boolean isError = false;
    private String error = "";

    private int itemPerPage = 20;

    private int page = 0;

    private final String url;
    private final Class<T> clazz;

    private ObjectMap<String, String> options = new ObjectMap<>();

    public PagingRequest(Class<T> clazz, String url) {
        this.url = url;
        this.clazz = clazz;
    }

    public synchronized void getPage(Cons<Seq<T>> listener) {
        if (isLoading)
            return;

        isError = false;
        isLoading = true;

        try {
            URIBuilder builder = new URIBuilder(url)
                    .setParameter("page", String.valueOf(page))
                    .setParameter("items", String.valueOf(itemPerPage));

            options.forEach(entry -> {
                builder.setParameter(entry.key, entry.value);
            });

            URI uri = builder.build();

            listener.get(null);
            Http.get(uri.toString())
                    .timeout(1200000)
                    .error(error -> handleError(listener, error, uri.toString()))
                    .submit(response -> handleResult(response, itemPerPage, listener));
        } catch (Exception e) {
            handleError(listener, e, url);
        }
    }

    public synchronized void handleError(Cons<Seq<T>> listener, Throwable e, String url) {
        Log.err(url, e);
        error = e.getMessage();

        isLoading = false;
        isError = true;

        listener.get(null);
    }

    public synchronized void setPage(int page) {
        this.page = page;
    }

    public synchronized void setOptions(ObjectMap<String, String> options) {
        this.options = options;
    }

    public synchronized int getItemPerPage() {
        return itemPerPage;
    }

    public synchronized void setItemPerPage(int itemPerPage) {
        this.itemPerPage = itemPerPage;
    }

    public synchronized boolean hasMore() {
        return hasMore;
    }

    public synchronized boolean isLoading() {
        return isLoading;
    }

    public synchronized boolean isError() {
        return isError;
    }

    public synchronized String getError() {
        return error;
    }

    public synchronized int getPage() {
        return page;
    }

    public synchronized void nextPage(Cons<Seq<T>> listener) {
        if (isLoading)
            return;

        if (hasMore) {
            page++;
        }

        getPage(listener);
    }

    public synchronized void previousPage(Cons<Seq<T>> listener) {
        if (isLoading)
            return;

        if (page > 0) {
            page--;
        }

        getPage(listener);
    }

    @SuppressWarnings("unchecked")
    private synchronized void handleResult(HttpResponse response, int itemPerPage, Cons<Seq<T>> listener) {
        isLoading = false;
        isError = false;

        String data = response.getResultAsString();
        Core.app.post(() -> {
            var schems = JsonIO.json.fromJson(Seq.class, clazz, data);

            hasMore = schems.size != 0;

            listener.get(schems);
        });
    }
}
