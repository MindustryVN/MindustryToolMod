# pure-solim-components Specification

## Purpose
TBD - created by archiving change pure-solim-button-refactor. Update Purpose after archive.
## Requirements
### Requirement: Solim components are unstyled by default
Solim UI layout and input components SHALL NOT automatically inject or resolve default UI styles or skins (`ButtonStyle`, `ImageButtonStyle`, skin fallbacks).

#### Scenario: Instantiating a pure Solim component
- **WHEN** a Solim component such as `Card` or `Button` is instantiated without an explicit style
- **THEN** the component initializes unstyled without fallback skin resolution

### Requirement: Solim components only wrap Arc elements without changing behavior
Solim components (`Button`, `Text`, `SolimImage`, `SolimTextField`, `Checkbox`, `Card`, etc.) SHALL wrap standard Arc widgets directly without creating custom subclasses that override Arc's native layout or measurement methods (`getPrefWidth`, `getPrefHeight`, `getMinWidth`, `getMinHeight`, etc.) or implementing synthetic constraint marker interfaces (`ConstrainedElement`).

#### Scenario: Instantiating standard Arc widgets directly
- **WHEN** a Solim component such as `Button`, `Text`, `Checkbox`, or `SolimTextField` is instantiated
- **THEN** its underlying element is a standard Arc widget instance (e.g. `arc.scene.ui.Button`, `Label`, `CheckBox`, `TextField`) without layout calculation overrides

#### Scenario: No ConstrainedElement marker implementation
- **WHEN** any Solim component's underlying Arc element is inspected
- **THEN** it does not implement `ConstrainedElement`

