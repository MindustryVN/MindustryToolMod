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

All HTTP requests MUST be executed via a `mindustrytool.services.Request` instance (backed by pure Java 8 `HttpURLConnection`).

Do not construct `HttpURLConnection`, `URL.openConnection()`, or any HTTP client directly outside `Request.java`. All calls must go through `Request` — either via the existing facades `mindustrytool.services.MindustryTool` / `mindustrytool.services.Github`, or via a new class that owns a `Request` instance built with `Request.builder().baseUrl(...).timeout(...).authProvider(...).build()`.

❌ Bad:

```java
URL url = new URL("https://api.example.com/data");
HttpURLConnection conn = (HttpURLConnection) url.openConnection();
conn.setRequestMethod("GET");
```

```java
// Direct HTTP connection construction outside Request.java
var conn = (HttpURLConnection) new URL(url).openConnection();
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

## Nullability — Mandatory

**By default, all variables, fields, method parameters, and method return values are non-nullable.**

If a variable, field, method parameter, or method return value can be null, you **MUST** annotate it with `@Nullable` (from `arc.util.Nullable`).

Never use `javax.annotation.Nullable`, `org.jetbrains.annotations.Nullable`, or other third-party annotations. Always use `arc.util.Nullable`.

❌ Bad:

```java
// Method return can be null, but lacks @Nullable
public Dialog getSettingDialog() {
    return null;
}

// Parameter can be null, but lacks @Nullable
public void process(String value) {
    if (value != null) { ... }
}

// Field can be null, but lacks @Nullable
private String cachedToken;
```

✅ Good:

```java
import arc.util.Nullable;

public @Nullable Dialog getSettingDialog() {
    return null;
}

public void process(@Nullable String value) {
    if (value != null) { ... }
}

private @Nullable String cachedToken;
```

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
* [ ] All HTTP calls go through `mindustrytool.services.Request` (via `MindustryTool`/`Github` or an owned `Request` instance); no direct HTTP connection construction outside `Request.java`.
* [ ] Java 8 runtime compatibility verified: no Java 9+ standard library APIs or methods (e.g., `List.of`, `Set.of`, `Map.of`, `Stream.toList`, `String.isBlank`, `Optional.isEmpty`) are used.
* [ ] Nullability verified: all variables, fields, parameters, and method return types that can be null are annotated with `@Nullable` (from `arc.util.Nullable`).

**A UI or player-facing feature is not considered complete until all of its display text has been properly added to the translation bundle with sufficient context for translators.**

---

# Architecture & Coding Rules

## Communication

* Always address the user as **Sir**.
* Be concise and direct unless Sir requests detailed explanation.
* Point out architectural problems instead of blindly implementing bad designs.

---

# Project Context

This project is a **Mindustry game mod**, not a backend application.

Do not apply backend architecture patterns by default.

Avoid introducing unnecessary concepts such as:

* Repository layers
* DAO layers
* Service layers for trivial logic
* Dependency injection frameworks
* Enterprise-style abstractions
* Request/response architecture
* Controller patterns
* Database-oriented architecture

Use architecture appropriate for a game mod:

```text
Feature
├── UI
├── Game logic
├── State
├── Events
└── Utilities
```

Organize code primarily around **features and game functionality**, not artificial technical layers.

---

# SOLID Principles — Pragmatic Usage

Follow SOLID principles where they improve the code.

Do not apply SOLID mechanically.

## Single Responsibility

Classes should have a clear responsibility.

For example:

```text
FeatureSettingsView
→ Feature settings UI

FeatureManager
→ Feature registration and lifecycle

FeatureCard
→ Rendering one feature

FeatureState
→ Shared feature state, when necessary
```

Do not split a simple feature into many layers without a real reason.

---

## Avoid Enterprise Architecture

Do not automatically create:

```text
FeatureRepository
FeatureRepositoryImpl
FeatureService
FeatureServiceImpl
FeatureController
FeatureDTO
FeatureMapper
```

This is a game mod.

Prefer direct and understandable code.

If a manager, registry, event system, or utility is sufficient, use that.

---

## Interfaces

Do not create interfaces for every class.

Create an interface when there is a meaningful behavioral contract or multiple implementations.

Good:

```java
Component
Disposable
Feature
```

Bad:

```java
FeatureManagerInterface
FeatureManagerImpl
```

unless multiple implementations are genuinely required.

---

# Solim UI Philosophy

## Declarative by Default

Application UI should be written declaratively.

Prefer:

```java
@Override
protected Element build() {
    return column(() -> {
        header();
        content();
        footer();
    });
}
```

Avoid manually constructing large imperative UI trees:

```java
Table root = new Table();

