package mindustrytool.services;

import java.util.concurrent.CompletableFuture;

import mindustrytool.Config;
import mindustrytool.models.TaskResponse;
import mindustrytool.utils.JsonUtils;

import static mindustrytool.services.Request.*;

public final class Github {

    private Github() {
    }

    // ─── Mod metadata ──────────────────────────────────────────────

    /**
     * Raw mod.hjson is not a JSON object mapping to a DTO; keep as String.
     */
    public static CompletableFuture<String> getModHjson() {
        return get(Config.MOD_HJSON_URL)
                .thenApply(r -> r.body());
    }

    // ─── Releases ──────────────────────────────────────────────────

    /**
     * GitHub releases return a heterogeneous JSON array; callers parse via Jval/JsonUtils.
     * Kept as String to avoid coupling to GitHub schema; use JsonUtils if typed parsing needed.
     */
    public static CompletableFuture<String> getReleases() {
        return get(Config.GITHUB_API_URL)
                .thenApply(r -> r.body());
    }

    public static CompletableFuture<String> getReleases(int page, int perPage) {
        return get(Config.GITHUB_API_URL + "?page=" + page + "&per_page=" + perPage)
                .thenApply(r -> r.body());
    }

    // ─── Project tasks ─────────────────────────────────────────────

    public static CompletableFuture<TaskResponse> getProjectTasks(String status) {
        return get(Config.PROJECT_URL + "/api/v1/projects/" + Config.PROJECT_ID + "/tasks?status=" + status, 20_000)
                .thenApply(r -> JsonUtils.fromJson(TaskResponse.class, r.body()));
    }
}
