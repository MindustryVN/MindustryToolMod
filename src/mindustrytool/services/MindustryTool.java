package mindustrytool.services;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;

import mindustrytool.Config;
import mindustrytool.models.ChannelDto;
import mindustrytool.models.ChatMessage;
import mindustrytool.models.ChatUser;
import mindustrytool.models.AuthTokenResponse;
import mindustrytool.models.LoginUriResponse;
import mindustrytool.models.MapData;
import mindustrytool.models.MapDetailData;
import mindustrytool.models.ModData;
import mindustrytool.models.PlayerConnectProvider;
import mindustrytool.models.PlayerConnectRoom;
import mindustrytool.models.SchematicData;
import mindustrytool.models.SchematicDetailData;
import mindustrytool.models.ServerData;
import mindustrytool.models.TagCategory;
import mindustrytool.models.UserData;
import mindustrytool.models.UserSession;
import mindustrytool.utils.JsonUtils;

import static mindustrytool.services.Request.*;

public final class MindustryTool {

    private MindustryTool() {
    }

    private static String url(String path) {
        return Config.API_URL + path;
    }
    // ─── Ping ──────────────────────────────────────────────────────

    public static CompletableFuture<Void> ping(String client) {
        return get(url("/ping?client=" + client))
                .thenAccept(r -> {});
    }

    // ─── Maps ──────────────────────────────────────────────────────

    public static CompletableFuture<byte[]> downloadMap(String itemId) {
        return getBytes(url("/maps/" + itemId + "/data"), LONG_TIMEOUT_MS)
                .thenApply(r -> r.body());
    }

    public static CompletableFuture<MapDetailData> findMap(String itemId) {
        return get(url("/maps/" + itemId))
                .thenApply(r -> JsonUtils.fromJson(MapDetailData.class, r.body()));
    }

    public static CompletableFuture<List<MapData>> searchMaps(int page, int size, String sort, String query, List<String> tags) {
        return get(buildPagedUrl(url("/maps"), page, size, sort, query, tags))
                .thenApply(r -> JsonUtils.fromJsonArray(MapData.class, r.body()));
    }

    // ─── Schematics ────────────────────────────────────────────────

    public static CompletableFuture<byte[]> downloadSchematic(String itemId) {
        return getBytes(url("/schematics/" + itemId + "/data"), LONG_TIMEOUT_MS)
                .thenApply(r -> r.body());
    }

    public static CompletableFuture<SchematicDetailData> findSchematic(String itemId) {
        return get(url("/schematics/" + itemId))
                .thenApply(r -> JsonUtils.fromJson(SchematicDetailData.class, r.body()));
    }

    public static CompletableFuture<List<SchematicData>> searchSchematics(int page, int size, String sort, String query, List<String> tags) {
        return get(buildPagedUrl(url("/schematics"), page, size, sort, query, tags))
                .thenApply(r -> JsonUtils.fromJsonArray(SchematicData.class, r.body()));
    }

    // ─── Tags ──────────────────────────────────────────────────────

    public static CompletableFuture<List<TagCategory>> getTags(String group) {
        return get(url("/tags?group=" + group))
                .thenApply(r -> JsonUtils.fromJsonArray(TagCategory.class, r.body()));
    }

    // ─── Users ─────────────────────────────────────────────────────

    public static CompletableFuture<List<UserData>> getUserBatch(List<String> ids) {
        String json = JsonUtils.toJson(java.util.Map.of("ids", ids));
        return post(url("/users/batches"), json)
                .thenApply(r -> JsonUtils.fromJsonArray(UserData.class, r.body()));
    }

    // ─── Planets ───────────────────────────────────────────────────

    public static CompletableFuture<List<ModData>> getPlanets() {
        return get(url("/planets"))
                .thenApply(r -> JsonUtils.fromJsonArray(ModData.class, r.body()));
    }

    // ─── Servers ───────────────────────────────────────────────────

    public static CompletableFuture<List<ServerData>> getServers(int page, int size) {
        return get(url("/servers?page=" + page + "&size=" + size))
                .thenApply(r -> JsonUtils.fromJsonArray(ServerData.class, r.body()));
    }

    // ─── Player Connect ────────────────────────────────────────────

    public static CompletableFuture<List<PlayerConnectRoom>> searchRooms(String query) {
        return get(url("/player-connect/rooms?q=" + query), LONG_TIMEOUT_MS)
                .thenApply(r -> JsonUtils.fromJsonArray(PlayerConnectRoom.class, r.body()));
    }

    public static CompletableFuture<List<PlayerConnectProvider>> getProviders() {
        return get(url("/player-connect/providers"), LONG_TIMEOUT_MS)
                .thenApply(r -> JsonUtils.fromJsonArray(PlayerConnectProvider.class, r.body()));
    }

    // ─── Chat ──────────────────────────────────────────────────────

    public static CompletableFuture<List<ChannelDto>> getChatChannels() {
        return authGet(url("/chats/channels"))
                .thenApply(r -> JsonUtils.fromJsonArray(ChannelDto.class, r.body()));
    }

