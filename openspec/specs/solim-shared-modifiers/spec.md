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

### Requirement: ElementModifiers gap utility for table spacing
The `ElementModifiers` static utility class SHALL provide `gap(@Nullable Table table, float gap)` and `gap(@Nullable Element element, float gap)` static methods to apply default cell padding / item gap spacing to Arc `Table` instances.

#### Scenario: Applying gap spacing via ElementModifiers
- **WHEN** `ElementModifiers.gap(table, gap)` is invoked with a non-null Table and float value `g`
- **THEN** `table.defaults().pad(g / 2f)` is executed on the table

### Requirement: Components delegate modifier methods to ElementModifiers
Solim components SHALL expose semantic fluent modifier methods (`width`, `height`, `size`, `position`, `visible`, `gap`, etc.) that return `this` for chaining while delegating implementation execution directly to `ElementModifiers`.

#### Scenario: Chaining modifier methods on UI components
- **WHEN** a developer calls `.width(100f).height(40f).gap(8f).visible(true)` on a Solim component (such as `Button` or `Row`)
- **THEN** internal element mutations including gap spacing are executed via `ElementModifiers` and the component instance is returned for chaining

### Requirement: ElementModifiers name utility for element naming
The `ElementModifiers` static utility class SHALL provide `name(@Nullable Element element, @Nullable String name)` to set the `name` field on the given Arc `Element`.

#### Scenario: Setting element name via ElementModifiers
- **WHEN** `ElementModifiers.name(element, "test-name")` is invoked with a non-null Element
- **THEN** `element.name` equals `"test-name"`

#### Scenario: Null-safe element naming
- **WHEN** `ElementModifiers.name(null, "test-name")` is invoked
- **THEN** no exception is thrown

### Requirement: Fluent name modifier on Solim components
All Solim components SHALL expose a fluent `.name(String name)` method that updates the underlying Arc `Element.name` and returns the component instance for method chaining.

#### Scenario: Chaining name modifier on Row
- **WHEN** `row().name("my-row").gap(8f)` is declared
- **THEN** the underlying table has `name` set to `"my-row"` and the `Row` instance is returned for subsequent chaining

#### Scenario: Chaining name modifier on Button
- **WHEN** `button("OK").name("confirm-button")` is declared
- **THEN** the underlying button has `name` set to `"confirm-button"`
