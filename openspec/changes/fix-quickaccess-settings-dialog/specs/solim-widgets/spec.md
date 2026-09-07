## MODIFIED Requirements

### Requirement: Checkbox, Switch, Slider, Select input widgets
`Checkbox` (`checkbox(String, Signal<Boolean>)`, `checkbox(String, boolean, Consumer<Boolean>)`), `Switch` (`switch(Signal<Boolean>)`), `Slider` (`slider(Signal<Float>, min, max, step)`, `slider(Signal<Integer>, min, max, step)`), `Select<T>` (`select(Signal<T>, List<T>)` or `select(Signal<T>, T... options)`) SHALL all support reactive value binding and change callbacks, delegating to Arc widgets.

#### Scenario: Checkbox binding
- **WHEN** `checkbox("Enable", enabled)` where `enabled = Signal.of(false)` and user toggles checkbox
- **THEN** `enabled.get()` toggles to true; when `enabled.set(false)` programmatically, checkbox shows unchecked

#### Scenario: Checkbox callback
- **WHEN** `checkbox("Enable", true, val -> callback.accept(val))` is rendered and user unchecks the box
- **THEN** the callback is invoked with `false`

#### Scenario: Slider binding
- **WHEN** `slider(volume, 0f, 1f, 0.1f)` where `volume = Signal.of(0.5f)` and slider drags to 0.8
- **THEN** `volume.get()` updates to 0.8; programmatic `volume.set(0.2f)` moves slider thumb

#### Scenario: Integer Slider binding
- **WHEN** `slider(cols, 1, 9, 1)` where `cols = Signal.of(3)` and slider drags to 6
- **THEN** `cols.get()` updates to 6; programmatic `cols.set(4)` moves slider thumb to 4

#### Scenario: Select binding
- **WHEN** `select(selected, List.of("A","B","C"))` and user picks "B"
- **THEN** `selected.get()` becomes "B"; programmatic `selected.set("C")` updates displayed selection
