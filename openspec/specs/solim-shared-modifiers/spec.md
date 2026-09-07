# solim-shared-modifiers Specification

## Requirements

### Requirement: Centralized ElementModifiers utility for element sizing and positioning
The Solim framework SHALL provide an `ElementModifiers` static utility class in package `solim.modifier` to handle sizing (`width`, `height`, `size`), positioning (`x`, `y`, `position`), alignment, and visibility mutations on Arc `Element` and `Table` instances.

#### Scenario: Delegating element size and position mutations
- **WHEN** a component invokes `ElementModifiers.width(element, width)` or `ElementModifiers.size(element, w, h)`
- **THEN** `ElementModifiers` safely applies lower-bounded dimensions to the element and updates preferred size if applicable

#### Scenario: Delegating table alignment mutations
- **WHEN** a component invokes `ElementModifiers.align(table, align)` or `ElementModifiers.center(table)`
- **THEN** `ElementModifiers` updates the table's alignment

### Requirement: Components delegate modifier methods to ElementModifiers
Solim components SHALL expose semantic fluent modifier methods (`width`, `height`, `size`, `position`, `visible`, etc.) that return `this` for chaining while delegating implementation execution directly to `ElementModifiers`.

#### Scenario: Chaining modifier methods on UI components
- **WHEN** a developer calls `.width(100f).height(40f).visible(true)` on a Solim component
- **THEN** internal element mutations are executed via `ElementModifiers` and the component instance is returned for chaining
