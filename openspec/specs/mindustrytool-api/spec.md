# mindustrytool-api Specification

## Purpose
Typed `mindustrytool.services.MindustryTool` API facade over `Config.API_URL` that delegates all HTTP to instance-based `Request` clients and parses JSON via copied `mindustrytool.utils.JsonUtils`.

## Requirements
### Requirement: MindustryTool delegates all HTTP to instance Request and uses only Config.API_URL

`MindustryTool` SHALL contain only methods that target `Config.API_URL`; it MUST NOT construct `HttpClient`/`HttpRequest` directly and SHALL delegate every call via instance `Request` (`api` with `MindustryAuthProvider`, `publicApi` without auth) using fluent `get/post/put/delete(...).header/timeout/json/bytes/withoutAuth().sendAsync()`.

#### Scenario: No direct HttpClient construction in MindustryTool
- **WHEN** `src/mindustrytool/services/MindustryTool.java` is inspected
- **THEN** it contains no `HttpClient.newBuilder`, no `HttpRequest.newBuilder`, and no inline `java.net.http.*` fully-qualified construction; all HTTP goes through `private static final Request api` and `publicApi` built via `Request.builder()`

#### Scenario: Auth vs public client split
- **WHEN** `MindustryTool` methods are listed
- **THEN** chat (`/chats/*` with `api`), `getSession` (`api.get("/auth/session")`), and similar use the authenticated `api`; `ping`, `maps/*`, `schematics/*`, `tags`, `users/batches`, `planets`, `servers`, `player-connect/*`, images, `submitCrashReport`, and unauth auth endpoints (`/auth/app/login-uri`, `/auth/app/login-token`, `/auth/app/refresh`, `/auth/app/logout` via `publicApi` with `withoutAuth`) use `publicApi`

#### Scenario: Chat stream and logout use Request with proper headers
- **WHEN** `chatStream(chatId)` is implemented
- **THEN** it uses `api.get("/chats/stream").header("Accept","text/event-stream").header("x-chat-id",chatId).timeout(Duration.ofMillis(0)).sendAsync(BodyHandlers.ofLines())` with a `SubmissionPublisher`; **WHEN** `logout(accessToken,refreshToken)` is implemented **THEN** it uses `publicApi.post("/auth/app/logout").withoutAuth().header("Authorization","Bearer "+accessToken).json(json).sendAsync()` to avoid refresh recursion

#### Scenario: Only API_URL endpoints remain
- **WHEN** all methods in `MindustryTool` are listed
- **THEN** every URL is relative (`/ping`, `/maps/...`) resolved against `Config.API_URL` or absolute `Config.*_URL` that equals `API_URL`; no GitHub or `PROJECT_URL` endpoints appear

### Requirement: Typed DTO returns via JsonUtils

`MindustryTool` methods SHALL return `CompletableFuture<DTO>` or `CompletableFuture<List<DTO>>` (or `CompletableFuture<byte[]>` for binary) by mapping `HttpResponse<String>` through `mindustrytool.utils.JsonUtils` instead of returning `CompletableFuture<String>`.

#### Scenario: Find map returns MapDetailData
- **WHEN** `MindustryTool.findMap(id)` is called
- **THEN** it calls `publicApi.get("/maps/"+id).sendAsync()` and maps `response.body()` via `JsonUtils.fromJson(MapDetailData.class, body)` to `CompletableFuture<MapDetailData>`

#### Scenario: Search maps returns List<MapData>
- **WHEN** `MindustryTool.searchMaps(page,size,sort,query,tags)` is called
- **THEN** it builds a paged URL and returns `CompletableFuture<List<MapData>>` via `JsonUtils.fromJsonArray(MapData.class, body)`

#### Scenario: Tags and users return typed lists
- **WHEN** `getTags` or `getUserBatch` is called
- **THEN** they return `CompletableFuture<List<TagCategory>>` / `List<UserData>` parsed from JSON, not raw string

#### Scenario: Binary endpoints remain byte[]
- **WHEN** `downloadMap` or `downloadSchematic` is called
- **THEN** they use `publicApi.get(...).timeout(LONG_TIMEOUT).sendAsync(BodyHandlers.ofByteArray())` and return `CompletableFuture<byte[]>`

### Requirement: No storage or translation endpoints

`MindustryTool` SHALL NOT contain storage (`/storage/*`) or translation (`/translations/translate`) endpoints.

#### Scenario: No storage methods
- **WHEN** `MindustryTool.java` is searched for `storageUrl`, `listStorageSlots`, `uploadFile`
- **THEN** no matches are found

#### Scenario: No translation methods
- **WHEN** `MindustryTool.java` is searched for `translate`
- **THEN** no method returns a translation endpoint

### Requirement: Import hygiene for MindustryTool

`MindustryTool` SHALL use explicit imports for `BodyHandlers`, `Duration`, `URLEncoder`, `StandardCharsets`, `Config`, `JsonUtils`, `models.*` and SHALL NOT use fully-qualified inline names for `old.*` and SHALL NOT `import old.mindustrytool.*` or `import static mindustrytool.services.Request.*`.

#### Scenario: No old imports and no static Request import
- **WHEN** `src/mindustrytool/services/MindustryTool.java` is inspected
- **THEN** it contains `import mindustrytool.services.Request`, `MindustryAuthProvider`, `AuthProvider` as needed, and no `import old.*`, no `import static ...Request.*`, and no `java.net.http.HttpRequest` direct construction

#### Scenario: Explicit model imports
- **WHEN** imports are listed
- **THEN** they include `ChannelDto`, `ChatMessage`, `ChatUser`, `AuthTokenResponse`, `LoginUriResponse`, `MapData`, `MapDetailData`, `ModData`, `PlayerConnectRoom/Provider`, `SchematicData/Detail`, `ServerData`, `TagCategory`, `UserData`, `UserSession` without wildcards

### Requirement: Retained endpoints stay complete with instance Request semantics

`MindustryTool` SHALL retain typed methods for ping, maps/schematics (find/download/search), tags, user batch, planets, servers, player-connect rooms/providers, chat (channels/messages/users/count/state/stream), auth (session/loginUri/pollLoginToken/logout/refreshToken), crash report, and images (schematic/map image) and implement them via `publicApi`/`api` with per-request `withoutAuth` where required.

#### Scenario: Auth session uses authenticated client automatically
- **WHEN** `getSession()` is called
- **THEN** it uses `api.get("/auth/session").sendAsync()` which triggers `refreshIfNeeded()` and Bearer header automatically

#### Scenario: Login/refresh use withoutAuth
- **WHEN** `getLoginUri()` / `pollLoginToken(loginId)` / `refreshToken(refreshToken)` are called
- **THEN** they use `publicApi.get/post(...).withoutAuth().json(...).sendAsync()` so they do not recurse

#### Scenario: Paged search helper remains
- **WHEN** `searchMaps`/`searchSchematics` build URLs
- **THEN** they use `buildPagedUrl` with `page`, `size<=100`, `sort`, `query`, `tags` encoded via `URLEncoder` with `StandardCharsets.UTF_8`
