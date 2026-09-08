# solim-layout Specification

## Purpose
TBD - created by archiving change create-solim-core. Update Purpose after archive.
## Requirements
### Requirement: Column and Row with flex-like modifiers
`Column` and `Row` SHALL be vertical/horizontal layout containers supporting fluent configuration modifiers `gap(int/float)`, `justify(Justify)` (START/CENTER/END/BETWEEN/AROUND/EVENLY), `align(Align)` (START/CENTER/END/STRETCH), `padding(int)`, and `grow()`, followed by `.children(Runnable)` for declaring children. Components SHALL delegate `gap` calculations to `ElementModifiers.gap()`.

#### Scenario: Row justify and align
- **WHEN** `row().justify(Justify.BETWEEN).align(Align.CENTER).gap(8).children(() -> { button("A"); button("B"); })` is called
- **THEN** row configuration is applied before children are declared, distributing children with space-between and centered vertically using `ElementModifiers.gap()`

#### Scenario: Column gap and padding
- **WHEN** `column().gap(16).padding(24).children(() -> { text("Title"); divider(); text("Body"); })` is called
- **THEN** column configuration is set before children execution, applying 16px gap and 24px padding using `ElementModifiers`

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

### Requirement: Spacer consumes remaining space
`spacer()` SHALL create an element that grows to consume remaining space in `Row`/`Column`, typically via `add(new Spacer()).grow()`. The spacer element SHALL be recognized by `Ui.isExpanding(child)` and cause `Row.ATTACHER` to apply `cell.growX()` and `Column.ATTACHER` to apply `cell.growY()`.

#### Scenario: Spacer between buttons in Row
- **WHEN** `row(() -> { button("Back"); spacer(); button("Save"); })` is rendered
- **THEN** spacer expands horizontally with `growX > 0` and "Back" is left-aligned while "Save" is right-aligned

#### Scenario: Spacer in Column
- **WHEN** `column(() -> { text("Top"); spacer(); text("Bottom"); })` is rendered
- **THEN** spacer expands vertically with `growY > 0`, pushing "Bottom" to the end of the column

### Requirement: Grid with columns and gap
`grid(int columns)` and `grid(columns).gap(g)` SHALL provide a grid layout container supporting `.children(Runnable)` after column count and gap configuration.

#### Scenario: Grid 3 columns
- **WHEN** `grid(3).children(() -> { button("One"); button("Two"); button("Three"); button("Four"); })` is rendered
- **THEN** grid configuration sets 3 columns before children are attached, arranging 3 buttons per row and wrapping the fourth

### Requirement: Wrap children wrapping
`wrap(() -> { ... })` SHALL layout children horizontally and wrap to next line when exceeding container width, using Arc wrapping container or `Table` with wrap enabled.

#### Scenario: Wrap tags
- **WHEN** `wrap(() -> { text("Java"); text("Mindustry"); text("Arc"); })` with narrow width
- **THEN** texts flow to next line when out of horizontal space

### Requirement: Stack overlays children
`stack(() -> { image(bg); text("Loading"); })` SHALL overlay children on top of each other (last on top) using Arc `Stack`.

#### Scenario: Stack overlay
- **WHEN** `stack(() -> { image(background); text("Loading"); })` is rendered
- **THEN** both children occupy same bounds with text drawn over image

### Requirement: Scroll container
`scroll().grow().children(() -> { column().children(...) })` SHALL wrap content in a `ScrollPane` or Arc `Scroll` widget with configuration declared prior to child content.

#### Scenario: Scroll overflow
- **WHEN** `scroll().grow().children(() -> { column().children(() -> { for (i in 0..100) text("Item "+i); }); })` is declared
- **THEN** scroll is configured to grow before child column elements are populated

### Requirement: Container, Divider, SplitPane
Framework SHALL provide `Container` (single child with padding/background), `Divider` (horizontal/vertical line), and `SplitPane` (if Arc provides `SplitPane` primitive) as thin wrappers.

#### Scenario: Divider
- **WHEN** `column(() -> { text("Above"); divider(); text("Below"); })` is rendered
- **THEN** a horizontal line (e.g., `Image` with `Tex.whiteui` tinted) separates sections

#### Scenario: Container padding
- **WHEN** `container(() -> text("Hi")).padding(12).background(Styles.black6)` is used
- **THEN** child has 12px padding and background drawable

### Requirement: Use Arc-native layout mechanisms
All layout primitives SHALL delegate to Arc's existing `Table`, `Stack`, `ScrollPane`, `Cell` APIs; SHALL NOT reimplement layout engine from scratch.

#### Scenario: No custom layout engine
- **WHEN** `Column.java`/`Row.java` are inspected
- **THEN** they contain `Table` or `arc.scene.ui.layout` delegation, not a custom measure/layout pass

### Requirement: Justify and Align enums
`Justify` SHALL have START, CENTER, END, BETWEEN, AROUND, EVENLY. `Align` SHALL have START, CENTER, END, STRETCH.

#### Scenario: Enum values exist
- **WHEN** `Justify.values()` and `Align.values()` are inspected
- **THEN** they contain exactly the listed constants