root.add(...);
root.row();
root.add(...);
```

unless Solim does not provide the required capability.

---

# Automatic Binding Through Components

## The User Must Not Bind Elements Manually

This is a core Solim rule.

Application developers should **not** manually create bindings:

```java
signal.bind(value -> {
    label.setText(value);
});
```

or:

```java
Effect.of(() -> {
    label.setText(signal.get());
});
```

for normal UI usage.

The **component itself** must handle reactive binding automatically.

The developer should declare reactive values:

```java
text(name);
```

not:

```java
Label label = new Label();

name.bind(value -> {
    label.setText(value);
});
```

---

## Components Accept Reactive Values

Solim components should support both static and reactive values.

For example:

```java
text("Hello");
```

and:

```java
text(username);
```

where:

```java
Signal<String> username
```

or:

```java
Readable<String> username
```

Both should work.

Internally:

```text
Component API
      ↓
Detect static or Readable value
      ↓
Component installs binding automatically
      ↓
Component owns binding
      ↓
Signal changes
      ↓
Arc element updates
```

The user should only describe what they want.

---

## Example: Automatic Text Binding

Desired user API:

```java
text(featureName);
```

Where:

```java
Readable<String> featureName;
```

Solim internally handles:

```text
featureName changes
        ↓
Label text updates
```

The application code does not manually subscribe.

---

## Example: Automatic Style Binding

Desired API:

```java
button()
    .color(enabled.map(value ->
        value ? Color.green : Color.scarlet
    ));
```

The developer declares the value.

Solim handles:

```text
Readable<Color>
        ↓
Internal component binding
        ↓
Button color updates
```

No manual `Effect`.

No manual listener.

No manual disposal.

---

## Example: Automatic Width Binding

Desired:

```java
card()
    .width(cardWidth);
```

or:

```java
card()
    .width(cardWidth.map(width -> width - 10f));
```

Solim automatically:

```text
cardWidth changes
        ↓
Element width updates
        ↓
Element invalidates layout
```

The user should never manually write:

```java
Effect.of(...)
```

for ordinary component properties.

---

# Binding Ownership

Every binding created by a Solim component must automatically belong to the current component.

Example:

```java
text(username);
```

Internally:

```text
FeatureCard
    │
    ├── Label
    │
    └── Text Binding
```

When:

```text
FeatureCard.dispose()
```

happens:

```text
Label binding disposed automatically
```

Application code must not manually dispose bindings.

---

# Property Reactivity

Use automatic component bindings for simple property updates.

Examples:

* Text
* Color
* Width
* Height
* Visibility
* Enabled
* Disabled
* Drawable
* Style values

Desired application code:

```java
text(status);
```

```java
visible(isVisible);
```

```java
color(colorSignal);
```

```java
width(sizeSignal);
```

Solim handles the subscriptions internally.

---

# Effects Are Only for Side Effects

Keep `Effect` support.

However, `Effect` is for non-UI side effects.

Good:

```java
effect(() -> {
    Log.info("Feature enabled: " + enabled.get());
});
```

Good:

```java
effect(() -> {
    saveConfig();
});
```

Do not use effects for normal UI properties:

```java
Effect.of(() -> {
    label.setText(name.get());
});
```

That behavior belongs inside the `Text` component.

---

# Structural Reactivity

Property binding updates existing elements.

Structural changes are different.

Use structural Solim components for:

* Dynamic lists
* Dynamic children
* Conditional UI
* Keyed collections

Example:

```java
reactiveGrid(
    features,
    Feature::id,
    feature -> new FeatureCard(feature)
);
```

Solim handles:

```text
Item added
→ Create component

Item removed
→ Remove element
→ Dispose component

Existing key
→ Reuse component

Order changed
→ Reorder existing elements
```

Do not manually:

```java
table.clear();
```

and recreate everything when keyed structural reactivity can handle it.

---

# Build Once, Bind Automatically

Normal component lifecycle:

```text
build()
   ↓
Create Arc elements once
   ↓
Components automatically install bindings
   ↓
Signals update existing elements
```

Avoid:

```text
Signal changes
   ↓
Rebuild entire component
```

unless the actual structure changed.

---

# No Manual Binding API Required for Normal UI

The public Solim developer experience should prioritize this:

```java
text(name);

button("Save")
    .visible(canSave)
    .enabled(canSave);

image(imageUrl)
    .width(imageWidth);
```

Instead of:

```java
Label label = ...;

name.bind(label::setText);

canSave.bind(button::setVisible);
```

Solim should hide subscription mechanics.

The framework owns the reactive complexity.

---

# Automatic Component Ownership

Application code should not manually register ownership.

Avoid:

```java
own(...)
ownChild(...)
scope.own(...)
```

Components created during `build()` should automatically be owned.

Example:

```java
@Override
protected Element build() {
    return column(() -> {

        new FeatureCard(featureA);

        new FeatureCard(featureB);

    });
}
```

The framework automatically tracks:

```text
Parent Component
    │
    ├── FeatureCard A
    └── FeatureCard B
