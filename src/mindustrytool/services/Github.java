package mindustrytool.services;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import mindustrytool.Config;
import mindustrytool.models.response.TaskResponse;
import mindustrytool.utils.JsonUtils;

public final class Github {

    private static final Request githubApi = Request.builder()
            .baseUrl(Config.GITHUB_API_URL)
            .timeout(Duration.ofSeconds(10))
            .build();

    private static final Request projectApi = Request.builder()
            .baseUrl(Config.PROJECT_URL)
            .timeout(Duration.ofSeconds(10))
            .build();

    private static final Request rawApi = Request.builder()
            .timeout(Duration.ofSeconds(10))
            .build();

    private Github() {
    }

    // ─── Mod metadata ──────────────────────────────────────────────

    /**
     * Raw mod.hjson is not a JSON object mapping to a DTO; keep as String.
     */
    public static CompletableFuture<String> getModHjson() {
        return rawApi.get(Config.MOD_HJSON_URL)
                .sendAsync()
                .thenApply(r -> r.body());
    }

    // ─── Releases ──────────────────────────────────────────────────

    /**
     * GitHub releases return a heterogeneous JSON array; callers parse via Jval/JsonUtils.
     * Kept as String to avoid coupling to GitHub schema; use JsonUtils if typed parsing needed.
     */
    public static CompletableFuture<String> getReleases() {
        return githubApi.get("")
                .sendAsync()
                .thenApply(r -> r.body());
    }

    public static CompletableFuture<String> getReleases(int page, int perPage) {
        return rawApi.get(Config.GITHUB_API_URL + "?page=" + page + "&per_page=" + perPage)
                .sendAsync()
                .thenApply(r -> r.body());
    }

    // ─── Project tasks ─────────────────────────────────────────────

    public static CompletableFuture<TaskResponse> getProjectTasks(String status) {
        return projectApi.get("/api/v1/projects/" + Config.PROJECT_ID + "/tasks?status=" + status)
                .timeout(Duration.ofMillis(20_000))
                .sendAsync()
                .thenApply(r -> JsonUtils.fromJson(TaskResponse.class, r.body()));
    }
}
