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
Log.info("Server started successfully");
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

5. Use the translation key in code instead of hardcoding the text.

Example:

### `bundle.properties`

```properties
error.no-permission=You don't have permission.
ui.start-game=Start Game
ui.stop-game=Stop Game
```

### Java

```java
player.sendMessage(Core.bundle.get("error.no-permission"));
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
message.welcome=Welcome, {0}!
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
ui.confirm=Confirm
ui.cancel=Cancel

button.save=Save
button.delete=Delete

dialog.confirm-delete=Confirm Deletion

message.welcome=Welcome, {0}!

error.no-permission=You don't have permission.
error.not-found=The requested item was not found.

status.running=Running
status.stopped=Stopped
```

### Key Naming Rules

- Use meaningful names.
- Keep keys grouped by feature or purpose.
- Prefer existing naming conventions in the project.
- Do not create duplicate keys for the same concept.
- Do not use vague keys such as:

```text
text1
message2
label
button1
```

---

## What Must Be Translated

This rule applies to **all user-visible text**, including:

- Buttons
- Labels
- Menus
- Dialog titles
- Dialog descriptions
- Tooltips
- Notifications
- Chat messages
- Error messages
- Warning messages
- Status messages
- Command responses
- Form validation messages
- Empty states
- Loading messages
- Help text
- Settings descriptions
- Game messages
- Player-facing messages

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
message.player=Player: {0}
```

```java
Core.bundle.format("message.player", player.name);
```

---

## Exceptions

The following generally do **not** need translation:

- Internal logs not shown to users
- Debug messages
- Developer-only error messages
- Class names
- Method names
- Variable names
- Configuration keys
- API field names
- Database values
- Protocol messages
- Machine-readable strings

However, if the text can be displayed to a player or end user, it **must be translated**.

---

## Before Completing Any Task

Before finishing a task, the AI agent must verify:

- [ ] No new user-visible text is unnecessarily hardcoded.
- [ ] Existing translation keys were reused where appropriate.
- [ ] Every new display string has a translation key.
- [ ] New keys were added to `assets/bundles/bundle.properties`.
- [ ] Dynamic values use `Core.bundle.format()` where appropriate.
- [ ] Translation keys follow the project's naming conventions.
- [ ] No duplicate translation keys were introduced.

**A UI or player-facing feature is not considered complete until all of its display text has been properly added to the translation
bundle.**
