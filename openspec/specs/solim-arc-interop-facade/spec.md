# solim-arc-interop-facade Specification

## Purpose
`UI.arc(Element el)` provides a named escape hatch for attaching raw Arc `Element` instances with no Solim equivalent, replacing the `component(() -> someElement)` workaround. The deliberate name discourages casual misuse.

## Requirements

### Requirement: UI.arc escape-hatch for raw Arc elements
`UI` SHALL expose a static `arc(Element el)` method that attaches a raw Arc `Element` to the current `ParentStack` parent and returns it. This replaces the `component(() -> rawElement)` workaround for Arc elements that do not extend `BaseComponent`. The method is intentionally named `arc` — not `element` — to signal that it is an escape hatch for direct Arc layer access, making casual misuse for things like `arc(new Label("hi"))` visually incongruent with the available Solim equivalent `text("hi")`.

#### Scenario: Raw Arc element attaches via arc()
- **WHEN** `arc(new SchematicImage(schematic).setScaling(Scaling.fit))` is called inside a `children()` block
- **THEN** the `SchematicImage` is attached to the current parent table

#### Scenario: arc() returns the element for chaining
- **WHEN** `Element img = arc(new SchematicImage(s))` is called
- **THEN** the return value is the same `SchematicImage` instance passed in

#### Scenario: arc() outside a children scope is a no-op attachment
- **WHEN** `arc(someEl)` is called with no active `ParentStack` parent
- **THEN** no exception is thrown and the element is not attached to any table (consistent with `attachToParent` no-op behavior)
