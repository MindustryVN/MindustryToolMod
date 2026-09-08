## MODIFIED Requirements

### Requirement: Centralized ElementModifiers utility for element sizing and positioning
The Solim framework SHALL provide an `ElementModifiers` static utility class in package `solim.modifier` to handle sizing (`width`, `height`, `size`), positioning (`x`, `y`, `position`), alignment, and visibility mutations on Arc `Element` and `Table` instances. Target `Element` and `Table` arguments SHALL NOT be null; `ElementModifiers` SHALL throw `NullPointerException` if a null target is provided.

#### Scenario: Delegating element size and position mutations
- **WHEN** a component invokes `ElementModifiers.width(element, width)` or `ElementModifiers.size(element, w, h)` with non-null `element`
- **THEN** `ElementModifiers` applies lower-bounded dimensions to the element and updates preferred size if applicable

#### Scenario: Delegating table alignment mutations
- **WHEN** a component invokes `ElementModifiers.align(table, align)` or `ElementModifiers.center(table)` with non-null `table`
- **THEN** `ElementModifiers` updates the table's alignment

#### Scenario: Rejecting null target element or table
- **WHEN** any sizing, positioning, alignment, or visibility method on `ElementModifiers` is invoked with `null` target
- **THEN** a `NullPointerException` is thrown immediately

### Requirement: ElementModifiers gap utility for table spacing
The `ElementModifiers` static utility class SHALL provide `gap(Table table, float gap)` and `gap(Element element, float gap)` static methods to apply default cell padding / item gap spacing to Arc `Table` instances. `table` and `element` SHALL NOT be null. For `gap(Element element, float gap)`, `element` MUST be an instance of `Table`.

#### Scenario: Applying gap spacing via ElementModifiers
- **WHEN** `ElementModifiers.gap(table, gap)` is invoked with a non-null Table and float value `g`
- **THEN** `table.defaults().pad(g / 2f)` is executed on the table

#### Scenario: Applying gap spacing via Element overload
- **WHEN** `ElementModifiers.gap(element, gap)` is invoked with an `element` that is an instance of `Table`
- **THEN** `table.defaults().pad(gap / 2f)` is executed on the table

#### Scenario: Rejecting null table or element for gap
- **WHEN** `ElementModifiers.gap((Table) null, gap)` or `ElementModifiers.gap((Element) null, gap)` is invoked
- **THEN** a `NullPointerException` is thrown immediately

#### Scenario: Rejecting non-Table element for gap
- **WHEN** `ElementModifiers.gap(element, gap)` is invoked where `element` is not an instance of `Table`
- **THEN** an `IllegalArgumentException` is thrown immediately

### Requirement: ElementModifiers name utility for element naming
The `ElementModifiers` static utility class SHALL provide `name(Element element, @Nullable String name)` to set the `name` field on the given Arc `Element`. The target `element` SHALL NOT be null.

#### Scenario: Setting element name via ElementModifiers
- **WHEN** `ElementModifiers.name(element, "test-name")` is invoked with a non-null Element
- **THEN** `element.name` equals `"test-name"`

#### Scenario: Rejecting null element for naming
- **WHEN** `ElementModifiers.name(null, "test-name")` is invoked
- **THEN** a `NullPointerException` is thrown immediately
