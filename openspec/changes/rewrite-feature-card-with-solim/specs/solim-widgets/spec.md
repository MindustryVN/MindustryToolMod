## MODIFIED Requirements

### Requirement: Text widget with static and reactive content
`Text` SHALL display string content via `text(String)` and `text(Readable<String>)` (reactive). It SHALL wrap `arc.scene.ui.Label` and provide fluent chained property modifiers including `.color(Color)`, `.color(Readable<Color>)`, `.style(LabelStyle)`, `.wrap(boolean)`, `.ellipsis(boolean)`, `.fontScale(float)`, and text alignment (`.left()`, `.center()`, `.right()`). Reactive bindings SHALL be managed internally by the component lifecycle.

#### Scenario: Static Text
- **WHEN** `text("Settings")` is called
- **THEN** a `Label` with "Settings" is created and added to current parent

#### Scenario: Reactive Text
- **WHEN** `Computed<String> t = count.map(v -> "Count: " + v)` and `text(t)` then `count.set(5)`
- **THEN** label text updates to "Count: 5" via binding without recreation

#### Scenario: Chained text styling and layout modifiers
- **WHEN** `text("Description").wrap(true).ellipsis(true).color(Color.lightGray).fontScale(0.9f)` is declared
- **THEN** the underlying `Label` has word-wrapping, ellipsis truncation, light gray color, and 0.9 font scale configured directly

#### Scenario: Reactive text color binding
- **WHEN** `text("Status").color(statusColorReadable)` is declared and the status color changes
- **THEN** the label's color updates immediately via internal component binding without external `Binding` calls

### Requirement: Button and IconButton with click handler and reactive props
`Button` and `IconButton` SHALL support `button(String|Signal|Computed|Readable, Runnable onClick)` and `iconButton(Drawable, Runnable onClick)` / `iconButton(Drawable, ImageButtonStyle, Runnable onClick)` plus chained modifiers `.enabled(Readable<Boolean>)`, `.visible(Readable<Boolean>)`, `.size(float)`, `.tooltip(String)`, `.style(...)`, and `.stopClickPropagation()`. Reactive bindings SHALL be managed internally by the component.

#### Scenario: Button click handler
- **WHEN** `button("Save", () -> save())` is clicked
- **THEN** `save()` is invoked

#### Scenario: Button reactive text and style
- **WHEN** `Computed<String> saveText = dirty.map(v -> v ? "● Save" : "Save")` and `button(saveText, onSave).enabled(dirty).style(Styles.PRIMARY)` then `dirty.set(true)`
- **THEN** button text, enabled, and style update via bindings; click only enabled when dirty true

#### Scenario: Button disabled via binding
- **WHEN** `button("Toggle").enabled(Signal.of(false))` is rendered
- **THEN** underlying `TextButton` is disabled

#### Scenario: IconButton creation and event stop propagation
- **WHEN** `iconButton(Icon.settings, Styles.clearNonei, onSettings).size(32f).tooltip("Settings")` is placed inside a clickable parent and clicked
- **THEN** the icon button executes `onSettings` and stops event bubbling to prevent triggering parent click handlers
