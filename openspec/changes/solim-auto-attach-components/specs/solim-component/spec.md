## MODIFIED Requirements

### Requirement: Component as child via ElementResolver
Layouts and parents SHALL accept children that are `Element` or `Component`; `Component` children SHALL be automatically resolved to `component.element()` without requiring callers to write `new Header().element()`. Additionally, `BaseComponent` subclasses instantiated inside a `children()` block SHALL be automatically attached to the current parent without requiring an explicit `component()` call — the `component()` call is now optional inside `children()` scopes.

#### Scenario: Direct Component child without component() call
- **WHEN** `column(() -> { new Header(); new ChatPanel(); })` where `Header` and `ChatPanel` extend `BaseComponent`
- **THEN** parent column contains their resolved `Element`s in order, without any `component()` call

#### Scenario: Mixed Element and Component children
- **WHEN** `column(() -> { text("Hello"); new UserCard(user); image(tex); })`
- **THEN** all three children are attached correctly: `text` and `image` via `attachToParent`, `UserCard` via the auto-attach pending mechanism

#### Scenario: Explicit component() still valid
- **WHEN** `column(() -> { component(new Header()); })` (old style)
- **THEN** the element is attached exactly once; no duplicate child is added
