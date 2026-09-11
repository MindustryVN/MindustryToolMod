# orientation-signal Specification

## Purpose
Provides a framework-level reactive signal tracking screen orientation (portrait vs landscape) via Arc's `ResizeEvent`.

## Requirements

### Requirement: Signals provides reactive isPortrait
`Signals` SHALL provide a static `Readable<Boolean> isPortrait()` method returning a shared `Signal<Boolean>` that reflects `Core.graphics.isPortrait()`. Static initialization SHALL automatically initialize the signal's current value and install a `ResizeEvent` listener so the signal fires whenever the screen orientation changes.

#### Scenario: Signal reflects initial orientation on init
- **WHEN** `Signals` class is loaded
- **THEN** `Signals.isPortrait().peek()` equals `Core.graphics.isPortrait()` at that moment

#### Scenario: Signal fires on orientation change
- **WHEN** a `ResizeEvent` is fired and `Core.graphics.isPortrait()` returns a different value than before
- **THEN** `Signals.isPortrait()` emits the new boolean value

#### Scenario: Signal does not fire when orientation is unchanged
- **WHEN** a `ResizeEvent` is fired but `Core.graphics.isPortrait()` returns the same value as before
- **THEN** `Signals.isPortrait()` does NOT emit (Signal deduplicates equal values)

#### Scenario: Multiple subscribers receive orientation changes
- **WHEN** two features subscribe to `Signals.isPortrait()`
- **THEN** both receive the new value when orientation changes
