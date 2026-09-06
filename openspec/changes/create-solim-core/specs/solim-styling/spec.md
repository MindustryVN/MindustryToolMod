# solim-styling Specification

## Purpose
Lightweight styling system for Solim — immutable `Style` objects applied to Arc widgets with static and reactive style binding support.

## ADDED Requirements

### Requirement: Style immutable value object
`Style` SHALL be an immutable object holding Arc-relevant styling data (e.g., `Drawable background`, `Color`, `font`, `pad`/`margin` variants relevant to widget, or a reference to Arc `TextButtonStyle`/`LabelStyle` wrapper) and SHALL NOT be mutated after creation. Predefined instances SHALL be provided via `Styles` constants (`PRIMARY`, `GHOST`, etc.).

#### Scenario: Immutable style
- **WHEN** `Style s = Style.builder().background(Styles.PRIMARY).pad(8).build()` then `s` is used on two buttons
- **THEN** modifying builder after `build()` does not affect `s`, and `s` has no setters

#### Scenario: Styles constants exist
- **WHEN** `Styles.PRIMARY` and `Styles.GHOST` are referenced
- **THEN** they are non-null `Style` singletons usable via `.style(Styles.PRIMARY)`

### Requirement: Static style application
`widget.style(Style)` SHALL apply the entire `Style` to the underlying `Element` immediately (e.g., `button.style.background = style.background` or `element.setStyle(...)` depending on widget). No CSS parsing SHALL be involved.

#### Scenario: Static style apply
- **WHEN** `button("Save").style(Styles.PRIMARY)` is called
- **THEN** underlying Arc element has primary style applied and `getStyle()` reflects it

### Requirement: Reactive style binding
`widget.style(Signal<Style>)` and `widget.style(Computed<Style>)` SHALL apply current style immediately, subscribe to future changes, and on each change re-apply the entire new `Style` to the `Element` without diffing.

#### Scenario: Reactive style toggle
- **WHEN** `Signal<Boolean> darkMode = Signal.of(false)` and `button("Toggle").style(darkMode.map(v -> v ? Styles.PRIMARY : Styles.GHOST))` and `darkMode.set(true)`
- **THEN** button style switches from GHOST to PRIMARY via full re-apply

#### Scenario: Reactive style is disposable
- **WHEN** style binding is disposed then signal changes
- **THEN** element style remains previous value

### Requirement: Style application without diffing or CSS
Framework SHALL NOT implement CSS parsing, CSS Grid/Flexbox recreation, or style diffing beyond straightforward full re-apply. Do not attempt to diff individual style fields unless Arc internals require it.

#### Scenario: No CSS engine
- **WHEN** `Style.java` is inspected
- **THEN** it contains no CSS parser, no `selector`, no `stylesheet`, and no `flex`/`grid` property strings

#### Scenario: Full re-apply on change
- **WHEN** reactive style changes from `A` to `B`
- **THEN** implementation calls `applyStyle(element, B)` that overwrites all relevant fields, not a field-by-field diff

### Requirement: Style builder and composition
`Style` SHALL provide a builder or factory that allows composing from existing `Styles` constants plus overrides, and SHALL remain thin and Arc-native.

#### Scenario: Compose from base
- **WHEN** `Style custom = Styles.PRIMARY.withPad(16).withBackground(otherDrawable)`
- **THEN** `custom` has primary fields plus overrides without mutating `PRIMARY`
