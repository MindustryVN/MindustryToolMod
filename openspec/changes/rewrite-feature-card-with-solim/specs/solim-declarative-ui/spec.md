## MODIFIED Requirements

### Requirement: Implicit parent stack with lambda scopes
The framework SHALL provide `solim.ui.ParentStack` (and `Ui` facade) with static helpers `column(Runnable)`, `row(Runnable)`, `stack(Runnable)`, `grid(int columns, Runnable)`, `wrap(Runnable)`, `scroll(Runnable)`, `container(Runnable)`, and `card(Runnable)` / `card(ButtonStyle, Runnable)` that push a layout `Element` onto a stack, execute the lambda, pop with `try/finally`, and return the layout `Element` or component. Every child created inside the lambda SHALL auto-attach to current parent.

#### Scenario: Push/pop with try/finally
- **WHEN** `column(() -> { text("Settings"); row(() -> { button("Cancel"); button("Save"); }); })` executes
- **THEN** internally: push Column, add Text to Column, push Row, add Button+Button to Row, pop Row, pop Column; if lambda throws, `finally` still pops

#### Scenario: Auto-attach children
- **WHEN** inside `column(() -> { text("Chat"); row(() -> { textField(input); button("Send", this::send); }); })`
- **THEN** `textField` and `button` are children of inner `row`, and `text` + `row` are children of outer `column` without explicit `add` calls

#### Scenario: No start/end API
- **WHEN** `solim.ui.Ui` is inspected
- **THEN** it does NOT expose `startColumn()`/`endColumn()` or `startComponent()`/`endComponent()` — only lambda-scoped methods exist

#### Scenario: Declarative card composition
- **WHEN** `card(Styles.black8, () -> { text("Card Title"); })` is called
- **THEN** a `Card` layout is pushed onto the stack, children are attached within the card's inner container, and the card is auto-attached to the current parent

## ADDED Requirements

### Requirement: Icon button declarative facades
`Ui` SHALL provide static facades `iconButton(Drawable icon, Runnable onClick)` and `iconButton(Drawable icon, ImageButtonStyle style, Runnable onClick)` that construct an `IconButton`, automatically attach it to the active parent in `ParentStack`, and return the component for chained modifier calls.

#### Scenario: Attaching icon button via Ui facade
- **WHEN** `iconButton(Icon.infoCircle, onClick)` is called inside a `row(...)`
- **THEN** an `IconButton` is created, its element attached to the row table, and the `IconButton` instance returned
