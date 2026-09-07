# solim-units Specification

## Purpose
Reactive viewport unit signals (`dvw`, `dvh`) and percentage calculation utilities that automatically synchronize with window resize events.

## Requirements

### Requirement: Reactive Viewport Signals
The `Units` class SHALL expose reactive signals `dvw` and `dvh` representing 1% of the dynamic viewport width and 1% of the dynamic viewport height in Arc scene coordinates (`(Core.graphics.getWidth() / Scl.scl()) / 100f` and `(Core.graphics.getHeight() / Scl.scl()) / 100f`).

#### Scenario: Reading initial dvw and dvh
- **WHEN** `Units.dvw` and `Units.dvh` signals are queried
- **THEN** they return 1% of current screen width and height in scene coordinates

### Requirement: Auto Update on ResizeEvent
The `Units` class SHALL automatically listen to Mindustry's `EventType.ResizeEvent` and update both `dvw` and `dvh` signals when the event fires.

#### Scenario: Screen size changes
- **WHEN** Mindustry fires `EventType.ResizeEvent` after the screen dimensions change
- **THEN** `Units.dvw` and `Units.dvh` signals update to reflect the new viewport dimensions
- **THEN** all reactive components and computed values bound to `Units.dvw` or `Units.dvh` are notified and updated

### Requirement: Viewport Percentage Helper Methods
The `Units` class SHALL provide helper methods `dvw(float percentage)` and `dvh(float percentage)` returning `Computed<Float>` representing the given percentage of the dynamic viewport width and height.

#### Scenario: Computing percentage of viewport
- **WHEN** `Units.dvw(50f)` or `Units.dvh(80f)` is evaluated
- **THEN** it returns a computed value equal to 50% of the viewport width or 80% of the viewport height respectively

#### Scenario: Dynamic percentage computation
- **WHEN** `Units.dvw(Readable<Float> percentage)` is evaluated with a reactive signal
- **THEN** the returned computed value updates when either the percentage signal or the viewport width changes

### Requirement: Full Viewport Dimensions Access
The `Units` class SHALL provide access to full viewport dimensions in scene coordinates.

#### Scenario: Reading full width and height
- **WHEN** full viewport dimensions are requested
- **THEN** `Units.width()` and `Units.height()` return computed signals equal to 100% of dynamic viewport width and height
