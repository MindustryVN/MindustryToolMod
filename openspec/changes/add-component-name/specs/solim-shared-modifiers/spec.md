## ADDED Requirements

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
