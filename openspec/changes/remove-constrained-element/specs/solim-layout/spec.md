## MODIFIED Requirements

### Requirement: Use Arc-native layout mechanisms
All layout primitives and components SHALL delegate to Arc's existing `Table`, `Stack`, `ScrollPane`, and `Cell` APIs and SHALL NOT reimplement or alter Arc's layout engine. Layout containers and components SHALL NOT implement `ConstrainedElement`, SHALL NOT subclass Arc widgets to override `getPrefWidth`, `getPrefHeight`, `getMinWidth`, `getMinHeight`, `getMaxWidth`, or `getMaxHeight`, and SHALL NOT alter Arc's native layout calculations. Layout sizing, expansion, padding, and alignment SHALL be configured through native Arc `Cell` and `Element` properties.

#### Scenario: No custom layout engine or measurement overrides
- **WHEN** layout containers (`Row`, `Column`, `Grid`, `Scroll`, `Dynamic`, `ForEach`, `ReactiveGrid`) and Solim widgets are inspected
- **THEN** they wrap standard Arc widgets directly without subclassing to override layout measurement methods or implementing `ConstrainedElement`

## REMOVED Requirements

### Requirement: ConstrainedElement interface
**Reason**: `ConstrainedElement` introduced custom subclasses of Arc widgets that overrode Arc layout calculation methods and violated the principle that Solim components must only wrap Arc without changing its behavior.
**Migration**: Use standard Arc `Element` and `Cell` APIs for sizing and layout configuration.
