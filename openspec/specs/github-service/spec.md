# github-service Specification

## Purpose
External GitHub/project API facade that delegates all HTTP to instance-based `Request` clients without authentication.

## Requirements
### Requirement: Github service owns only external endpoints and delegates to instance Request

`Github` SHALL own only `Config.GITHUB_API_URL`, `Config.MOD_HJSON_URL`, and `Config.PROJECT_URL` endpoints, delegate every call to instance `Request` (`githubApi`, `projectApi`, `rawApi` built via `Request.builder()`) and contain no direct `HttpClient` construction and no auth provider.

#### Scenario: Github delegates to instance Request
- **WHEN** `src/mindustrytool/services/Github.java` is inspected
- **THEN** it declares `private static final Request githubApi = Request.builder().baseUrl(Config.GITHUB_API_URL).build()`, `projectApi` for `Config.PROJECT_URL`, and `rawApi` for absolute URLs; each method calls `githubApi.get("").sendAsync()` or `rawApi.get(Config.MOD_HJSON_URL).sendAsync()` / `rawApi.get(Config.GITHUB_API_URL + "?page=" + page + "...").sendAsync()` and maps `HttpResponse<String>.body()` via `JsonUtils` or returns String, with no `HttpClient`/`HttpRequest` instantiation

#### Scenario: No MindustryTool API URLs in Github
- **WHEN** `Github.java` URLs are listed
- **THEN** none start with `Config.API_URL`; all use `GITHUB_API_URL`, `MOD_HJSON_URL`, or `PROJECT_URL`

#### Scenario: No auth provider and no old imports
- **WHEN** `Github.java` is inspected
- **THEN** all three `Request` instances are built without `.authProvider(...)` and the file contains no `import old.*`

### Requirement: Typed returns for Github and project calls

`Github` methods SHALL return typed futures where applicable (e.g. release list parsed to DTO) instead of raw string where a model exists, with fallback to `String` for raw `mod.hjson`.

#### Scenario: getReleases returns String (raw) with rationale
- **WHEN** `Github.getReleases()` is called
- **THEN** it delegates to `githubApi.get("").sendAsync()` or `rawApi.get(GITHUB_API_URL + "?page=...").sendAsync()` and returns `CompletableFuture<String>` (raw mod.hjson/releases are heterogeneous and kept as String with Javadoc rationale)

#### Scenario: getProjectTasks returns typed response
- **WHEN** `Github.getProjectTasks(status)` is called
- **THEN** it calls `projectApi.get("/api/v1/projects/" + Config.PROJECT_ID + "/tasks?status=" + status).timeout(Duration.ofMillis(20000)).sendAsync()` and parses via `JsonUtils.fromJson(TaskResponse.class, body)`

### Requirement: Import hygiene for Github

`Github` SHALL use explicit imports for `Duration`, `CompletableFuture`, `mindustrytool.Config`, `mindustrytool.utils.JsonUtils`, `mindustrytool.models.TaskResponse` and SHALL NOT use fully-qualified inline names or `import static mindustrytool.services.Request.*`.

#### Scenario: No static Request import
- **WHEN** `Github.java` imports are inspected
- **THEN** they contain `import mindustrytool.services.Request` and no `import static ...Request.*`, and no `import old.*`

#### Scenario: Explicit timeout handling
- **WHEN** `getProjectTasks` needs 20s timeout
- **THEN** it uses `.timeout(Duration.ofMillis(20_000))` on the builder, not `int timeoutMs` parameter on static helper
