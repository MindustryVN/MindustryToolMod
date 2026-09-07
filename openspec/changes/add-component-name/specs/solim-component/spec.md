## MODIFIED Requirements

### Requirement: Component interface
The framework SHALL provide `solim.core.Component` with `arc.scene.Element element()`, `default void dispose()`, and `default Component name(String name)` to rename the component's underlying Arc `Element`.

#### Scenario: Component returns Element
- **WHEN** `Component c = new MyComponent()` and `Element e = c.element()` is called
- **THEN** `e` is the Arc `Element` built by the component (non-null after build)

#### Scenario: Default dispose is no-op
- **WHEN** a `Component` does not override `dispose()`
- **THEN** calling `dispose()` does not throw and is safe

#### Scenario: Default name sets element name
- **WHEN** `Component c = new MyComponent()` and `c.name("custom-name")` is called
- **THEN** `c.element().name` equals `"custom-name"` and `c` is returned
