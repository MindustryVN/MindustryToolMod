# AGENTS.md

## Internationalization (i18n) — Mandatory

**All user-visible text must be translatable.**

Never introduce hardcoded display text into the codebase unless it is explicitly intended to be non-user-facing data.

The primary translation bundle is:

```text
assets/bundles/bundle.properties
```

---

## Core Rules

### Never hardcode user-visible text

Do not write display text directly in Java code.

❌ Bad:

```java
player.sendMessage("You don't have permission.");
```

```java
button.setText("Start Game");
```

✅ Good:

```java
player.sendMessage(Core.bundle.get("error.no-permission"));
```

```java
button.setText(Core.bundle.get("ui.start-game"));
```

---

## Adding New Text

Whenever new user-visible text is introduced:

1. Check whether an existing translation key already represents the same text.
2. Reuse the existing key if possible.
3. Otherwise, create a new meaningful key.
4. Add the English/default translation to:

```text
assets/bundles/bundle.properties
```

5. **Always add a descriptive comment directly above every new translation key.**
6. Use the translation key in code instead of hardcoding the text.

---

## Translation Comments — Mandatory

Every translation key added to `assets/bundles/bundle.properties` **must have a comment directly above it** explaining:

* What the text means.
* Where the text is displayed.
* When the key should be used.
* Any important context needed by translators.
* The meaning of placeholders such as `{0}`, `{1}`, etc., when applicable.

These comments exist to help translators understand the context and choose accurate translations.

### Basic Example

```properties
# Displayed on the confirmation button when the user confirms an action.
# Use for generic confirmation actions.
ui.confirm=Confirm
```

### Context-Specific Example

```properties
# Displayed when a player attempts an action without the required permission.
# Used in player-facing error messages.
error.no-permission=You don't have permission.
```

### Dynamic Text Example

```properties
# Welcome message displayed to a player.
# {0} is the player's display name.
message.welcome=Welcome, {0}!
```

### Multiple Parameters Example

```properties
# Displays a player's current score.
# {0} is the player's name.
# {1} is the player's score.
message.player-score={0} has {1} points.
```

### Important Rules for Comments

* The comment must be **directly above the key** it describes.
* Do not place unrelated comments between the comment and its key.
* Write comments in clear English.
* Describe the **usage and context**, not just repeat the text.
* Include placeholder descriptions when the value contains parameters.
* Avoid vague comments such as:

```properties
# Save
button.save=Save
```

Prefer:

```properties
# Displayed on a button that saves the current configuration or changes.
button.save=Save
```

### Group Comments vs Key Comments

Group comments may be used for organization, but they **do not replace the required comment for each key**.

❌ Not sufficient:

```properties
# General buttons
button.save=Save
button.cancel=Cancel
button.delete=Delete
```

✅ Correct:

```properties
# Displayed on a button that saves the current configuration or changes.
button.save=Save

# Displayed on a button that cancels the current operation without applying changes.
button.cancel=Cancel

# Displayed on a button that permanently deletes the selected item.
button.delete=Delete
```

---

## Dynamic Text and Parameters

For text containing dynamic values, use bundle formatting instead of string concatenation.

❌ Bad:

```java
player.sendMessage("Welcome, " + player.name + "!");
```

❌ Bad:

```java
player.sendMessage("You have " + coins + " coins.");
```

✅ Good:

### `bundle.properties`

```properties
# Welcome message displayed to a player.
# {0} is the player's display name.
message.welcome=Welcome, {0}!

# Displays the number of coins currently owned by the player.
# {0} is the number of coins.
message.coins=You have {0} coins.
```

### Java

```java
player.sendMessage(Core.bundle.format("message.welcome", player.name));
```

```java
player.sendMessage(Core.bundle.format("message.coins", coins));
```

---

## Translation Key Naming

Use lowercase dot-separated keys.

Preferred structure:

```text
ui.*
button.*
menu.*
dialog.*
message.*
error.*
warning.*
status.*
command.*
setting.*
```

Examples:

```properties
# Displayed on a generic confirmation button.
ui.confirm=Confirm

# Displayed on a generic cancel button.
ui.cancel=Cancel

# Displayed on a button that saves the current changes.
button.save=Save

# Displayed on a button that permanently deletes the selected item.
button.delete=Delete

# Title of a dialog asking the user to confirm deletion.
dialog.confirm-delete=Confirm Deletion

# Welcome message displayed to a player.
# {0} is the player's display name.
message.welcome=Welcome, {0}!

# Displayed when a user attempts an action without sufficient permission.
error.no-permission=You don't have permission.

# Displayed when the requested item cannot be found.
error.not-found=The requested item was not found.

# Indicates that a service, server, or process is currently running.
status.running=Running

# Indicates that a service, server, or process has stopped.
status.stopped=Stopped
```

### Key Naming Rules

* Use meaningful names.
* Keep keys grouped by feature or purpose.
* Prefer existing naming conventions in the project.
* Do not create duplicate keys for the same concept.
* Do not use vague keys such as:

```text
text1
message2
label
button1
```

---

## What Must Be Translated

This rule applies to **all user-visible text**, including:

* Buttons
* Labels
* Menus
* Dialog titles
* Dialog descriptions
* Tooltips
* Notifications
* Chat messages
* Error messages
* Warning messages
* Status messages
* Command responses
* Form validation messages
* Empty states
* Loading messages
* Help text
* Settings descriptions
* Game messages
* Player-facing messages

