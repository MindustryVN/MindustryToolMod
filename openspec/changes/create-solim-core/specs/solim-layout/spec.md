# solim-layout Specification

## Purpose
Core layout primitives for Solim — `Column`, `Row`, `Stack`, `Grid`, `Wrap`, `Scroll`, `Container`, `Spacer`, `Divider`, `SplitPane` with `gap`/`justify`/`align`/`grow` semantics built on Arc `Table`/`Stack`/`ScrollPane` mechanisms.

## ADDED Requirements

### Requirement: Column and Row with flex-like modifiers
`Column` and `Row` SHALL be vertical/horizontal layout containers supporting modifiers `gap(int/float)`, `justify(Justify)` (START/CENTER/END/BETWEEN/AROUND/EVENLY), `align(Align)` (START/CENTER/END/STRETCH), `padding(int)`, and `grow`. Each modifier SHALL map to Arc `Table` cell/alignment behavior without perfect CSS Flexbox replication.

#### Scenario: Row justify and align
- **WHEN** `row(() -> { button("A"); button("B"); }).justify(Justify.BETWEEN).align(Align.CENTER).gap(8)` is called
- **THEN** row distributes children with space-between and centers vertically, gap 8 between cells

#### Scenario: Column gap and padding
- **WHEN** `column(() -> { text("Title"); divider(); text("Body"); }).gap(16).padding(24)` is called
- **THEN** column has 16px gap between children and 24px padding, verified via Arc `Table` defaults/padding

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
`grid(int columns, Runnable)` and `grid().columns(n).gap(g)` SHALL provide a simple grid layout with fixed column count and gap. `grid(3, () -> { button("One"); button("Two"); button("Three"); })` SHALL arrange 3 columns. Optional `minCellWidth` responsive mode may be added later but SHALL NOT implement full CSS Grid spec.

#### Scenario: Grid 3 columns
- **WHEN** `grid(3, () -> { button("One"); button("Two"); button("Three"); button("Four"); })` is rendered
- **THEN** buttons are arranged 3 per row, fourth wraps to next row, with uniform cell sizes

#### Scenario: Grid with gap
- **WHEN** `grid().columns(3).gap(8).addChildren(...)` equivalent is used
- **THEN** cells have 8px gap both directions

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
`scroll(() -> { column(...) })` SHALL wrap content in a `ScrollPane` or Arc `Scroll` widget enabling scrolling when content overflows.

#### Scenario: Scroll overflow
- **WHEN** `scroll(() -> { column(() -> { for (i in 0..100) text("Item "+i); }) })` exceeds viewport
- **THEN** content is scrollable vertically

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