    public static CompletableFuture<ChatMessage> sendChatMessage(String endpoint, String channelId, String content, String replyTo) {
        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("content", content);
        payload.put("channelId", channelId);
        if (replyTo != null && !replyTo.isEmpty()) {
            payload.put("replyTo", replyTo);
        }
        return authPost(url("/chats/" + endpoint), JsonUtils.toJson(payload))
                .thenApply(r -> JsonUtils.fromJson(ChatMessage.class, r.body()));
    }

    public static CompletableFuture<List<ChatUser>> getChatUsers(String channelId) {
        return authGet(url("/chats/users?channelId=" + channelId))
                .thenApply(r -> JsonUtils.fromJsonArray(ChatUser.class, r.body()));
    }

    public static CompletableFuture<Integer> getChatUserCount(String channelId) {
        return authGet(url("/chats/users/count?channelId=" + channelId))
                .thenApply(r -> Integer.parseInt(r.body().trim()));
    }

    public static CompletableFuture<List<ChatMessage>> getChatMessages(String channelId, String cursor) {
        String u = url("/chats?channelId=" + channelId);
        if (cursor != null && !cursor.isEmpty()) {
            u += "&cursor=" + cursor;
        }
        return authGet(u)
                .thenApply(r -> JsonUtils.fromJsonArray(ChatMessage.class, r.body()));
    }

    public static CompletableFuture<Void> updateChatState(String state) {
        String json = JsonUtils.toJson(java.util.Map.of("state", state));
        return authPut(url("/chats/users/state"), json)
                .thenAccept(r -> {});
    }

    public static CompletableFuture<Flow.Publisher<String>> chatStream(String chatId) {
        return authStream(url("/chats/stream"), chatId);
    }

    // ─── Auth ──────────────────────────────────────────────────────

    public static CompletableFuture<UserSession> getSession() {
        return authGet(url("/auth/session"))
                .thenApply(r -> {
                    String body = r.body();
                    if (body == null || body.isEmpty() || body.equals("null")) {
                        return null;
                    }
                    return JsonUtils.fromJson(UserSession.class, body);
                });
    }

    public static CompletableFuture<LoginUriResponse> getLoginUri() {
        return get(url("/auth/app/login-uri"))
                .thenApply(r -> JsonUtils.fromJson(LoginUriResponse.class, r.body()));
    }

    public static CompletableFuture<AuthTokenResponse> pollLoginToken(String loginId) {
        return get(url("/auth/app/login-token?loginId=" + loginId), LONG_TIMEOUT_MS)
                .thenApply(r -> JsonUtils.fromJson(AuthTokenResponse.class, r.body()));
    }

    public static CompletableFuture<Void> logout(String accessToken, String refreshToken) {
        String json = JsonUtils.toJson(java.util.Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken));
        return postWithAuth(url("/auth/app/logout"), json, accessToken)
                .thenAccept(r -> {});
    }

    public static CompletableFuture<AuthTokenResponse> refreshToken(String refreshToken) {
        String json = JsonUtils.toJson(java.util.Map.of("refreshToken", refreshToken));
        return post(url("/auth/app/refresh"), json)
                .thenApply(r -> JsonUtils.fromJson(AuthTokenResponse.class, r.body()));
    }

    // ─── Crash Report ──────────────────────────────────────────────

    public static CompletableFuture<Void> submitCrashReport(String crashData) {
        String json = JsonUtils.toJson(java.util.Map.of("content", crashData));
        return post(url("/crashes"), json)
                .thenAccept(r -> {});
    }

    // ─── Images ────────────────────────────────────────────────────

    public static CompletableFuture<byte[]> downloadSchematicImage(String itemId, boolean preview) {
        String u = url("/schematics/" + itemId + "/image.png");
        if (preview) u += "?variant=preview";
        return getBytes(u, LONG_TIMEOUT_MS)
                .thenApply(r -> r.body());
    }

    public static CompletableFuture<byte[]> downloadMapImage(String itemId, boolean preview) {
        String u = url("/maps/" + itemId + "/image.png");
        if (preview) u += "?variant=preview";
        return getBytes(u, LONG_TIMEOUT_MS)
                .thenApply(r -> r.body());
    }

    // ─── Paged search helper ───────────────────────────────────────

    private static String buildPagedUrl(String baseUrl, int page, int size, String sort, String query, List<String> tags) {
        StringBuilder sb = new StringBuilder(baseUrl);
        sb.append("?page=").append(page).append("&size=").append(Math.min(size, 100));
        if (sort != null && !sort.isEmpty()) sb.append("&sort=").append(URLEncoder.encode(sort, StandardCharsets.UTF_8));
        if (query != null && !query.isEmpty()) sb.append("&query=").append(URLEncoder.encode(query, StandardCharsets.UTF_8));
        if (tags != null) {
            for (String tag : tags) {
                if (tag != null && !tag.isEmpty()) sb.append("&tags=").append(URLEncoder.encode(tag, StandardCharsets.UTF_8));
            }
        }
        return sb.toString();
    }
}
