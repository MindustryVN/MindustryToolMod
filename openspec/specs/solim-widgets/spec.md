# solim-widgets Specification

## Purpose
TBD - created by archiving change create-solim-core. Update Purpose after archive.
## Requirements
### Requirement: Text widget with static and reactive content
`Text` SHALL display string content via `text(String)` and `text(Signal<String>|Computed<String>)` (reactive). It SHALL wrap `arc.scene.ui.Label` or equivalent and support `style` binding and modifiers.

#### Scenario: Static Text
- **WHEN** `text("Settings")` is called
- **THEN** a `Label` with "Settings" is created and added to current parent

#### Scenario: Reactive Text
- **WHEN** `Computed<String> t = count.map(v -> "Count: " + v)` and `text(t)` then `count.set(5)`
- **THEN** label text updates to "Count: 5" via binding without recreation

### Requirement: Image and Icon widgets
`Image` SHALL display `Drawable`/`TextureRegion` with `image(Drawable)` and `image(Signal<Drawable>)`. `Icon` SHALL display icon drawable with `icon(IconType)` and reactive overload. Both SHALL support size/style bindings.

#### Scenario: Image display
- **WHEN** `image(backgroundDrawable)` is called
- **THEN** an `Image` with that drawable is added

#### Scenario: Icon reactive
- **WHEN** `icon(darkMode.map(v -> v ? Icon.moon : Icon.sun))` and `darkMode` toggles
- **THEN** icon drawable updates via binding

### Requirement: Badge and Avatar lightweight components
`Badge` SHALL be a lightweight label/container for counts/status, `Avatar` SHALL display user image with fallback. Both may be convenience composites over `Text`/`Image` + `Container`.

#### Scenario: Badge count
- **WHEN** `badge(count.map(v -> v > 99 ? "99+" : String.valueOf(v)))` is used
- **THEN** badge text updates reactively

### Requirement: Button and IconButton with click handler and reactive props
`Button` SHALL support `button(String|Signal|Computed, Runnable onClick)` plus modifiers `.enabled(Signal<Boolean>)`, `.visible(Signal<Boolean>)`, `.style(Signal<Style>)`. `IconButton` SHALL be variant with icon drawable.

#### Scenario: Button click handler
- **WHEN** `button("Save", () -> save())` is clicked
- **THEN** `save()` is invoked

#### Scenario: Button reactive text and style
- **WHEN** `Computed<String> saveText = dirty.map(v -> v ? "● Save" : "Save")` and `button(saveText, onSave).enabled(dirty).style(Styles.PRIMARY)` then `dirty.set(true)`
- **THEN** button text, enabled, and style update via bindings; click only enabled when dirty true

#### Scenario: Button disabled via binding
- **WHEN** `button("Toggle").enabled(Signal.of(false))` is rendered
- **THEN** underlying `TextButton` is disabled

### Requirement: TextField and TextArea with Signal binding
`TextField` SHALL bind to `Signal<String>` via `textField(signal)` with two-way sync: typing updates signal, signal changes update field text (without cursor jump when possible). `TextArea` SHALL be multiline variant.

#### Scenario: Two-way TextField
- **WHEN** `Signal<String> input = Signal.of("")` and `textField(input)` is displayed and user types "hi"
- **THEN** `input.get()` becomes "hi"; when `input.set("hello")` programmatically, field text updates to "hello"

#### Scenario: TextArea multiline
- **WHEN** `textArea(description)` is used
- **THEN** a multiline `TextArea` is created bound to `description` signal

### Requirement: Checkbox, Switch, Slider, Select input widgets
`Checkbox` (`checkbox(String, Signal<Boolean>)`), `Switch` (`switch(Signal<Boolean>)`), `Slider` (`slider(Signal<Float>, min, max, step)`), `Select<T>` (`select(Signal<T>, List<T>)` or `select(Signal<T>, T... options)`) SHALL all support reactive value binding and change callbacks, delegating to Arc widgets.

#### Scenario: Checkbox binding
- **WHEN** `checkbox("Enable", enabled)` where `enabled = Signal.of(false)` and user toggles checkbox
- **THEN** `enabled.get()` toggles to true; when `enabled.set(false)` programmatically, checkbox shows unchecked

#### Scenario: Slider binding
- **WHEN** `slider(volume, 0f, 1f, 0.1f)` where `volume = Signal.of(0.5f)` and slider drags to 0.8
- **THEN** `volume.get()` updates to 0.8; programmatic `volume.set(0.2f)` moves slider thumb

#### Scenario: Select binding
- **WHEN** `select(selected, List.of("A","B","C"))` and user picks "B"
- **THEN** `selected.get()` becomes "B"; programmatic `selected.set("C")` updates displayed selection

### Requirement: Dialog and Popup overlay widgets
`Dialog` SHALL wrap Arc `BaseDialog`/`Dialog` with declarative content via `dialog(title, Runnable content)` and `show()`/`hide()` methods, using Arc native overlay. `Popup` SHALL be lightweight tooltip/context menu.

#### Scenario: Dialog show/hide
- **WHEN** `Dialog d = dialog("Confirm", () -> { text("Are you sure?"); row(() -> { button("Yes", d::hide); button("No", d::hide); }); }); d.show();`
- **THEN** dialog appears via Arc `show()` and content is built via parent stack inside dialog container

### Requirement: Spinner, ProgressBar, Alert feedback widgets
`Spinner` SHALL show loading indicator, `ProgressBar` SHALL bind to `Signal<Float>` progress (0..1), `Alert` SHALL show dismissible message with type (info/warning/error).

#### Scenario: ProgressBar reactive progress
- **WHEN** `progressBar(progress)` where `progress = Signal.of(0.3f)` then `progress.set(0.7f)`
- **THEN** progress bar visual updates to 70%

#### Scenario: Alert dismiss
- **WHEN** `alert("Saved!", AlertType.SUCCESS).show()` then close button clicked
- **THEN** alert is removed from parent

### Requirement: Widgets built on binding, layout, style foundations
All widgets SHALL reuse `Binding`/`Effect` for reactivity, `ParentStack` for declarative construction, and `Style` for styling; SHALL NOT reimplement reactive or layout logic per-widget.

#### Scenario: Widget thin wrapper
- **WHEN** `Button.java`/`Text.java` are inspected
- **THEN** they delegate to Arc `TextButton`/`Label` creation and use `Binding.of(...)` or `Effect.of(...)` for reactive props, not custom state loops

### Requirement: Prioritized implementation order
Widgets SHALL be implemented in order: `Text`, `Button`, then `TextField`, `Checkbox`, `Switch`, `Slider`, `Select` before `Image`/`Icon`, then Badge/Avatar, then Dialog/Popup, then Spinner/ProgressBar/Alert. Tests SHALL cover first batch before advanced controls.

#### Scenario: Text and Button tests first
- **WHEN** `gradle :solim:test` runs after initial implementation
- **THEN** `Text`/`Button` reactive binding tests pass before `Dialog` tests are required

