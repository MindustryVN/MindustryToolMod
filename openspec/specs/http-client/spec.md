# http-client Specification

## Purpose
Instance-based HTTP client for `mindustrytool.services` that wraps `java.net.http.HttpClient`/`HttpRequest`/`HttpResponse`, supports per-`Request` optional `AuthProvider` (Bearer only), fluent per-request builder, and multiple independent API clients.

## Requirements
### Requirement: Instance-based Request with builder

`Request` SHALL be an instantiable class wrapping a shared `java.net.http.HttpClient`, with per-instance `baseUrl`, default `timeout`, and optional `AuthProvider`. It SHALL be created via builder `Request.builder().baseUrl(...).timeout(...).authProvider(...).build()` and MUST NOT use global static auth state.

#### Scenario: Builder creates independent instances
- **WHEN** `Request.builder().baseUrl("https://example.com/api/").build()` and `Request.builder().baseUrl("https://other.example.com/").authProvider(otherProvider).build()` are created
- **THEN** each instance stores its own `baseUrl`/`timeout`/`authProvider` independently and `authProvider` may be `null` for public APIs

#### Scenario: Public API without authentication
- **WHEN** `Request.builder().baseUrl("https://example.com/api/").build()` is used
- **THEN** the instance has no `AuthProvider` and `sendAsync()` sends without `Authorization` header

#### Scenario: Authenticated API with Bearer provider
- **WHEN** `Request.builder().baseUrl(Config.API_URL).authProvider(authService).build()` is used where `authService` implements `AuthProvider`
- **THEN** the instance stores the provider and uses it for all requests unless `withoutAuth()` is set

#### Scenario: Shared HttpClient may be reused
- **WHEN** multiple `Request` instances are created
- **THEN** the underlying `HttpClient` MAY be a shared static instance (e.g. `CLIENT`) with `connectTimeout(10s)`; each `Request` MUST keep its own `baseUrl`/`timeout`/`authProvider`

#### Scenario: No global static auth provider
- **WHEN** `src/mindustrytool/services/Request.java` is inspected
- **THEN** it contains no `static AuthProvider`, no `setAuthProvider(...)`, and no `static volatile` token state; auth is per-instance only

### Requirement: AuthProvider interface for Bearer token

A new `mindustrytool.services.AuthProvider` SHALL provide Bearer-only authentication via `CompletableFuture<Void> refreshIfNeeded()` and `String getAccessToken()` and MUST NOT support Basic/API-key/chains/interceptors.

#### Scenario: AuthProvider contract
- **WHEN** `AuthProvider` is inspected
- **THEN** it declares `refreshIfNeeded()` returning `CompletableFuture<Void>` and `getAccessToken()` returning `String`/`null`, with no other auth methods

#### Scenario: Request uses provider in sendAsync flow
- **WHEN** `api.get("users/me").sendAsync()` is called on a `Request` with `AuthProvider`
- **THEN** `sendAsync` first calls `authProvider.refreshIfNeeded()`, then `getAccessToken()`, then adds `Authorization: Bearer <token>` if non-null before `HttpClient.sendAsync()`

#### Scenario: withoutAuth bypasses provider
- **WHEN** `api.get("auth/app/login-uri").withoutAuth().sendAsync()` is called
- **THEN** the builder skips `refreshIfNeeded()` and does not add `Authorization` even if the `Request` has an `AuthProvider`

### Requirement: Fluent per-request builder

Each `Request` SHALL expose `get/post/put/delete(String url)` returning a fluent `RequestBuilder` that supports `header`, `timeout`, `body`, `json`, `bytes`, `withoutAuth`, `sendAsync()`, and `sendAsync(BodyHandler<T>)` without duplicated `auth*` methods.

#### Scenario: Fluent GET with header and timeout
- **WHEN** `api.get("users/me").header("Accept","application/json").timeout(Duration.ofSeconds(30)).sendAsync()` is called
- **THEN** it builds a `GET` `HttpRequest` with the header, per-request timeout override, and shared/client timeout fallback

#### Scenario: POST JSON helper
- **WHEN** `api.post("items").json(json).sendAsync()` is called
- **THEN** it sets `Content-Type: application/json` and `BodyPublishers.ofString(json)`

#### Scenario: Body variants
- **WHEN** `body(String)` is called it uses `BodyPublishers.ofString`; **WHEN** `bytes(byte[])` is called it uses `BodyPublishers.ofByteArray`; `header` appends to `LinkedHashMap` for stable order

#### Scenario: No auth* duplicated methods
- **WHEN** `Request` and `RequestBuilder` are inspected
- **THEN** there are no `authGet/authPost/authPut/authDelete` methods; authentication is automatic via the instance's `AuthProvider`

