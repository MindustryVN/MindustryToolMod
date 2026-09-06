package mindustrytool.services.auth;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

import arc.Core;
import arc.Events;
import arc.util.Log;
import arc.util.Timer;
import arc.util.serialization.Jval;
import mindustrytool.Config;
import mindustrytool.events.LoginEvent;
import mindustrytool.events.LoginUriEvent;
import mindustrytool.events.LogoutEvent;
import mindustrytool.events.SessionLoadEvent;
import mindustrytool.models.response.UserSession;
import mindustrytool.services.MindustryTool;
import mindustrytool.services.Request;

/**
 * Merged auth provider for the rewritten codebase.
 * Implements AuthProvider and owns a Request instance, and now also owns
 * session/login/logout lifecycle previously in AuthService.
 * All HTTP for auth/* goes via MindustryTool typed facade except refresh
 * which uses its own Request with withoutAuth to avoid recursion.
 */
public class MindustryAuthProvider implements AuthProvider {
    private static MindustryAuthProvider instance;

    public static final String KEY_ACCESS_TOKEN = "mindustrytool.auth.access-token";
    public static final String KEY_REFRESH_TOKEN = "mindustrytool.auth.refresh-token";
    public static final String KEY_LOGIN_ID = "mindustrytool.auth.login-id";
    public static final String KEY_LOGIN_EXPIRY = "mindustrytool.auth.login-expiry";

    private final Request api;
    private CompletableFuture<Void> refreshFuture;
    private CompletableFuture<Void> loginFuture;
    private UserSession currentSession;

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

    // ─── Token storage ──────────────────────────────

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

    // ─── Session ──────────────────────────────

    public UserSession getSession() {
        return currentSession;
    }

    public CompletableFuture<UserSession> fetchSession() {
        Core.app.post(() -> Events.fire(new SessionLoadEvent(currentSession, null, true)));
        return MindustryTool.getSession()
                .handle((session, err) -> {
                    if (err != null) {
                        Throwable cause = err.getCause() != null ? err.getCause() : err;
                        Core.app.post(() -> Events.fire(new SessionLoadEvent(currentSession, cause, false)));
                        throw new RuntimeException(cause);
                    }
                    this.currentSession = session;
                    UserSession finalSession = session;
                    Core.app.post(() -> Events.fire(new SessionLoadEvent(finalSession, null, false)));
                    if (session != null) {
                        Events.fire(session);
                    }
                    return session;
                });
    }

    // ─── Login ──────────────────────────────

    public synchronized CompletableFuture<Void> login() {
        if (loginFuture != null && !loginFuture.isDone()) {
            return loginFuture;
        }

        loginFuture = new CompletableFuture<>();

        MindustryTool.getLoginUri()
                .whenComplete((uri, err) -> {
                    if (err != null) {
                        loginFuture.completeExceptionally(new RuntimeException("Failed to get login URI", err));
                        return;
                    }
                    try {
                        String loginUrl = uri.getLoginUrl();
                        String loginId = uri.getLoginId();

                        Core.settings.put(KEY_LOGIN_ID, loginId);
                        Core.settings.put(KEY_LOGIN_EXPIRY, Instant.now().plus(Duration.ofMinutes(5)).toEpochMilli());

                        Core.app.post(() -> Events.fire(new LoginUriEvent(loginUrl, loginId)));

                        pollLoginToken(loginId).whenComplete((v, e) -> {
                            if (e != null) {
                                loginFuture.completeExceptionally(e);
                            } else {
                                loginFuture.complete(null);
                            }
                        });

                        if (!Core.app.openURI(loginUrl)) {
                            Core.app.setClipboardText(loginUrl);
                        }
                    } catch (Exception e) {
                        loginFuture.completeExceptionally(new RuntimeException("Failed to start login flow", e));
                    }
                });

        return loginFuture;
    }

    public void cancelLogin() {
        if (loginFuture != null && !loginFuture.isDone()) {
            loginFuture.completeExceptionally(new RuntimeException(Core.bundle.get("auth.login.failed")));
        }
    }

    public CompletableFuture<Void> pollLoginToken(String loginId) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        MindustryTool.pollLoginToken(loginId)
                .whenComplete((token, err) -> {
                    if (err != null) {
                        String msg = err.getMessage() != null ? err.getMessage().toLowerCase() : "";
                        boolean isTimeout = err instanceof java.net.SocketTimeoutException
                                || (err.getCause() instanceof java.net.SocketTimeoutException)
                                || msg.contains("timed out") || msg.contains("timeout");
                        if (isTimeout) {
                            future.completeExceptionally(err);
                            return;
                        }
                        Core.settings.remove(KEY_LOGIN_ID);
                        future.completeExceptionally(new RuntimeException("Failed to get login token", err));
                        return;
                    }
                    try {
                        Core.settings.remove(KEY_LOGIN_ID);

                        if (token != null && token.getAccessToken() != null && token.getRefreshToken() != null) {
                            saveTokens(token.getAccessToken(), token.getRefreshToken());

                            fetchSession().whenComplete((v, e) -> {
                                if (e != null) {
                                    future.completeExceptionally(e);
                                } else {
                                    Core.app.post(() -> Events.fire(new LoginEvent()));
                                    future.complete(null);
                                }
                            });
                        } else {
                            future.completeExceptionally(new RuntimeException("Invalid response: missing tokens"));
                        }
                    } catch (Exception e) {
                        future.completeExceptionally(e);
                    }
                });

        return future;
    }

    // ─── Logout ──────────────────────────────

    public void logout() {
        String accessToken = Core.settings.getString(KEY_ACCESS_TOKEN, "");
        String refreshToken = Core.settings.getString(KEY_REFRESH_TOKEN, "");

        if (!accessToken.isEmpty() && !refreshToken.isEmpty()) {
            MindustryTool.logout(accessToken, refreshToken)
                    .whenComplete((v, err) -> {
                        if (err != null) {
                            Log.err("Logout failed", err);
                        } else {
                            Log.info("Logout successful");
                        }
                    });
        }

        Core.settings.remove(KEY_ACCESS_TOKEN);
        Core.settings.remove(KEY_REFRESH_TOKEN);
        Core.settings.remove(KEY_LOGIN_ID);

        fetchSession();

        Events.fire(new LogoutEvent());

        Log.info("Logged out");
    }

    // ─── Init ──────────────────────────────

    public void init() {
        fetchSession();

        Timer.schedule(() -> {
            if (isLoggedIn()) {
                fetchSession();
            }
        }, 60 * 5, 60 * 5);

        String loginId = Core.settings.getString(KEY_LOGIN_ID);

        if (loginId != null) {
            Instant expiry = Instant.ofEpochMilli(Core.settings.getLong(KEY_LOGIN_EXPIRY, 0));

            if (expiry.isBefore(Instant.now())) {
                Core.settings.remove(KEY_LOGIN_ID);
                Core.settings.remove(KEY_LOGIN_EXPIRY);
            } else {
                pollLoginToken(loginId).exceptionally(e -> {
                    Log.err("Background login polling failed", e);
                    return null;
                });
            }
        }
    }
}
