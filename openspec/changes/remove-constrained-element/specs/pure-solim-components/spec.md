## ADDED Requirements

### Requirement: Solim components only wrap Arc elements without changing behavior
Solim components (`Button`, `Text`, `SolimImage`, `SolimTextField`, `Checkbox`, `Card`, etc.) SHALL wrap standard Arc widgets directly without creating custom subclasses that override Arc's native layout or measurement methods (`getPrefWidth`, `getPrefHeight`, `getMinWidth`, `getMinHeight`, etc.) or implementing synthetic constraint marker interfaces (`ConstrainedElement`).

#### Scenario: Instantiating standard Arc widgets directly
- **WHEN** a Solim component such as `Button`, `Text`, `Checkbox`, or `SolimTextField` is instantiated
- **THEN** its underlying element is a standard Arc widget instance (e.g. `arc.scene.ui.Button`, `Label`, `CheckBox`, `TextField`) without layout calculation overrides

#### Scenario: No ConstrainedElement marker implementation
- **WHEN** any Solim component's underlying Arc element is inspected
- **THEN** it does not implement `ConstrainedElement`
