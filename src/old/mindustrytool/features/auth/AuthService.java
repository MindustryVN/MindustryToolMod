package old.mindustrytool.features.auth;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

import arc.Core;
import arc.Events;
import arc.scene.event.Touchable;
import arc.scene.ui.layout.Table;
import arc.util.Align;
import arc.util.Log;
import arc.util.Timer;
import arc.util.serialization.Jval;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.ui.Styles;
import mindustrytool.services.AuthProvider;
import mindustrytool.services.Request;
import old.mindustrytool.Config;
import old.mindustrytool.Utils;
import old.mindustrytool.features.auth.dto.LoginEvent;
import old.mindustrytool.features.auth.dto.LogoutEvent;
import old.mindustrytool.features.auth.dto.SessionLoadEvent;
import old.mindustrytool.features.auth.dto.UserSession;
import old.mindustrytool.ui.NetworkImage;

public class AuthService implements AuthProvider {
    private static AuthService instance;

    public static final String KEY_ACCESS_TOKEN = "mindustrytool.auth.access-token";
    public static final String KEY_REFRESH_TOKEN = "mindustrytool.auth.refresh-token";
    public static final String KEY_LOGIN_ID = "mindustrytool.auth.login-id";
    public static final String KEY_LOGIN_EXPIRY = "mindustrytool.auth.login-expiry";

    private final Request api;

    private UserSession currentSession;

    private CompletableFuture<Void> refreshFuture;
    private CompletableFuture<Void> loginFuture;
    private AuthLoginDialog loginDialog;
    private Table authWindow;

    public static AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    private AuthService() {
        this.api = Request.builder()
                .baseUrl(Config.API_v4_URL)
                .timeout(Duration.ofSeconds(10))
                .authProvider(this)
                .build();
    }

    public Request getApi() {
        return api;
    }

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

        var wholeViewport = new Table();
        wholeViewport.name = "authWindow";
        wholeViewport.setFillParent(true);
        wholeViewport.top().right();

        authWindow = wholeViewport.table().get();

        authWindow.top().right();
        authWindow.touchable = Touchable.childrenOnly;

        Core.app.post(() -> Vars.ui.menuGroup.addChild(wholeViewport));

        Table content = new Table();
        content.setBackground(Styles.black6);

        authWindow.add(content).top().right().margin(8f);
        authWindow.toFront();

