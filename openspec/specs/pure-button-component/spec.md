# pure-button-component Specification

## Purpose
TBD - created by archiving change pure-solim-button-refactor. Update Purpose after archive.
## Requirements
### Requirement: Solim provides a pure composable button component
The Solim UI framework SHALL provide a single `button()` container component that accepts an optional click listener, centers its child elements by default, and relies on `.children()` blocks for content layout and composition.

#### Scenario: Constructing a button with children
- **WHEN** a developer calls `button(onClick).children(() -> { ... })`
- **THEN** a button component is created with default center alignment for its children, with the click listener and inner child components attached to the button container

### Requirement: Deprecation of specialized button facades
The system SHALL NOT provide `IconButton` or specialized text/icon factory overloads on `solim.ui.Ui`.

#### Scenario: Attempting to use removed button facades
- **WHEN** building UI components via `solim.ui.Ui`
- **THEN** only `button()` and `button(onClick)` are available to instantiate buttons