```

When the parent is disposed, children are disposed automatically.

---

# Automatic Resource Ownership

Solim-created resources must automatically belong to their creating component.

This includes:

* Child components
* Reactive bindings
* Effects
* Solim event listeners
* Solim input controls
* Structural reactive children

Application developers should not manually track these resources.

---

# Mindustry and Arc API Rules

## Do Not Add Unnecessary Null Checks

**Any static value from `Core.*` and `Vars.*` is always non-null at runtime — you do not have to check for null.**

Mindustry and Arc runtime APIs that are guaranteed to exist during normal mod execution should be used directly.

Do not write:

```java
if (Core.app != null) {
    Core.app.post(...);
}
```

```java
if (Core.bundle != null) {
    return Core.bundle.get("key");
}
```

Use:

```java
Core.app.post(...);
```

```java
return Core.bundle.get("key");
```

Do not wrap normal runtime APIs with defensive null checks.

Examples include normal mod runtime usage of:

```java
Core.app
Core.graphics
Core.scene
Core.bundle
Core.atlas
Core.settings
Core.camera
Vars.ui
Vars.player
Vars.state
Vars.world
Vars.control
```

Any static value from `Core.*` and `Vars.*` is always non-null during mod runtime execution.

---

## Only Check Null When Null Is Actually Possible

Use null checks for:

* Explicitly nullable API values
* Optional feature implementations
* User-provided values
* Lifecycle states where initialization is genuinely uncertain

Do not check everything defensively.

Null checks should communicate a real possibility, not hide uncertainty.

---

# Do Not Wrap Mindustry APIs Without Value

Do not create wrappers that merely rename APIs.

Bad:

```java
public static void addToScene(Element element) {
    Core.scene.add(element);
}
```

Bad:

```java
public static boolean openUri(String uri) {
    return Core.app.openURI(uri);
}
```

Wrap Mindustry or Arc APIs only when Solim provides meaningful functionality such as:

* Declarative integration
* Automatic lifecycle ownership
* Reactive binding
* Reusable UI behavior

---

# Game Mod Architecture

Prefer feature-oriented organization.

Example:

```text
features/
├── settings/
│   ├── FeatureSettingDialog
│   ├── FeatureSettingsView
│   └── FeatureCard
│
├── chat/
│   ├── ChatFeature
│   └── ChatView
│
└── server/
    ├── ServerFeature
    └── ServerDialog
```

Shared functionality can live in:

```text
core/
signal/
ui/
utils/
```

Do not force everything into:

```text
controllers/
services/
repositories/
dto/
```

unless the project genuinely needs those concepts.

---

# One Source of Truth

Do not duplicate state.

Bad:

```text
boolean enabled
+
Signal<Boolean> enabledSignal
```

unless synchronization is explicitly required.

Prefer one reactive source:

```java
private final Signal<Boolean> enabled =
    Signal.of(false);
```

Then expose normal and reactive access:

```java
public boolean isEnabled() {
    return enabled.get();
}

public void setEnabled(boolean value) {
    enabled.set(value);
}

public Readable<Boolean> enabled() {
    return enabled;
}
```

This allows:

```java
if (feature.isEnabled()) {
    ...
}
```

and:

```java
visible(feature.enabled());
```

without duplicate state.

---

# Performance Philosophy

Solim uses Arc's retained UI tree.

Do not introduce:

* Virtual DOM
* Global diffing
* Full render reconciliation

Instead:

```text
Property changes
→ Update existing Arc element

Structural changes
→ Targeted keyed reconciliation
```

Preserve existing components whenever possible.

---

# Core Philosophy

Follow this order:

```text
Correctness
    ↓
Simple game-mod architecture
    ↓
Clear ownership
    ↓
Declarative Solim components
    ↓
Automatic reactive bindings
    ↓
Localized structural reactivity
    ↓
Performance optimization
```

The developer should primarily write:

```text
What the UI is
```

not:

```text
How to subscribe
How to update elements
How to dispose bindings
How to mount children manually
```

Solim is responsible for those mechanics.

---

# Testing Rules

## UI Testing — Solim vs Mod

* **Always: you do NOT have to write UI tests for the mod (`mod/` module).** The mod runs inside Mindustry's engine where scene, atlas, graphics, skins, fonts, and game state are initialized at runtime. Headless unit tests for mod UI components are prone to mock/skin failures and are explicitly NOT required.
* **Only Solim needs UI tests.** The Solim UI framework (`solim/` module) is where UI primitives, reactive bindings, components, layouts, and signal pipelines must be tested.

