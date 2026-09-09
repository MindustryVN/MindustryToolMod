# basecomponent-ui-separation Specification

## Purpose
TBD - created by archiving change solim-architecture-refactor. Update Purpose after archive.
## Requirements
### Requirement: BaseComponent has no UI attachment logic
`BaseComponent` SHALL NOT import or reference `ParentStack`, `Table`, or any Arc layout type for the purpose of UI placement. It SHALL NOT call `ParentStack.registerPendingComponent(...)` or any equivalent in its constructor.

#### Scenario: BaseComponent constructor has no UI side effects
- **WHEN** `new MyComponent()` is called outside any `children()` block
- **THEN** no UI element is attached to any parent and no `ParentStack` state is mutated

#### Scenario: element() builds without mounting
- **WHEN** `component.element()` is called directly
- **THEN** the Arc Element is built and returned but NOT attached to any scene parent

### Requirement: UI attachment is driven by the composition layer
All UI attachment (adding an Element to a Table/parent) SHALL happen through `Ui.*` factory methods or the explicit `ParentStack` API in the composition layer. Application components SHALL obtain their Element via `element()` or `element.add(component.element())`.

#### Scenario: Ui.* factory attaches element to parent
- **WHEN** a Solim factory like `column()` is called inside a `children()` block
- **THEN** the resulting Element is attached to the current parent via `ParentStack`

#### Scenario: Component created manually requires explicit attachment
- **WHEN** `new MyComponent()` is called and then `table.add(component.element())`
- **THEN** the element is attached at that point, not at construction time

