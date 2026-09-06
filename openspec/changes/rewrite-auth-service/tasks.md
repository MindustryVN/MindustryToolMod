## 1. Models, Events & i18n Setup

- [ ] 1.1 Copy `UserSession`, `AuthTokenResponse`, `LoginUriResponse` DTOs from `src/old` to `src/mindustrytool/models` (package `mindustrytool.models`) if missing; verify Jackson annotations and `FAIL_ON_UNKNOWN_PROPERTIES=false`
- [ ] 1.2 Create `mindustrytool.events` DTOs `SessionLoadEvent(UserSession user, Throwable error, boolean isLoading)`, `LoginEvent`, `LogoutEvent` by copying `old.mindustrytool.features.auth.dto.*` with new package `mindustrytool.events`, no `old.*` imports
- [ ] 1.3 Clear all legacy auth i18n keys from `assets/bundles/bundle.properties` (remove generic `@login`/`@loading` usage by auth) and add new namespaced keys with mandatory per-key comments: `auth.login`, `auth.login.dialog-title`, `auth.login.cancel`, `auth.login.loading`, `auth.login.login-url`, `auth.login.copied`, `auth.login.success`, `auth.login.failed`, `auth.login.timeout`, `auth.logout`, `auth.logout.confirm-title`, `auth.logout.confirm-message` (`{0}` player name), `auth.session.loading`, `auth.session.error`, `auth.session.retry` — each comment explains where displayed and placeholder meaning
- [ ] 1.4 Verify `mindustrytool.utils.JsonUtils` handles auth DTOs; ensure `Config.API_URL` normalized (no trailing slash), note `API_v4_URL` dropped for new code

## 2. Preserve MindustryAuthProvider

- [ ] 2.1 Verify `src/mindustrytool/services/MindustryAuthProvider.java` remains preserved unchanged: `implements AuthProvider`, `private final Request api = Request.builder().baseUrl(Config.API_URL).authProvider(this).build()`, JWT `exp<60s` via `Jval`, `synchronized refreshIfNeeded()` with `CompletableFuture<Void> refreshFuture` deduplication, `Core.settings` `mindustrytool.auth.*` keys, direct `api.post("auth/app/refresh").withoutAuth().json(...)` refresh path
- [ ] 2.2 Verify `MindustryTool.java` keeps `private static final Request api` wired to `MindustryAuthProvider.getInstance()` and `publicApi` without provider; no wiring to `AuthService`
- [ ] 2.3 Ensure `MindustryAuthProvider` has no `old.*` imports and no delegation to `AuthService` (no alias, no forwarding)

## 3. Core AuthService Logic (no UI)

- [ ] 3.1 Create `src/mindustrytool/services/AuthService.java` pure-logic singleton (`getInstance()`), constants `KEY_ACCESS_TOKEN/REFRESH_TOKEN/LOGIN_ID/LOGIN_EXPIRY`, `UserSession currentSession`, `CompletableFuture<Void> loginFuture` only (no `refreshFuture`, no `Request api`, no `AuthProvider`), no `arc.scene.*`/`mindustry.ui.*` imports
- [ ] 3.2 Implement token/setting helpers `saveTokens`, `isLoggedIn()` (settings-only `has(ACCESS)&&has(REFRESH)`), `getAccessToken`/`getRefreshToken` via `Core.settings` (same keys as preserved provider) — no JWT logic duplicated
- [ ] 3.3 Implement `fetchSession(): CompletableFuture<UserSession>` delegating to `MindustryTool.getSession()`, posting `SessionLoadEvent(isLoading=true)` then success/error on `Core.app.post`, updating `currentSession`, firing `Events.fire(session)` on non-null
- [ ] 3.4 Implement `synchronized CompletableFuture<Void> login()` delegating to `MindustryTool.getLoginUri()` then `MindustryTool.pollLoginToken(loginId)` (60s timeout), persisting `KEY_LOGIN_ID/EXPIRY (+5m)`, deduplicating via `loginFuture`, coordinating with `AuthOverlay` via `Core.app.post` callbacks to show `showLoading()`/`showLoginUrl(loginUrl)`, `Core.app.openURI` fallback to clipboard `Core.app.setClipboardText`, and `cancelLogin()` completing exceptionally with `Core.bundle.get("auth.login.failed")` or `"Login cancelled"` via `auth.*` key
- [ ] 3.5 Implement `pollLoginToken(String loginId): CompletableFuture<Void>` for background resume (remove `KEY_LOGIN_ID` on completion, handle timeout vs failure) and `logout()` delegating to `MindustryTool.logout(accessToken, refreshToken)` then local cleanup (`remove` keys, clear `currentSession`), `fetchSession()` null emit, `Events.fire(new LogoutEvent())`, `Log.info` with no hardcoded strings
- [ ] 3.6 Implement `init()` logic-only: immediate `fetchSession()`, `Timer.schedule(300s)` periodic `fetchSession` if `isLoggedIn()`, resume unexpired `KEY_LOGIN_ID` via `pollLoginToken` in background (remove if expired) — no `authWindow`/`Vars.ui` code
- [ ] 3.7 Verify `AuthService.java` imports only `java.*`, `arc.*`, `mindustrytool.Config`, `MindustryTool`, `MindustryAuthProvider` (if needed), `models.*`/`events.*`, `Core.bundle`; no `old.*`, no `Request` auth literals, `grep` empty for `old\.mindustrytool`