### Requirement: URL resolution and timeout handling

`RequestBuilder` SHALL resolve relative URLs against `baseUrl` and absolute URLs ( `http://`/`https://` ) as-is, and SHALL apply per-request `timeout` or instance default `DEFAULT_TIMEOUT(10s)`.

#### Scenario: Relative URL resolves against baseUrl
- **WHEN** `baseUrl="https://api.example.com/api/v4"` and `get("users/me")` is called
- **THEN** resolved URL is `https://api.example.com/api/v4/users/me` (handling trailing/leading slash and `?`/`#` without extra slash)

#### Scenario: Absolute URL bypasses baseUrl
- **WHEN** `api.get("https://other-api.example.com/data").sendAsync()` is called
- **THEN** the request URI is exactly `https://other-api.example.com/data` regardless of `baseUrl`

#### Scenario: Per-request timeout overrides instance timeout
- **WHEN** instance `timeout=10s` and `get("/maps/1/data").timeout(Duration.ofSeconds(60)).sendAsync(BodyHandlers.ofByteArray())` is called
- **THEN** the `HttpRequest` is built with `timeout(60s)`; otherwise it uses the instance default

### Requirement: AuthService implements AuthProvider with deduplicated refresh

`MindustryAuthProvider` (new `mindustrytool.services` auth service) SHALL implement `AuthProvider`, own its `Request api = Request.builder().baseUrl(Config.API_URL).authProvider(this).build()`, and handle token storage, JWT `exp` check (`<60s` near-expiry), and deduplicated concurrent refresh via single `refreshFuture: CompletableFuture<Void>`.

#### Scenario: Refresh deduplication
- **WHEN** three concurrent `api.get("/auth/session").sendAsync()` calls find the access token near expiry
- **THEN** only one `POST auth/app/refresh` with `withoutAuth()` is sent; all three await the same `refreshFuture`; success completes all, failure fails all exceptionally

#### Scenario: Recursion prevention
- **WHEN** `refreshIfNeeded()` triggers `api.post("auth/app/refresh").withoutAuth().json(...).sendAsync()`
- **THEN** that inner request has `useAuth=false` so it does not call `refreshIfNeeded()` again; similarly `auth/app/login-uri` and `auth/app/login-token` use `withoutAuth()`

#### Scenario: Authenticated session fetch uses automatic Bearer
- **WHEN** `api.get("auth/session").sendAsync()` is called (via `MindustryTool.getSession()`)
- **THEN** it automatically refreshes if needed and sends `Authorization: Bearer <latest token>` without caller manually adding the header

### Requirement: Multiple independent API clients

The architecture SHALL support `mindustrytool` API with auth, another API with different auth, and a public API without auth simultaneously without interference.

#### Scenario: Three clients coexist
- **WHEN** `Request mindustryApi = Request.builder().baseUrl(Config.API_URL).authProvider(mindustryAuthProvider).build()`, `Request otherApi = Request.builder().baseUrl(OTHER_API_URL).authProvider(otherAuthProvider).build()`, and `Request publicApi = Request.builder().baseUrl(PUBLIC_API_URL).build()` exist
- **THEN** each sends with its own provider (or none) and tokens do not leak between clients

#### Scenario: MindustryTool uses two internal clients
- **WHEN** `MindustryTool` is inspected
- **THEN** it holds `private static final Request api` with `MindustryAuthProvider` and `private static final Request publicApi` without provider, using `api` for `/chats/*`/`/auth/session` and `publicApi` for `/ping`, `/maps/*`, `/schematics/*`, images, and unauth auth endpoints (`/auth/app/login-uri` with `withoutAuth`)

### Requirement: Multipart upload helper retained

`Request` SHALL retain `static byte[] buildMultipartBody(boundary, fileBytes, fileName, hash)` building `multipart/form-data` with `hash` and `file` parts.

#### Scenario: Multipart body structure
- **WHEN** `buildMultipartBody` is called
- **THEN** it writes `hash` part and `file` part with `Content-Type: application/octet-stream` bounded by `--boundary` and `--boundary--`

### Requirement: Import hygiene for Request

`Request` SHALL use explicit imports for `HttpClient`, `HttpRequest`, `HttpResponse`, `BodyHandlers`, `BodyPublishers`, `URI`, `Duration`, `StandardCharsets` and MUST NOT use fully-qualified inline names inside method bodies.

#### Scenario: No fully-qualified Http types in method bodies
- **WHEN** `src/mindustrytool/services/Request.java` is inspected
- **THEN** it contains `import java.net.http.HttpRequest` etc. and bodies reference `HttpRequest`, `BodyHandlers`, `BodyPublishers`, `Duration` without `java.net.http.*` prefixes, and contains no `import old.*`
