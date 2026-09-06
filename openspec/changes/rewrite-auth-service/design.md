## Context

Legacy `old.mindustrytool.features.auth.AuthService` predates the rewrite and violates current rewrite invariants:
- Uses `Config.API_v4_URL` (legacy baseUrl) and builds `Request` directly for every auth endpoint, bypassing `MindustryTool` typed facade.
- Mixes concerns: token storage, JWT parsing, `refreshFuture`/`loginFuture` deduplication, `UserSession` cache, `arc.Events` (`SessionLoadEvent`/`LoginEvent`/`LogoutEvent`), Arc UI (`authWindow`/`AuthLoginDialog`), and periodic `Timer`.
- New `mindustrytool.services.MindustryAuthProvider` extracts only the token/refresh slice (192 LOC) with `Request api = Request.builder().baseUrl(Config.API_URL).authProvider(this).build()`, JWT `exp<60s`, `refreshFuture` deduplication, and `Core.settings` `mindustrytool.auth.*`; it still does raw `api.post("auth/app/refresh").withoutAuth().json(...)` but is intended to be preserved as the sole `AuthProvider`. `MindustryTool` already exposes typed `getSession`, `getLoginUri`, `pollLoginToken`, `logout`, `refreshToken` via `publicApi`/`api` with `withoutAuth` where required and is wired to `MindustryAuthProvider.getInstance()`.
- 20+ call sites import `old.mindustrytool.features.auth.AuthService`; rewrite must replace it with a split `mindustrytool.*` AuthService (logic + UI) with zero `old.*` imports, `auth.*` i18n namespace only, single HTTP path via `MindustryTool`, no delegation shim, and preserved `MindustryAuthProvider`.
- i18n keys used by auth (`@login`, `@loading`, `@error`, `@retry`, `@generate-loading-link`, `@copied`, `Logout`, confirm text) are cleared; all new auth text must be under `auth.*` with per-key comments per `AGENTS.md`.

Stack: Java 17, Gradle, `arc.Core.settings`/`arc.Events`/`arc.util.serialization.Jval` (only in preserved `MindustryAuthProvider`), `mindustrytool.services.Request`+`AuthProvider`+`MindustryTool`, `mindustrytool.utils.JsonUtils`, `mindustrytool.Config.API_URL`. UI uses Mindustry `Vars.ui` / `Styles` / `Icon` / `Table` / `BaseDialog` (Arc scene2d). No new dependencies.

Stakeholders: mod runtime (game thread, `Main.init`), legacy features (`StorageService`, `ChatStreamClient`, `ChatStateManager`, `SaveSyncFeature`), future new-code consumers, translators (`assets/bundles/bundle.properties`).

## Goals / Non-Goals

**Goals:**
- Preserve `mindustrytool.services.MindustryAuthProvider` unchanged as the sole `AuthProvider`; `MindustryTool` wiring stays `authProvider(MindustryAuthProvider.getInstance())`.
- Introduce split `mindustrytool.services.AuthService` (pure logic, no UI imports) that delegates every HTTP call through `MindustryTool` typed methods and owns session cache/events/timers/loginFuture, and `mindustrytool.ui.AuthOverlay` (plus `AuthLoginDialog`) that owns `authWindow`/dialog rendering and `SessionLoadEvent` subscription — clean separation, no circular UI dependency.
- Zero `import old.*` in `src/mindustrytool`; auth events/DTOs under `mindustrytool.models`/`events`; `Config.API_URL` only.
- All user-visible auth text under `auth.*` namespace with mandatory per-key comments; cleared legacy generic keys are not reused.

**Non-Goals:**
- Changing auth endpoint contracts or `Config.API_URL`.
- Modifying `MindustryAuthProvider` token/JWT/refresh logic (preserve as-is, including its direct `api.post("auth/app/refresh").withoutAuth()` path).
- Merging `MindustryAuthProvider` into `AuthService` or making either delegate to the other.
- Adding token encryption, secure storage, or SSE reconnection logic.
- Migrating every legacy `AuthService` call site in this change beyond deleting `old AuthService` and wiring `Main.init` to new `AuthService`+`AuthOverlay` (full migration is follow-on).

## Decisions