---

## Core.bundle Usage

Use:

```java
Core.bundle.get("translation.key");
```

for static text.

Use:

```java
Core.bundle.format("translation.key", value1, value2);
```

for text containing dynamic values.

Do not manually concatenate translated text when formatting can be used.

❌ Bad:

```java
Core.bundle.get("message.player") + player.name;
```

✅ Good:

```properties
# Displays a player's name.
# {0} is the player's display name.
message.player=Player: {0}
```

```java
Core.bundle.format("message.player", player.name);
```

---

## Exceptions

The following generally do **not** need translation:

* Internal logs not shown to users
* Debug messages
* Developer-only error messages
* Class names
* Method names
* Variable names
* Configuration keys
* API field names
* Database values
* Protocol messages
* Machine-readable strings

However, if the text can be displayed to a player or end user, it **must be translated**.

---

## HTTP Client — Mandatory

All HTTP requests MUST be executed via a `mindustrytool.services.Request` instance.

Do not construct `java.net.http.HttpClient` or `java.net.http.HttpRequest` directly outside `Request.java`. All calls must go through `Request` — either via the existing facades `mindustrytool.services.MindustryTool` / `mindustrytool.services.Github`, or via a new class that owns a `Request` instance built with `Request.builder().baseUrl(...).timeout(...).authProvider(...).build()`.

❌ Bad:

```java
HttpClient client = HttpClient.newHttpClient();
HttpRequest req = HttpRequest.newBuilder(URI.create("https://api.example.com/data")).GET().build();
client.sendAsync(req, BodyHandlers.ofString());
```

```java
// HttpClient/HttpRequest construction outside Request.java
var request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofSeconds(10)).build();
```

✅ Good:

```java
private final Request api = Request.builder()
        .baseUrl(Config.API_URL)
        .timeout(Duration.ofSeconds(10))
        .authProvider(authProvider)
        .build();

api.get("/maps/1").sendAsync().thenApply(r -> JsonUtils.fromJson(MapData.class, r.body()));
```

```java
// Via facades that delegate to Request internally
MindustryTool.getSession();
Github.getReleases();
```

---

## Java Compatibility — Mandatory

**This project uses Java 17 syntax running in a Java 8 runtime environment.**

### Language Features vs Runtime APIs

* **Java 17 Language Syntax is allowed**:
  You may use Java 17 syntax supported by the compiler and desugaring toolchain, such as `var`, switch expressions, text blocks, etc.

* **Java 8 Runtime APIs only**:
  The application runs in a Java 8 runtime environment (including Mindustry JRE and Android runtime). You **MUST NOT** use standard library classes or methods introduced in Java 9 or later unless provided by an included library or backport.

### Common Pitfalls & Replacements

| Feature | ❌ Do NOT use (Java 9+) | ✅ Use instead (Java 8 compatible) |
|---|---|---|
| Immutable Collections | `List.of(...)`, `Set.of(...)`, `Map.of(...)` | `Arrays.asList(...)`, `new HashSet<>(...)`, `Collections.unmodifiableList(...)`, or Arc's `Seq.with(...)` |
| Stream to List | `stream.toList()` | `stream.collect(Collectors.toList())` |
| String Checks | `str.isBlank()`, `str.strip()` | `str.trim().isEmpty()`, `arc.util.Strings.isEmpty(...)` |
| Optional | `opt.isEmpty()` | `!opt.isPresent()` |
| Stream Predicate | `Predicate.not(...)` | Lambda `x -> !condition(x)` |
| Stream Drop/Take | `stream.takeWhile(...)`, `stream.dropWhile(...)` | Java 8 stream filters or standard loops |
| Input Stream | `in.readAllBytes()`, `in.transferTo(...)` | Byte buffers, `Streams.copy(...)`, or Java 8 loop |
| File IO | `Files.readString(...)`, `Files.writeString(...)` | Arc's `Fi` utilities (`fi.readString()`), or Java 8 `BufferedReader` / `BufferedWriter` |

---

## Before Completing Any Task

Before finishing a task, the AI agent must verify:

* [ ] No new user-visible text is unnecessarily hardcoded.
* [ ] Existing translation keys were reused where appropriate.
* [ ] Every new display string has a translation key.
* [ ] Every translation key has a descriptive comment directly above it.
* [ ] Comments explain the purpose and usage context of the key.
* [ ] All placeholders such as `{0}` and `{1}` are explained in comments.
* [ ] New keys were added to `assets/bundles/bundle.properties`.
* [ ] Dynamic values use `Core.bundle.format()` where appropriate.
* [ ] Translation keys follow the project's naming conventions.
* [ ] No duplicate translation keys were introduced.
* [ ] All HTTP calls go through `mindustrytool.services.Request` (via `MindustryTool`/`Github` or an owned `Request` instance); no direct `HttpClient`/`HttpRequest` construction outside `Request.java`.
* [ ] Java 8 runtime compatibility verified: no Java 9+ standard library APIs or methods (e.g., `List.of`, `Set.of`, `Map.of`, `Stream.toList`, `String.isBlank`, `Optional.isEmpty`) are used.

**A UI or player-facing feature is not considered complete until all of its display text has been properly added to the translation bundle with sufficient context for translators.**
