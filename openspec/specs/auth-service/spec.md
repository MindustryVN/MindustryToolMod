# auth-service Specification

## Purpose
TBD - created by archiving change rewrite-auth-service. Update Purpose after archive.
## Requirements
### Requirement: AuthService logic split from UI
`mindustrytool.services.AuthService` SHALL be a pure-logic singleton with no Arc scene/UI imports, owning `UserSession currentSession`, `CompletableFuture<Void> loginFuture`, `KEY_*` constants (`mindustrytool.auth.access-token`, `mindustrytool.auth.refresh-token`, `mindustrytool.auth.login-id`, `mindustrytool.auth.login-expiry`), and delegating every HTTP call through `mindustrytool.services.MindustryTool` typed methods (`getSession`, `getLoginUri`, `pollLoginToken`, `logout`) rather than constructing `Request` directly. `mindustrytool.ui.AuthOverlay` (and `mindustrytool.ui.AuthLoginDialog`) SHALL be the sole UI owners of `authWindow`/`wholeViewport` and dialog rendering. `MindustryAuthProvider` SHALL remain preserved unchanged as the sole `AuthProvider` and `MindustryTool.api` wiring `authProvider(MindustryAuthProvider.getInstance())` SHALL NOT change.

#### Scenario: AuthService has no UI imports and no Request for auth endpoints
- **WHEN** `src/mindustrytool/services/AuthService.java` is inspected
- **THEN** it contains no `import arc.scene.*`, no `import mindustry.ui.*`, no `import mindustrytool.ui.*`, and contains no `api.get("auth/app/login-uri")`, `api.get("auth/app/login-token")`, `api.post("auth/app/refresh")`, `api.post("auth/app/logout")`; all such calls go via `MindustryTool.*`

#### Scenario: AuthOverlay owns UI and AuthService owns logic
- **WHEN** `src/mindustrytool/ui/AuthOverlay.java` and `AuthLoginDialog.java` are inspected
- **THEN** `AuthOverlay` owns `Table authWindow`/`wholeViewport` and `AuthLoginDialog loginDialog`, while `AuthService` owns `currentSession` and `loginFuture`; `AuthService` does not reference `AuthOverlay` and `AuthOverlay` depends one-way on `AuthService`

#### Scenario: MindustryAuthProvider preserved and wired
- **WHEN** `src/mindustrytool/services/MindustryAuthProvider.java` is inspected and `MindustryTool.java` is inspected
- **THEN** `MindustryAuthProvider` still declares `private final Request api = Request.builder().baseUrl(Config.API_URL).authProvider(this).build()` and `synchronized refreshIfNeeded()` unchanged, and `MindustryTool` still contains `Request.builder().baseUrl(Config.API_URL).authProvider(MindustryAuthProvider.getInstance()).build()` for `api`; `AuthService` does NOT implement `AuthProvider` and does NOT appear as provider

#### Scenario: No old imports and no delegation shim
- **WHEN** `grep -R "old\.mindustrytool" src/mindustrytool` and `Select-String` are run
- **THEN** they return no results and `src/old/mindustrytool/features/auth/AuthService.java` does not exist (deleted without delegating wrapper)

### Requirement: AuthService session and event orchestration via MindustryTool
`AuthService` SHALL maintain `UserSession currentSession` cache, expose `UserSession getSession()` and `CompletableFuture<UserSession> fetchSession()` delegating to `MindustryTool.getSession()`, fire `SessionLoadEvent(user, error, isLoading)` (loading=true before request, then success/error on `Core.app.post`), fire raw `UserSession` event on non-null success, fire `LoginEvent` after successful token poll+fetch, fire `LogoutEvent` on logout, and expose `isLoggedIn()` as `Core.settings.has(KEY_ACCESS_TOKEN) && Core.settings.has(KEY_REFRESH_TOKEN)` (settings-only).