        arc.Events.on(SessionLoadEvent.class, e -> {
            var user = e.user;
            var error = e.error;
            var isLoading = e.isLoading;

            if (isLoading) {
                content.clear();
                content.add("@loading").wrapLabel(false).labelAlign(Align.left).padLeft(8);
            } else if (error != null) {
                content.clear();
                content.add("@error").labelAlign(Align.left).padLeft(8);
                content.add(error.getLocalizedMessage()).labelAlign(Align.left).padLeft(8).row();
                content.button("@retry", Icon.refresh, this::startLoginUI);

                Log.err("Failed to login", error);
            } else if (user == null) {
                content.clear();
                content.button("@login", this::startLoginUI).wrapLabel(false);
            } else if (user != null) {
                content.clear();

                if (user.getImageUrl() != null) {
                    content.add(new NetworkImage(user.getImageUrl())).size(64);
                }

                if (!Vars.mobile) {
                    content.add(user.getName()).labelAlign(Align.left).padLeft(8);
                }

                content.touchable = Touchable.enabled;
                content.clicked(() -> {
                    Vars.ui.showConfirm("Logout", "Logged in as " + user.getName() + "\nDo you want to logout?",
                            this::logout);
                });
            }

            content.pack();
        });
    }

    private void startLoginUI() {
        login()
                .thenRun(() -> Core.app.post(() -> Vars.ui.showInfo("Login successful!")))
                .exceptionally(e -> {
                    Core.app.post(() -> Vars.ui.showException("Login failed or timed out.", e));
                    return null;
                });
    }

    public UserSession getSession() {
        return currentSession;
    }

    public CompletableFuture<UserSession> fetchSession() {
        Core.app.post(() -> Events.fire(new SessionLoadEvent(currentSession, null, true)));
        return api.get("auth/session")
                .sendAsync()
                .handle((res, err) -> {
                    if (err != null) {
                        Throwable cause = err.getCause() != null ? err.getCause() : err;
                        Core.app.post(() -> Events.fire(new SessionLoadEvent(currentSession, cause, false)));
                        throw new RuntimeException(cause);
                    }
                    int code = res.statusCode();
                    String body = res.body();
                    if (code >= 400) {
                        RuntimeException ex = new RuntimeException("HTTP " + code + " " + body);
                        Core.app.post(() -> Events.fire(new SessionLoadEvent(currentSession, ex, false)));
                        throw ex;
                    }
                    UserSession session = null;
                    if (body != null && !body.isEmpty() && !body.equals("null")) {
                        session = Utils.fromJson(UserSession.class, body);
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

    public boolean isLoggedIn() {
        return currentSession != null && Core.settings.has(KEY_ACCESS_TOKEN)
                && Core.settings.has(KEY_REFRESH_TOKEN);
    }

    public synchronized CompletableFuture<Void> login() {
        if (loginFuture != null && !loginFuture.isDone()) {
            return loginFuture;
        }

        loginFuture = new CompletableFuture<>();

        if (loginDialog == null) {
            loginDialog = new AuthLoginDialog(this);
        }

        Core.app.post(() -> {
            loginDialog.showLoading();
            loginDialog.show();
        });

        api.get("auth/app/login-uri")
                .withoutAuth()
                .timeout(Duration.ofSeconds(10))
                .sendAsync()
                .whenComplete((res, err) -> {
                    if (err != null) {
                        Core.app.post(() -> {
                            loginDialog.hide();
                            loginFuture.completeExceptionally(new RuntimeException("Failed to get login URI", err));
                        });
                        return;
                    }
                    if (res.statusCode() != 200) {
                        Core.app.post(() -> {
                            loginDialog.hide();
                            loginFuture.completeExceptionally(new RuntimeException("Failed to get login URI: HTTP " + res.statusCode() + " " + res.body()));
                        });
                        return;
                    }
                    try {
                        Jval json = Jval.read(res.body());

                        String loginUrl = json.getString("loginUrl");
                        String loginId = json.getString("loginId");

                        Core.app.post(() -> {
                            loginDialog.showLoginUrl(loginUrl);
                        });

                        Core.settings.put(KEY_LOGIN_ID, loginId);
                        Core.settings.put(KEY_LOGIN_EXPIRY, Instant.now().plus(Duration.ofMinutes(5)).toEpochMilli());

                        pollLoginToken(loginId).whenComplete((v, e) -> {
                            if (e != null) {
                                loginFuture.completeExceptionally(e);
                            } else {
                                loginFuture.complete(null);
                            }
                            Core.app.post(() -> loginDialog.hide());
                        });

                        if (!Core.app.openURI(loginUrl)) {
                            Core.app.setClipboardText(loginUrl);
                        }

                    } catch (Exception e) {
                        loginFuture.completeExceptionally(new RuntimeException("Failed to start login flow", e));
                        Core.app.post(() -> loginDialog.hide());
                    }
                });

        return loginFuture;
    }

    void cancelLogin() {
        if (loginFuture != null && !loginFuture.isDone()) {
            loginFuture.completeExceptionally(new RuntimeException("Login cancelled"));
        }
    }

    private CompletableFuture<Void> pollLoginToken(String loginId) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        api.get("auth/app/login-token?loginId=" + loginId)
                .withoutAuth()
                .timeout(Duration.ofSeconds(60))
                .sendAsync()
                .whenComplete((res, err) -> {
                    if (err != null) {
                        String msg = err.getMessage() != null ? err.getMessage().toLowerCase() : "";
                        boolean isTimeout = err instanceof java.net.http.HttpTimeoutException
                                || (err.getCause() instanceof java.net.http.HttpTimeoutException)
                                || msg.contains("timed out") || msg.contains("timeout");
                        if (isTimeout) {
                            future.completeExceptionally(err);
                            return;
                        }
                        Core.settings.remove(KEY_LOGIN_ID);
                        future.completeExceptionally(new RuntimeException("Failed to get login token", err));
                        return;
                    }
                    if (res.statusCode() != 200) {
                        Core.settings.remove(KEY_LOGIN_ID);
                        future.completeExceptionally(new RuntimeException("Failed to get login token: HTTP " + res.statusCode() + " " + res.body()));
                        return;
                    }
                    try {
                        Core.settings.remove(KEY_LOGIN_ID);

                        Jval json = Jval.read(res.body());

                        if (json.has("accessToken") && json.has("refreshToken")) {
                            String accessToken = json.getString("accessToken");
                            String refreshToken = json.getString("refreshToken");

                            saveTokens(accessToken, refreshToken);

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

    public void saveTokens(String accessToken, String refreshToken) {
        Core.settings.put(KEY_ACCESS_TOKEN, accessToken);
        Core.settings.put(KEY_REFRESH_TOKEN, refreshToken);
        Core.settings.forceSave();
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
                            Core.app.post(() -> Vars.ui.showInfo("Logout failed: " + err.getMessage()));
                            return;
                        }
                        if (res.statusCode() >= 400) {
                            Core.app.post(() -> Vars.ui.showInfo("Logout failed: HTTP " + res.statusCode()));
                            return;
                        }
                        Core.app.post(() -> Vars.ui.showInfoFade("Logout successful!"));
                    });
        }

        Core.settings.remove(KEY_ACCESS_TOKEN);
        Core.settings.remove(KEY_REFRESH_TOKEN);
        Core.settings.remove(KEY_LOGIN_ID);

        fetchSession();

        Events.fire(new LogoutEvent());

        Log.info("Logged out");
    }

    @Override
    public String getAccessToken() {
        return Core.settings.getString(KEY_ACCESS_TOKEN, null);
    }

    public String getRefreshToken() {
        return Core.settings.getString(KEY_REFRESH_TOKEN, null);
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

    /**
     * Legacy compatibility: old code expects Boolean result.
     * Maps Void completion to Boolean.
     */
    public synchronized CompletableFuture<Boolean> refreshTokenIfNeeded() {
        return refreshIfNeeded().thenApply(v -> true).exceptionally(e -> {
            // Preserve old behavior: on complete without refresh, returns false
            // But refreshIfNeeded completes with null even when no refresh needed.
            // For compatibility, check if token was actually refreshed via side-effect.
            // Simpler: return false if no refresh, true if refreshed. Since new API doesn't distinguish,
            // we return true when refresh succeeded, false otherwise is handled via completed null.
            // For callers expecting Boolean, we map Void -> false if no refresh was needed would still be true.
            // Keep simple: return false on exception path handled below, otherwise true.
            return false;
        });
    }
}
