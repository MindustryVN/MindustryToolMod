# solim-binding Specification

## Purpose
TBD - created by archiving change create-solim-core. Update Purpose after archive.
## Requirements
### Requirement: Widgets support static and reactive values
Widget factory methods (e.g., `text(...)`, `button(...)`, `visible(...)`, `enabled(...)`) SHALL accept both plain values (`String`, `boolean`) and reactive values (`Signal<T>`, `Computed<T>`). Reactive overloads SHALL apply current value immediately and update on change.

#### Scenario: Static text
- **WHEN** `text("Hello")` is called inside a parent
- **THEN** a `Label` with "Hello" is added and no subscription is created

#### Scenario: Reactive text immediate apply
- **WHEN** `Signal<String> username = Signal.of("Alice")` and `text(username)` is called
- **THEN** resulting `Label` text is "Alice" immediately after construction

#### Scenario: Reactive text future updates
- **WHEN** `username.set("Bob")` after binding
- **THEN** `Label` text automatically updates to "Bob" without rebuilding the widget

### Requirement: Binding updates Arc element directly, no rebuild
Reactive bindings SHALL update the underlying Arc `Element` property via setter (e.g., `label.setText(...)`, `button.setDisabled(...)`) and SHALL NOT rebuild the widget or recreate the `Element`.

#### Scenario: No Element recreation on change
- **WHEN** reactive `text` binding updates due to signal change
- **THEN** `label` instance identity remains same (`==` check passes) and only its property changed

### Requirement: Binding is disposable and tracks subscription
Each reactive binding SHALL create an internal `Subscription`/`Effect` that is disposable. Widget wrappers SHALL expose or internally hold this subscription and dispose on `Component.dispose()` or when element is removed.

#### Scenario: Binding dispose stops updates
- **WHEN** `Binding<String> b = Binding.of(label::setText, username)` then `b.dispose()` then `username.set("Charlie")`
- **THEN** `label` text remains previous value

#### Scenario: Component dispose disposes bindings
- **WHEN** component creates `text(username)` binding and then `component.dispose()` is called
- **THEN** the binding subscription is disposed and no longer reacts

### Requirement: Common reactive properties
Widgets SHALL support reactive bindings for at least: `text` (String), `visible` (boolean), `enabled`/`disabled` (boolean), `style` (Style), and `checked` (for Checkbox/Switch). Additional widget-specific bindings (e.g., `value` for TextField/Slider, `progress` for ProgressBar) follow same pattern.

#### Scenario: Visible binding
- **WHEN** `button("Save").visible(isLoggedIn)` where `isLoggedIn = Signal.of(false)` and later `isLoggedIn.set(true)`
- **THEN** button visibility toggles via `element.setVisible(...)` or Arc equivalent

#### Scenario: Enabled binding
- **WHEN** `button(saveText, onClick).enabled(dirty)` where `dirty = Signal.of(false)` then `dirty.set(true)`
- **THEN** button enabled state updates via `button.setDisabled(!dirty.get())`

### Requirement: Binding helper and Effect integration
`Binding` utility SHALL be implementable via `Effect.of(() -> target.set(prop.get()))` or direct `Subscription`; both are valid. Implementation SHALL prefer `Effect` where multi-dependency computed is involved, or `Subscription` for single signal.

#### Scenario: Computed text binding
- **WHEN** `Computed<String> saveText = dirty.map(v -> v ? "● Save" : "Save")` and `button(saveText, ...)` is bound
- **THEN** binding correctly tracks `dirty` through `saveText` computed and updates on dirty change

### Requirement: No string concatenation for dynamic text
Dynamic text SHALL use bundle formatting or `Computed` mapping, not manual `Core.bundle.get(...) + value` concatenation inside binding.

#### Scenario: Bundle formatted binding
- **WHEN** display needs `Core.bundle.format("message.player", name.get())`
- **THEN** it is expressed as `Signal.computed(() -> Core.bundle.format("message.player", name.get()))` and bound as reactive text

