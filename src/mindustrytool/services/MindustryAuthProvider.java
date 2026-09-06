package mindustrytool.services;

import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

import arc.Core;
import arc.util.Log;
import arc.util.serialization.Jval;
import mindustrytool.Config;

/**
 * New AuthService for the rewritten codebase.
 * Implements AuthProvider and owns a Request instance.
 * Copied from old.mindustrytool.features.auth.AuthService token logic,
 * but without importing old code - all dependencies are duplicated locally.
 */
public class MindustryAuthProvider implements AuthProvider {
    private static MindustryAuthProvider instance;

    public static final String KEY_ACCESS_TOKEN = "mindustrytool.auth.access-token";
    public static final String KEY_REFRESH_TOKEN = "mindustrytool.auth.refresh-token";
    public static final String KEY_LOGIN_ID = "mindustrytool.auth.login-id";
    public static final String KEY_LOGIN_EXPIRY = "mindustrytool.auth.login-expiry";

    private final Request api;
    private CompletableFuture<Void> refreshFuture;

    public static MindustryAuthProvider getInstance() {
        if (instance == null) {
            instance = new MindustryAuthProvider();
        }
        return instance;
    }

    private MindustryAuthProvider() {
        this.api = Request.builder()
                .baseUrl(Config.API_URL)
                .timeout(Duration.ofSeconds(10))
                .authProvider(this)
                .build();
    }

    public Request getApi() {
        return api;
    }

    @Override
    public String getAccessToken() {
        return Core.settings.getString(KEY_ACCESS_TOKEN, null);
    }

    public String getRefreshToken() {
        return Core.settings.getString(KEY_REFRESH_TOKEN, null);
    }

    public void saveTokens(String accessToken, String refreshToken) {
        Core.settings.put(KEY_ACCESS_TOKEN, accessToken);
        Core.settings.put(KEY_REFRESH_TOKEN, refreshToken);
        Core.settings.forceSave();
    }

    public boolean isTokenNearExpiry(String token) {
        if (token == null) {
            return true;
        }
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2)
                return true;
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            Jval json = Jval.read(payload);
            long exp = json.getLong("exp", 0);
            long now = System.currentTimeMillis() / 1000;
            return (exp - now) < 60;
        } catch (Exception e) {
            Log.err("Failed to parse token expiry", e);
            return true;
        }
    }

    @Override
    public synchronized CompletableFuture<Void> refreshIfNeeded() {
        if (refreshFuture != null && !refreshFuture.isDone()) {
            return refreshFuture;
        }

        refreshFuture = new CompletableFuture<>();

        String accessToken = getAccessToken();
        String refreshToken = getRefreshToken();

        if (refreshToken == null) {
            refreshFuture.complete(null);
            return refreshFuture;
        }

        if (accessToken != null && !isTokenNearExpiry(accessToken)) {
            refreshFuture.complete(null);
            return refreshFuture;
        }

        if (isTokenNearExpiry(refreshToken)) {
            Log.info("Refresh token near expiry, removed it");
            Core.settings.remove(KEY_REFRESH_TOKEN);
            refreshFuture.complete(null);
            return refreshFuture;
        }

        Jval json = Jval.newObject();
        json.put("refreshToken", refreshToken);

        api.post("auth/app/refresh")
                .withoutAuth()
                .json(json.toString())
                .timeout(Duration.ofSeconds(10))
                .sendAsync()
                .whenComplete((res, err) -> {
                    if (err != null) {
                        Log.err("Failed to refresh token", err);
                        refreshFuture.completeExceptionally(err);
                        return;
                    }
                    int code = res.statusCode();
                    String body = res.body();
                    if (code == 401) {
                        Core.settings.remove(KEY_ACCESS_TOKEN);
                        Core.settings.remove(KEY_REFRESH_TOKEN);
                        Log.info("Remove tokens after 401");
                        Log.err(body);
                        refreshFuture.completeExceptionally(new RuntimeException("Refresh failed: HTTP 401 " + body));
                        return;
                    }
                    if (code != 200) {
                        Log.err("Failed to refresh token: HTTP " + code + " " + body);
                        refreshFuture.completeExceptionally(new RuntimeException("Refresh failed: HTTP " + code + " " + body));
                        return;
                    }
                    try {
                        Jval resJson = Jval.read(body);
                        if (resJson.has("accessToken") && resJson.has("refreshToken")) {
                            saveTokens(resJson.getString("accessToken"), resJson.getString("refreshToken"));
                            Log.info("Token refreshed successfully");
                            refreshFuture.complete(null);
                        } else {
                            refreshFuture.completeExceptionally(new RuntimeException("Invalid refresh response: " + resJson));
                        }
                    } catch (Exception e) {
                        refreshFuture.completeExceptionally(new RuntimeException("Failed to refresh token: exception", e));
                    }
                });

        return refreshFuture;
    }

    public boolean isLoggedIn() {
        return Core.settings.has(KEY_ACCESS_TOKEN) && Core.settings.has(KEY_REFRESH_TOKEN);
    }

    public void logout() {
        String accessToken = Core.settings.getString(KEY_ACCESS_TOKEN, "");
        String refreshToken = Core.settings.getString(KEY_REFRESH_TOKEN, "");

        if (!accessToken.isEmpty() && !refreshToken.isEmpty()) {
            Jval json = Jval.newObject();
            json.put("accessToken", accessToken);
            json.put("refreshToken", refreshToken);

            api.post("auth/app/logout")
                    .withoutAuth()
                    .header("Authorization", "Bearer " + accessToken)
                    .json(json.toString())
                    .sendAsync()
                    .whenComplete((res, err) -> {
                        if (err != null) {
                            Log.err("Logout failed", err);
                            return;
                        }
                        if (res.statusCode() >= 400) {
                            Log.err("Logout failed: HTTP " + res.statusCode() + " " + res.body());
                        } else {
                            Log.info("Logout successful");
                        }
                    });
        }

        Core.settings.remove(KEY_ACCESS_TOKEN);
        Core.settings.remove(KEY_REFRESH_TOKEN);
        Core.settings.remove(KEY_LOGIN_ID);
        Log.info("Logged out");
    }
}
