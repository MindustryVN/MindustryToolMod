# solim-tabs Delta Specification

## ADDED Requirements

### Requirement: Lazy Tab Mounting and Inactive Tab Layout Isolation
The Tabs component SHALL support lazy tab mounting where tab content is instantiated only when first selected, and inactive tab containers SHALL have their layout updates disabled (`setLayoutEnabled(false)`) so inactive subtrees do not participate in scene layout validation or consume CPU cycles.

#### Scenario: Inactive tabs do not build until selected
- **WHEN** a tab container is constructed with multiple tabs
- **THEN** only the currently active tab's content builder is initially executed and mounted.

#### Scenario: Inactive tab layout disabled
- **WHEN** a tab is not active
- **THEN** its container element has `setLayoutEnabled(false)` and `setVisible(false)` applied, preventing layout propagation from affecting the parent scene.
