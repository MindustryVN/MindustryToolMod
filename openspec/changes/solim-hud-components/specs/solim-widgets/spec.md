## MODIFIED Requirements

### Requirement: Button and IconButton with click handler and reactive props
`Button` and `IconButton` SHALL support `button(String|Signal|Computed|Readable, Runnable onClick)` and `iconButton(Drawable, Runnable onClick)` / `iconButton(Drawable, ImageButtonStyle, Runnable onClick)` plus chained modifiers `.enabled(Readable<Boolean>)`, `.visible(Readable<Boolean>)`, `.size(float)`, `.tooltip(String)`, `.tooltip(Readable<String>)`, `.onLongClick(Runnable)`, `.onLongClick(long, Runnable)`, `.style(...)`, and `.stopClickPropagation()`. When a long click triggers, regular `onClick` SHALL be suppressed. Reactive bindings SHALL be managed internally by the component.

#### Scenario: Button click handler
- **WHEN** `button("Save", () -> save())` is clicked
- **THEN** `save()` is invoked

#### Scenario: Button reactive text and style
- **WHEN** `Computed<String> saveText = dirty.map(v -> v ? "● Save" : "Save")` and `button(saveText, onSave).enabled(dirty).style(Styles.PRIMARY)` then `dirty.set(true)`
- **THEN** button text, enabled, and style update via bindings; click only enabled when dirty true

#### Scenario: Button long click handler
- **WHEN** a user holds a button configured with `.onLongClick(onLongPressAction)` for >= 300ms
- **THEN** `onLongPressAction` is executed and regular `onClick` is suppressed upon release

#### Scenario: Button short click does not trigger long press
- **WHEN** a user taps and releases a button configured with both `.onClick(clickAction)` and `.onLongClick(longAction)` in < 300ms
- **THEN** `clickAction` is executed and `longAction` is not triggered

#### Scenario: Reactive tooltip on Button
- **WHEN** `button("Action", () -> {}).tooltip(tooltipSignal)` is declared and `tooltipSignal` emits a new string
- **THEN** the button's tooltip text updates to reflect the new string value
