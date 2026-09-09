# two-way-binding Specification

## Purpose
TBD - created by archiving change solim-architecture-refactor. Update Purpose after archive.
## Requirements
### Requirement: Shared TwoWayBinding utility
A `TwoWayBinding<T>` utility SHALL exist in `solim.input` (package-private is acceptable) that implements the signal ↔ widget synchronization pattern with feedback loop prevention. It SHALL implement `Disposable`.

#### Scenario: Signal change updates widget
- **WHEN** the bound `Signal<T>` value changes
- **THEN** the widget is updated via the widget setter, with equality check to prevent unnecessary updates

#### Scenario: Widget change updates signal
- **WHEN** the widget fires a change event
- **THEN** the `Signal<T>` is updated via `signal.set(widgetGetter.get())`

#### Scenario: Programmatic signal update does not loop back
- **WHEN** signal changes and triggers widget setter
- **THEN** the widget setter does NOT cause another signal update (feedback loop is prevented)

#### Scenario: TwoWayBinding is Disposable
- **WHEN** `TwoWayBinding.dispose()` is called
- **THEN** the Effect and widget listener are both unregistered

### Requirement: All input components use TwoWayBinding
`Checkbox`, `SolimTextField`, `SolimSlider`, `SolimSelect`, and `Switch` SHALL delegate their two-way binding logic to `TwoWayBinding` and SHALL NOT independently maintain a `boolean updating` flag.

#### Scenario: Checkbox uses TwoWayBinding
- **WHEN** `Checkbox` is constructed with a `Signal<Boolean>`
- **THEN** its synchronization behavior is handled by `TwoWayBinding`

#### Scenario: SolimTextField uses TwoWayBinding
- **WHEN** `SolimTextField` is constructed with a `Signal<String>`
- **THEN** its synchronization behavior is handled by `TwoWayBinding`