## 4. AuthOverlay UI Split

- [ ] 4.1 Create `src/mindustrytool/ui/AuthOverlay.java` singleton UI overlay: `Table wholeViewport`/`authWindow`, `AuthLoginDialog loginDialog`, `initUi()`/`init()` building top-right `authWindow` (`Styles.black6`, `Touchable.childrenOnly`, `Core.app.post` → `Vars.ui.menuGroup.addChild`, null-guarded for headless), subscribing `Events.on(SessionLoadEvent.class, render)`
- [ ] 4.2 Port `AuthLoginDialog` to `src/mindustrytool/ui/AuthLoginDialog.java` taking `AuthService` in constructor, using `Core.bundle.get("auth.login.dialog-title")`, `get("auth.login.cancel")`, methods `showLoading()` → `cont.add(Core.bundle.get("auth.login.loading"))`, `showLoginUrl(loginUrl)` → clipboard button with `Core.bundle.get("auth.login.copied")` via `Vars.ui.showInfoFade`
- [ ] 4.3 Implement `AuthOverlay` `SessionLoadEvent` rendering: `auth.session.loading` label, `auth.session.error` + message + `auth.session.retry` button → `AuthService.getInstance().login()`, guest `auth.login` button → `startLoginUI()`, authenticated `NetworkImage(64)` + `user.getName()` desktop-only + `Touchable.enabled` click → `Vars.ui.showConfirm(Core.bundle.get("auth.logout.confirm-title"), Core.bundle.format("auth.logout.confirm-message", user.getName()), AuthService.getInstance()::logout)`, `content.pack()` + `toFront()`
- [ ] 4.4 Add `startLoginUI()` in `AuthOverlay` (`AuthService.getInstance().login().thenRun(() -> Core.app.post(() -> Vars.ui.showInfo(Core.bundle.get("auth.login.success")))).exceptionally(e -> Core.app.post(() -> Vars.ui.showException(Core.bundle.get("auth.login.failed"), e)))`) with all strings via `auth.*`
- [ ] 4.5 Verify `AuthOverlay`/`AuthLoginDialog` use `Core.bundle.get/format("auth.*")` for every user-visible string, no `@login`/`@loading` generic keys, no hardcoded literals like `"Login successful!"` or `"Logged in as "`, each new key has per-key comment in `bundle.properties`

## 5. Legacy Removal (no delegate)

- [ ] 5.1 Delete `src/old/mindustrytool/features/auth/AuthService.java` outright (no delegating wrapper, no `@Deprecated` shim); update `src/old/mindustrytool/Main.java` to initialize new `mindustrytool.services.AuthService.getInstance().init()` and `mindustrytool.ui.AuthOverlay.getInstance().initUi()` (or single entry), remove `old AuthService` import
- [ ] 5.2 Do NOT create a delegating alias from `MindustryAuthProvider` to `AuthService` nor `old AuthService` to new; document hard break for 20+ legacy call sites (`StorageService`, `ChatStreamClient`, etc.) to be migrated in follow-on
- [ ] 5.3 Ensure `assets/bundles/bundle.properties` deletion of legacy cleared keys is intentional and documented; no auth code references cleared keys

## 6. Verification

- [ ] 6.1 Run `Select-String "old\\.mindustrytool" -Path src/mindustrytool -Recurse` → no results; run `./gradlew compileJava` → only pre-existing `PagingRequest` error remains; verify `AuthService.java` has no `arc.scene.*` imports and `AuthOverlay.java` owns all UI imports
- [ ] 6.2 Run `Select-String "^auth\\." -Path assets/bundles/bundle.properties` → ≥12 keys under `auth.*`, each with directly-above comment explaining display location and `{0}` placeholders; verify no `@login`/`@loading` used by auth
- [ ] 6.3 Manual/semi-manual test runtime: `AuthService` login flow (get URI → dialog → poll 60s → save → LoginEvent), `fetchSession` events (loading→success/error), `isLoggedIn` settings-only, background `loginId` resume, `Timer` 300s periodic, `AuthOverlay` states (loading/error/guest/authenticated) with `auth.*` texts, `AuthLoginDialog` clipboard/openURI, logout confirm via `auth.logout.confirm-*`
- [ ] 6.4 Regression: `MindustryTool` typed `UserSession`/`LoginUriResponse`/`AuthTokenResponse` via `JsonUtils` unchanged; `MindustryAuthProvider` refresh deduplication still single `POST auth/app/refresh` withoutAuth; `Request` per-instance auth/ `withoutAuth` unchanged