#### Scenario: fetchSession emits loading and result events
- **WHEN** `fetchSession()` is called
- **THEN** it posts `SessionLoadEvent(currentSession, null, true)` on `Core.app.post`, then on completion posts `SessionLoadEvent(finalSession, null, false)` on success or `SessionLoadEvent(currentSession, cause, false)` on error, and on non-null session also calls `Events.fire(session)`

#### Scenario: isLoggedIn reflects settings only
- **WHEN** `saveTokens` has been called but `fetchSession` has not yet completed
- **THEN** `isLoggedIn()` returns true because both settings keys exist

### Requirement: AuthService login flow and deduplication via MindustryTool and UI overlay
`AuthService` SHALL implement `synchronized CompletableFuture<Void> login()` with deduplicated `loginFuture`, persisting `KEY_LOGIN_ID`/`KEY_LOGIN_EXPIRY` (`+5m`), and delegating HTTP to `MindustryTool.getLoginUri()` and `MindustryTool.pollLoginToken(loginId)` (60s timeout). It SHALL coordinate with `AuthOverlay`/`AuthLoginDialog` to show `showLoading()` then `showLoginUrl(loginUrl)` on `Core.app.post`, open `Core.app.openURI(loginUrl)` fallback to `Core.app.setClipboardText(loginUrl)`, and expose `void cancelLogin()` completing `loginFuture` exceptionally. Token poll on failure removes `KEY_LOGIN_ID` and hides dialog via overlay.

#### Scenario: Login delegates to MindustryTool not raw Request
- **WHEN** `login()` is called
- **THEN** it calls `MindustryTool.getLoginUri()`, stores `loginId`/`loginExpiry`, opens URI, then calls `MindustryTool.pollLoginToken(loginId)` and on success `saveTokens` + `fetchSession` + `LoginEvent`; concurrent calls return same `loginFuture` and only one `getLoginUri()` is initiated

#### Scenario: Concurrent login returns same future
- **WHEN** `login()` is called twice before first completes
- **THEN** second call returns same `CompletableFuture<Void>` instance

#### Scenario: cancelLogin aborts pending login
- **WHEN** `loginFuture` pending and `cancelLogin()` called
- **THEN** `loginFuture` completes exceptionally with "Login cancelled"

### Requirement: AuthService init and periodic refresh (logic only)
`AuthService.init()` SHALL call `fetchSession()` immediately, schedule `Timer.schedule(() -> if(isLoggedIn()) fetchSession(), 300, 300)` every 5 minutes, and resume unexpired `KEY_LOGIN_ID` via `pollLoginToken(loginId)` in background (remove if expired). It SHALL NOT build `authWindow` or subscribe to `SessionLoadEvent` for rendering; that is `AuthOverlay` responsibility.

#### Scenario: Resume login on startup
- **WHEN** `Core.settings` contains `KEY_LOGIN_ID` with future `KEY_LOGIN_EXPIRY` at `init()` time
- **THEN** `pollLoginToken(loginId)` is called in background; **WHEN** expiry is past **THEN** both keys are removed and no poll occurs

#### Scenario: Periodic session refresh
- **WHEN** `init()` has completed and 5 minutes elapse while `isLoggedIn()` true
- **THEN** `fetchSession()` is invoked

### Requirement: AuthOverlay UI lifecycle
`mindustrytool.ui.AuthOverlay` SHALL expose `initUi()`/`init()` building `wholeViewport` fillParent top-right, `content` with `Styles.black6`, `authWindow.touchable = Touchable.childrenOnly`, adding to `Vars.ui.menuGroup` via `Core.app.post`, null-guarded for headless, and subscribing `Events.on(SessionLoadEvent.class, ...)` to render: `auth.session.loading` label, `auth.session.error` + message + `auth.session.retry` button → `AuthService.login()`, guest `auth.login` button, or authenticated avatar (`NetworkImage` 64) + name (desktop only) click → confirm dialog `auth.logout.confirm-title`/`auth.logout.confirm-message` (`{0}` player name) → `AuthService.logout()`. `AuthLoginDialog` SHALL display `auth.login.dialog-title`, `auth.login.cancel`, `auth.login.loading` label, and `auth.login.login-url` button with clipboard `auth.login.copied` toast, using `Core.bundle.get/format("auth.*")` only.

