# solim-declarative-ui Specification

## Purpose
TBD - created by archiving change create-solim-core. Update Purpose after archive.
## Requirements
### Requirement: Implicit parent stack with lambda scopes
The framework SHALL provide `solim.ui.ParentStack` (and `Ui` facade) with static helpers `column()`, `row()`, `stack()`, `grid(int columns)`, `wrap()`, `scroll()`, `container()`, `card()`, and `dialog(String title)` returning fluent builder instances supporting `.children(Runnable)`. Calling `.children(Runnable)` pushes the layout `Element` onto `ParentStack`, executes the lambda, pops with `try/finally`, attaches the layout to the outer active parent container, and returns the container instance. Every child created inside the `.children(Runnable)` lambda SHALL auto-attach to current parent.

#### Scenario: Push/pop with try/finally and configuration before children
- **WHEN** `column().grow().children(() -> { text("Settings"); row().growX().children(() -> { button("Cancel"); button("Save"); }); })` executes
- **THEN** internally: configuration `.grow()` is applied to column first, then column table is pushed to ParentStack, text is added to column, row table is pushed with `.growX()`, buttons are added to row, row is popped and attached to column, column is popped; if lambda throws, `finally` still pops

#### Scenario: Auto-attach children
- **WHEN** inside `column().children(() -> { text("Chat"); row().children(() -> { textField(input); button("Send", this::send); }); })`
- **THEN** `textField` and `button` are children of inner `row`, and `text` + `row` are children of outer `column` without explicit `add` calls

#### Scenario: No start/end API
- **WHEN** `solim.ui.Ui` is inspected
- **THEN** it does NOT expose `startColumn()`/`endColumn()` or `startComponent()`/`endComponent()` — only configuration-before-children fluent methods exist

#### Scenario: Declarative card composition
- **WHEN** `card(Styles.black8).padding(10f).children(() -> { text("Card Title"); })` is called
- **THEN** card padding is configured, a `Card` layout is pushed onto the stack, children are attached within the card's inner container, and the card is auto-attached to the current parent

### Requirement: Icon button declarative facades
`Ui` SHALL provide static facades `iconButton(Drawable icon, Runnable onClick)` and `iconButton(Drawable icon, ImageButtonStyle style, Runnable onClick)` that construct an `IconButton`, automatically attach it to the active parent in `ParentStack`, and return the component for chained modifier calls.

#### Scenario: Attaching icon button via Ui facade
- **WHEN** `iconButton(Icon.infoCircle, onClick)` is called inside a `row(...)`
- **THEN** an `IconButton` is created, its element attached to the row table, and the `IconButton` instance returned

### Requirement: Stack cleanup guarantee
`ParentStack` SHALL guarantee cleanup even if child construction throws, using `try { push; runnable.run(); } finally { pop; }`.

#### Scenario: Exception cleanup
- **WHEN** `column(() -> { text("A"); throw new RuntimeException("fail"); })`
- **THEN** `ParentStack` size returns to previous value after catch, and subsequent `column(...)` works correctly

### Requirement: Current parent tracking and child add
`ParentStack` SHALL expose `current()` (or `peek()`) returning current parent `Element`/`Table` and `add(Element|Component)` that resolves and attaches to current parent. If no parent is active, `add` SHALL either throw `IllegalStateException` or attach to a supplied root.

#### Scenario: Add outside parent throws or roots
- **WHEN** `text("Hello")` is called with no active parent
- **THEN** framework either throws `IllegalStateException("No parent")` or requires caller to supply container; behavior is documented

### Requirement: Child resolution Element vs Component
`ElementResolver` SHALL resolve children: `Component → component.element()` , `Element → element` . Layout helpers SHALL accept both without forcing `.element()` at call sites.

#### Scenario: Resolve Component
- **WHEN** `ElementResolver.resolve(new Header())` where `Header implements Component`
- **THEN** returns `header.element()`

#### Scenario: Resolve Element passthrough
- **WHEN** `ElementResolver.resolve(new Label("hi"))`
- **THEN** returns same `Label` instance

#### Scenario: Column add overloads
- **WHEN** `column(() -> { add(new Header()); add(someElement); })`
- **THEN** both are added via resolver without `// require new Header().element()` comment

### Requirement: Layout facades return layout Element
Each declarative helper SHALL return the created layout `Element` so callers can chain modifiers or store references.

#### Scenario: Chained modifiers
- **WHEN** `Element e = column(() -> { text("Hi"); }).padding(24).gap(16)`
- **THEN** `e` is the column `Table` with padding/gap applied

### Requirement: Single-threaded stack without ThreadLocal
`ParentStack` SHALL be implemented as a simple `Deque<Element>` / `ArrayDeque` static stack for single-threaded game thread; `ThreadLocal` SHALL NOT be used unless proven necessary.

#### Scenario: Stack is plain static
- **WHEN** `ParentStack.java` is inspected
- **THEN** it contains `private static final Deque<...> stack = new ArrayDeque<>()` and no `ThreadLocal` import

