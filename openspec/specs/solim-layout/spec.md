# solim-layout Specification

## Purpose
TBD - created by archiving change create-solim-core. Update Purpose after archive.
## Requirements
### Requirement: Column and Row with flex-like modifiers
`Column` and `Row` SHALL be vertical/horizontal layout containers supporting fluent configuration modifiers `gap(int/float)`, `justify(Justify)` (START/CENTER/END/BETWEEN/AROUND/EVENLY), `align(Align)` (START/CENTER/END/STRETCH), `padding(int)`, and `grow()`, followed by `.children(Runnable)` for declaring children.

#### Scenario: Row justify and align
- **WHEN** `row().justify(Justify.BETWEEN).align(Align.CENTER).gap(8).children(() -> { button("A"); button("B"); })` is called
- **THEN** row configuration is applied before children are declared, distributing children with space-between and centered vertically

#### Scenario: Column gap and padding
- **WHEN** `column().gap(16).padding(24).children(() -> { text("Title"); divider(); text("Body"); })` is called
- **THEN** column configuration is set before children execution, applying 16px gap and 24px padding

### Requirement: Grow semantics
Layouts SHALL support `growX()`, `growY()`, `grow()` on cells and convenience on widgets (e.g., `textField(input).growX()` or `cell(textField(input)).growX()`). Grow SHALL map to Arc `cell.growX()`/`grow()`.

#### Scenario: GrowX on text field inside row
- **WHEN** `row(() -> { textField(input).growX(); button("Send"); })` inside a column
- **THEN** text field cell has `growX` set and expands to fill remaining width

#### Scenario: Grow via cell wrapper
- **WHEN** `cell(textField(input)).growX()` is used as alternative API
- **THEN** same Arc `growX` behavior occurs

### Requirement: Spacer consumes remaining space
`spacer()` SHALL create an element that grows to consume remaining space in `Row`/`Column`, typically via `add(new Spacer()).grow()`.

#### Scenario: Spacer between buttons
- **WHEN** `row(() -> { button("Back"); spacer(); button("Save"); })` is rendered
- **THEN** spacer expands and "Back" is left-aligned while "Save" is right-aligned

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

