## MODIFIED Requirements

### Requirement: Column and Row with flex-like modifiers
`Column` and `Row` SHALL be vertical/horizontal layout containers supporting fluent configuration modifiers `gap(int/float)`, `justify(Justify)` (START/CENTER/END/BETWEEN/AROUND/EVENLY), `align(Align)` (START/CENTER/END/STRETCH), `padding(int)`, and `grow()`, followed by `.children(Runnable)` for declaring children.

#### Scenario: Row justify and align
- **WHEN** `row().justify(Justify.BETWEEN).align(Align.CENTER).gap(8).children(() -> { button("A"); button("B"); })` is called
- **THEN** row configuration is applied before children are declared, distributing children with space-between and centered vertically

#### Scenario: Column gap and padding
- **WHEN** `column().gap(16).padding(24).children(() -> { text("Title"); divider(); text("Body"); })` is called
- **THEN** column configuration is set before children execution, applying 16px gap and 24px padding

### Requirement: Grid with columns and gap
`grid(int columns)` and `grid(columns).gap(g)` SHALL provide a grid layout container supporting `.children(Runnable)` after column count and gap configuration.

#### Scenario: Grid 3 columns
- **WHEN** `grid(3).children(() -> { button("One"); button("Two"); button("Three"); button("Four"); })` is rendered
- **THEN** grid configuration sets 3 columns before children are attached, arranging 3 buttons per row and wrapping the fourth

### Requirement: Scroll container
`scroll().grow().children(() -> { column().children(...) })` SHALL wrap content in a `ScrollPane` or Arc `Scroll` widget with configuration declared prior to child content.

#### Scenario: Scroll overflow
- **WHEN** `scroll().grow().children(() -> { column().children(() -> { for (i in 0..100) text("Item "+i); }); })` is declared
- **THEN** scroll is configured to grow before child column elements are populated
