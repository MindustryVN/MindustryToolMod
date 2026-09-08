## ADDED Requirements

### Requirement: ElementModifiers opacity and alpha utilities
The `ElementModifiers` static utility class SHALL provide `opacity(@Nullable Element element, float opacity)` and `opacity(@Nullable Element element, Readable<Float> opacity)` (with `alpha` as an alias) to adjust element color alpha transparency, supporting both static values and reactive signals.

#### Scenario: Setting static element opacity
- **WHEN** `ElementModifiers.opacity(element, 0.6f)` is called
- **THEN** the element's color alpha is set to 0.6f

#### Scenario: Binding reactive element opacity
- **WHEN** `ElementModifiers.opacity(element, opacitySignal)` is called and `opacitySignal` changes from 1.0f to 0.5f
- **THEN** the element's color alpha updates to 0.5f via an internal effect

### Requirement: Fluent opacity modifier on Solim layout containers
Solim layout containers (`Hud`, `Column`, `Row`, `Card`) SHALL expose fluent `.opacity(float)` and `.opacity(Readable<Float>)` (and `.alpha(...)` alias) modifiers that delegate to `ElementModifiers` and return the component instance.

#### Scenario: Chaining opacity on a layout component
- **WHEN** `hud().opacity(feature.opacityConfig.signal())` is declared
- **THEN** the HUD container's transparency is bound to the opacity signal