#### Scenario: AuthOverlay renders SessionLoadEvent states with auth.* keys
- **WHEN** `SessionLoadEvent(isLoading=true)` fired
- **THEN** `authWindow` content clears and shows `Core.bundle.get("auth.session.loading")`; **WHEN** error **THEN** `auth.session.error` + message + `auth.session.retry` button; **WHEN** user==null **THEN** `auth.login` button; **WHEN** user != null **THEN** avatar (if `imageUrl`) + name + click→confirm with `auth.logout.confirm-*` via `Core.bundle.format`

#### Scenario: AuthLoginDialog uses auth.* keys
- **WHEN** `AuthLoginDialog` is inspected
- **THEN** it uses `Core.bundle.get("auth.login.dialog-title")`, `get("auth.login.cancel")`, `get("auth.login.loading")`, `get("auth.login.copied")` and no `@login`/`@loading` generic keys nor hardcoded strings

#### Scenario: Headless init does not crash
- **WHEN** `AuthOverlay.init()` called with `Vars.ui == null`
- **THEN** it skips `authWindow`/`menuGroup` setup without exception; `AuthService.init()` still performs `fetchSession()`, timer, and loginId resume

### Requirement: Auth i18n namespace
All user-visible auth text SHALL be under `auth.*` keys in `assets/bundles/bundle.properties` with a mandatory comment directly above each key explaining where displayed, when used, and placeholder meanings (`{0}` etc.). Legacy generic keys (`@login`, `@loading`, `@error`, `@retry`, `@generate-loading-link`, `@copied`) SHALL NOT be used by auth after this change; they are cleared and replaced by `auth.*` equivalents. Dynamic text SHALL use `Core.bundle.format` with placeholders, not string concatenation.

#### Scenario: New keys exist with comments
- **WHEN** `assets/bundles/bundle.properties` is grepped for `^auth\.`
- **THEN** it contains at least `auth.login`, `auth.login.dialog-title`, `auth.login.cancel`, `auth.login.loading`, `auth.login.login-url`, `auth.login.copied`, `auth.login.success`, `auth.login.failed`, `auth.logout`, `auth.logout.confirm-title`, `auth.logout.confirm-message`, `auth.session.loading`, `auth.session.error`, `auth.session.retry`, each with a directly-above comment describing context and placeholders; **WHEN** `AuthService.java`/`AuthOverlay.java` are inspected **THEN** they contain `Core.bundle.get("auth.*")`/`format` and no hardcoded display strings like `"Login successful!"` or `"Logged in as "`

#### Scenario: No hardcoded user-visible text in AuthService/AuthOverlay
- **WHEN** `src/mindustrytool/services/AuthService.java` and `src/mindustrytool/ui/AuthOverlay.java` are inspected
- **THEN** they contain no string literals for UI messages outside `Core.bundle.get/format`; `Vars.ui.showConfirm` uses `Core.bundle.format("auth.logout.confirm-message", userName)` etc.

### Requirement: AuthService self-containment and import hygiene
`AuthService` and `AuthOverlay` SHALL import only `java.*`, `arc.*`, `mindustrytool.Config`, `mindustrytool.services.Request` (overlay only if needed), `mindustrytool.services.MindustryTool`, `mindustrytool.services.MindustryAuthProvider` (if token access needed), `mindustrytool.utils.JsonUtils` (if needed), `mindustrytool.models.*`/`events.*`, and SHALL NOT import `old.mindustrytool.*`. They SHALL use `Config.API_URL` (not `API_v4_URL`) and be safe when `Vars.ui` is null.

#### Scenario: No old imports and correct baseUrl
- **WHEN** `grep -R "old\.mindustrytool" src/mindustrytool/services/AuthService.java src/mindustrytool/ui/AuthOverlay.java` is run
- **THEN** returns no results and files reference `Config.API_URL` only

