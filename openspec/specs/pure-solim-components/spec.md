# pure-solim-components Specification

## Purpose
TBD - created by archiving change pure-solim-button-refactor. Update Purpose after archive.
## Requirements
### Requirement: Solim components are unstyled by default
Solim UI layout and input components SHALL NOT automatically inject or resolve default UI styles or skins (`ButtonStyle`, `ImageButtonStyle`, skin fallbacks).

#### Scenario: Instantiating a pure Solim component
- **WHEN** a Solim component such as `Card` or `Button` is instantiated without an explicit style
- **THEN** the component initializes unstyled without fallback skin resolution