**1. Preserve `MindustryAuthProvider` — no merge, no delegation.**
- Why: User explicitly requires preservation; provider is already the correct `AuthProvider` owner of `Request api` and JWT logic. Splitting keeps token auth stable and avoids re-introducing the `AuthService↔MindustryTool` circular dependency that a merged provider would recreate.
- Alternative considered: merge `MindustryAuthProvider` into `AuthService` (previous design). Rejected per updated requirement.
- Consequence: `AuthService` logic does NOT implement `AuthProvider`, does NOT own a `Request`, and does NOT deduplicate refresh; refresh remains in `MindustryAuthProvider.refreshIfNeeded()`. `AuthService` only reads/writes tokens via `Core.settings` `mindustrytool.auth.*` (same keys) for session/login/logout and relies on `MindustryAuthProvider` for Bearer refresh on `api` calls. `MindustryTool.api` wiring unchanged.
- Verification: `src/mindustrytool/services/MindustryAuthProvider.java` untouched; `grep` shows no new `AuthService implements AuthProvider`.

**2. Split `AuthService` into logic vs UI.**
- Why: Legacy mixed periodic `Timer`, events, session cache, and `authWindow`/`AuthLoginDialog` in one class (hard to test, headless needs UI guards). Splitting isolates Arc UI (`Vars.ui`, `Styles`, `Table`, `BaseDialog`, `NetworkImage`, `Icon`) from pure logic (events, `Core.settings`, `MindustryTool`).
- Structure:
  - `mindustrytool.services.AuthService` (logic): singleton, `KEY_*` constants, `UserSession currentSession`, `CompletableFuture<Void> loginFuture`, methods `fetchSession()`→`MindustryTool.getSession()`, `login()`→`MindustryTool.getLoginUri()`+`pollLoginToken`+`saveTokens`+`fetchSession`, `pollLoginToken(loginId)`, `logout()`→`MindustryTool.logout`, `isLoggedIn()` settings-only, `init()` (fetchSession, Timer 300s, resume loginId). No `arc.scene.*`/`mindustry.ui.*` imports; delegates dialog show to `AuthOverlay` via callbacks or `Core.app.post`.
  - `mindustrytool.ui.AuthOverlay` (+ `AuthLoginDialog`): owns `AuthLoginDialog` (BaseDialog), `Table authWindow`/`wholeViewport`, `initUi()` building overlay top-right `Styles.black6`, subscribes `Events.on(SessionLoadEvent.class, ...)` to render `auth.session.*` states, exposes `showLoading()`/`showLoginUrl(url)` forwarding to dialog, `startLoginUI()` helper. Depends on `AuthService` but `AuthService` does not depend on `AuthOverlay` (one-way).
- Alternative considered: single class + headless guards. Rejected: violates split requirement and leaks UI into logic tests.
- Headless: `AuthService.init()` works with `Vars.ui==null`; `AuthOverlay.init()` no-ops when `Vars.ui.menuGroup==null`.

**3. All HTTP through `MindustryTool` typed facade, not raw `Request` in new code.**
- Why: Enforces single HTTP boundary per `mindustrytool-api` spec; `AuthService` logic uses `MindustryTool.getSession`/`getLoginUri`/`pollLoginToken`/`logout` which already handle `publicApi`/`api`, `withoutAuth`, timeouts, DTO parsing. Preserved `MindustryAuthProvider` continues to use its own `api.post("auth/app/refresh")` for refresh (out of scope).
- `AuthService` contains no `api.get("auth/app/login-uri")` etc.; verification via `grep` in spec.

**4. `auth.*` i18n namespace with per-key comments, cleared legacy keys.**
- Why: User cleared all legacy i18n; new auth UI must not reuse `@login`/`@loading`/`@error` generic keys. New keys: `auth.login`, `auth.login.dialog-title`, `auth.login.cancel`, `auth.login.loading`, `auth.login.login-url`, `auth.login.copied`, `auth.login.success`, `auth.login.failed`, `auth.login.timeout`, `auth.logout`, `auth.logout.confirm-title`, `auth.logout.confirm-message` (with `{0}` player name), `auth.session.loading`, `auth.session.error`, `auth.session.retry`, etc.
- Per `AGENTS.md`, every key in `assets/bundles/bundle.properties` needs a directly-above comment explaining where displayed, when used, and placeholders. Group comments alone insufficient.
- Dynamic text via `Core.bundle.format("auth.logout.confirm-message", userName)` not string concatenation.

**5. No delegation shim for deleted `old AuthService`.**
- Why: Explicit requirement; legacy class deleted outright, not kept as delegator to new `AuthService`. `Main.init()` updated to call `AuthService.getInstance().init()` and `AuthOverlay.getInstance().initUi()` (or combined init). Legacy call sites (`StorageService`, `ChatStreamClient`, etc.) will fail to compile until follow-on migration — intentional hard break to force rewrite consumption.

