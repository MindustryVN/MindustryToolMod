# solim-component-auto-attach Specification

## Purpose
`BaseComponent` instances created inside an active `ParentStack` scope auto-attach their element to the current parent, making `component()` unnecessary for the common in-`children()` usage.

## Requirements

### Requirement: BaseComponent auto-attaches to active parent
When a `BaseComponent` subclass is instantiated inside an active `ParentStack` scope (i.e., inside a `children()` block), it SHALL automatically schedule its element for attachment to the current parent container without requiring an explicit `component()` call.

#### Scenario: BaseComponent auto-attaches in children block
- **WHEN** `column(() -> { new ChatMessageListView(store, service); })` is called
- **THEN** the `ChatMessageListView` element is attached to the column table without any `component()` call

#### Scenario: BaseComponent in nested children blocks
- **WHEN** `row(() -> { text("Label"); new MyCard(data); })` is called inside a `column(() -> { ... })`
- **THEN** `MyCard`'s element is attached to the inner `row`, not the outer `column`

#### Scenario: No auto-attach outside children scope
- **WHEN** a `BaseComponent` is instantiated outside any `ParentStack` scope (e.g., stored as a field before `build()` runs)
- **THEN** no element attachment occurs; the element is only attached when explicitly placed via `component()` or a parent's `children()` block

#### Scenario: Explicit component() still works and does not double-attach
- **WHEN** `column(() -> { component(new ChatChannelListView(store)); })` is called (old style)
- **THEN** the element is attached exactly once; the auto-attach from construction and the explicit `component()` call do NOT produce duplicate children
