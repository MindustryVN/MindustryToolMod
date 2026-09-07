## MODIFIED Requirements

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