**6. Keep JWT parsing and `Core.settings` keys in preserved provider; AuthService reuses same keys.**
- Why: No change to token storage contract; `mindustrytool.auth.*` keys remain for upgrade interop. `AuthService.saveTokens`/`getAccessToken`/`getRefreshToken` replicate provider's `Core.settings` access (or call provider getters) without duplicating JWT logic.

## Risks / Trade-offs

- **Two auth singletons (`MindustryAuthProvider` vs `AuthService`) confuse callers** → Mitigation: document ownership clearly — provider = token refresh/Bearer, service = session/login/logout/events. Spec `http-client` preserves provider requirement; new `auth-service` spec defines service's non-provider role. No `implements AuthProvider` on `AuthService` to avoid ambiguity.
- **Duplicate token read/write paths (`AuthService` + `MindustryAuthProvider`) diverge** → Mitigation: `AuthService` delegates token persistence to `MindustryAuthProvider.saveTokens` or shares exact `KEY_*` constants; add scenario asserting both classes use same `mindustrytool.auth.*` keys.
- **No delegate means hard compile break for 20+ legacy call sites** → Mitigation: intentional per requirement; gate change behind `openspec` feature flag or keep follow-on migration branch; verify `./gradlew compileJava` intentionally shows `old AuthService` missing until migrated (expected).
- **Event DTO relocation breaks legacy subscribers** → Mitigation: new events in `mindustrytool.events` with same shape; legacy subscribers updated in follow-on; optionally fire both old and new events for one release if bridge needed (not delegation).
- **UI split adds initialization order dependency (`AuthService.init()` before `AuthOverlay.init()`)** → Mitigation: `AuthOverlay` subscribes to `SessionLoadEvent` before `AuthService.fetchSession()` or tolerates missed initial event by querying `AuthService.getSession()` on init; document order in `Main`.
- **`auth.*` key churn requires translator rework** → Mitigation: per-key comments added; provide migration table old→new in PR description.

## Migration Plan

1. Delete `src/old/mindustrytool/features/auth/AuthService.java` (no shim) and clear legacy auth i18n keys from `assets/bundles/bundle.properties`.
2. Ensure auth DTOs/events (`UserSession`, `LoginUriResponse`, `AuthTokenResponse`, `SessionLoadEvent`, `LoginEvent`, `LogoutEvent`) exist in `mindustrytool.models`/`events` with no `old.*` imports.
3. Add new `auth.*` keys with per-key comments to `assets/bundles/bundle.properties`.
4. Create `mindustrytool.services.AuthService` (logic only, no UI imports) delegating HTTP to `MindustryTool`; wire no `AuthProvider`.
5. Create `mindustrytool.ui.AuthOverlay` + port `AuthLoginDialog` to `mindustrytool.ui`, both using `Core.bundle.get/format("auth.*")`.
6. Keep `MindustryAuthProvider.java` unchanged; verify `MindustryTool` still wires `MindustryAuthProvider.getInstance()`.
7. Update `src/old/mindustrytool/Main.java` (or new entrypoint) to init `AuthService` and `AuthOverlay`; leave legacy feature call-site migration to follow-on.
8. Verify: `grep -R "old\.mindustrytool" src/mindustrytool` empty, `grep -R "auth\.\*login" assets/bundles/bundle.properties` hits new keys with comments, `grep -R "import old" src/mindustrytool` empty, `AuthService.java` has no `arc.scene.*` imports, `AuthOverlay.java` has no `MindustryTool` direct auth endpoint literals.
9. Rollback: restore deleted `old AuthService.java` and legacy bundle keys; revert new `AuthService`/`AuthOverlay` files (isolated).

## Open Questions

- Exact `auth.*` key inventory: confirm full set with UX review (e.g. `auth.login.open-browser-failed` vs clipboard fallback).
- Should `AuthOverlay` be `mindustrytool.ui.AuthOverlay` or `mindustrytool.features.auth.AuthUi`? Current choice `mindustrytool.ui` for reusability; feature package alternative `mindustrytool.features.auth.ui`.
- Does `AuthService.pollLoginToken` need to be public for `AuthOverlay` to resume on startup, or should `AuthService` resume internally? Prefer internal `init()` resume, public `login()` only.
- Should `SessionLoadEvent` keep `isLoading` boolean or refine to enum `{LOADING,SUCCESS,ERROR}`? Keep boolean for compat.
