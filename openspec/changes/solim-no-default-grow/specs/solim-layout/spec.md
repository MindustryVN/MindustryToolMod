## MODIFIED Requirements

### Requirement: Grow semantics
Layouts SHALL support `growX()`, `growY()`, `grow()` on cells and widgets (e.g., `textField(input).growX()` or `button("Action").growX()`). Components in layout containers (`Column`, `Row`, `Card`, `Scroll`) and structural components (`ForEach`, `Dynamic`) SHALL NOT grow by default. Sizing SHALL adhere to the child's natural content size or explicit constraints unless `grow()`, `growX()`, or `growY()` is explicitly invoked, or the element is an expanding spacer. Grow SHALL map to Arc `cell.growX()`/`grow()`.

#### Scenario: Components do not grow by default
- **WHEN** a component without `growX()`, `growY()`, or `grow()` is attached to a `Column`, `Row`, `Card`, `Scroll`, or `ForEach`
- **THEN** the parent cell does NOT set `growX` or `growY`, and the component retains its natural or explicit dimensions

#### Scenario: Explicit growX on text field inside row
- **WHEN** `row(() -> { textField(input).growX(); button("Send"); })` inside a column
- **THEN** text field cell has `growX` set and expands to fill remaining width, while the button does not grow

#### Scenario: Grow via cell wrapper
- **WHEN** `cell(textField(input)).growX()` is used as alternative API
- **THEN** same Arc `growX` behavior occurs
