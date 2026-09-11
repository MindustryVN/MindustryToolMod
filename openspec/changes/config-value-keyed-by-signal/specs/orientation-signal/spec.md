## ADDED Requirements

### Requirement: OrientationSignal provides reactive isPortrait
`OrientationSignal` SHALL provide a static `Readable<Boolean> isPortrait()` method returning a shared `Signal<Boolean>` that reflects `Core.graphics.isPortrait()`. A static `init()` method SHALL initialize the signal's current value and install a `ResizeEvent` listener so the signal fires whenever the screen orientation changes.

#### Scenario: Signal reflects initial orientation on init
- **WHEN** `OrientationSignal.init()` is called
- **THEN** `OrientationSignal.isPortrait().peek()` equals `Core.graphics.isPortrait()` at that moment

#### Scenario: Signal fires on orientation change
- **WHEN** a `ResizeEvent` is fired and `Core.graphics.isPortrait()` returns a different value than before
- **THEN** `OrientationSignal.isPortrait()` emits the new boolean value

#### Scenario: Signal does not fire when orientation is unchanged
- **WHEN** a `ResizeEvent` is fired but `Core.graphics.isPortrait()` returns the same value as before
- **THEN** `OrientationSignal.isPortrait()` does NOT emit (Signal deduplicates equal values)

#### Scenario: Multiple subscribers receive orientation changes
- **WHEN** two features subscribe to `OrientationSignal.isPortrait()`
- **THEN** both receive the new value when orientation changes
