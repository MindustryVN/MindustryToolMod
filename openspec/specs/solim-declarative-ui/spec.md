# solim-declarative-ui Specification

## Purpose
TBD - created by archiving change create-solim-core. Update Purpose after archive.
## Requirements
### Requirement: Implicit parent stack with lambda scopes
The framework SHALL provide `solim.ui.ParentStack` (or `Ui` facade) with static helpers `column(Runnable)`, `row(Runnable)`, `stack(Runnable)`, `grid(int columns, Runnable)`, `wrap(Runnable)`, `scroll(Runnable)`, `container(Runnable)` that push a layout `Element` onto a stack, execute the lambda, pop with `try/finally`, and return the layout `Element`. Every child created inside the lambda SHALL auto-attach to current parent.

#### Scenario: Push/pop with try/finally
- **WHEN** `column(() -> { text("Settings"); row(() -> { button("Cancel"); button("Save"); }); })` executes
- **THEN** internally: push Column, add Text to Column, push Row, add Button+Button to Row, pop Row, pop Column; if lambda throws, `finally` still pops

#### Scenario: Auto-attach children
- **WHEN** inside `column(() -> { text("Chat"); row(() -> { textField(input); button("Send", this::send); }); })`
- **THEN** `textField` and `button` are children of inner `row`, and `text` + `row` are children of outer `column` without explicit `add` calls

#### Scenario: No start/end API
- **WHEN** `solim.ui.Ui` is inspected
- **THEN** it does NOT expose `startColumn()`/`endColumn()` or `startComponent()`/`endComponent()` — only lambda-scoped methods exist

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

