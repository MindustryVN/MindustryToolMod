# solim-tabs Specification

## Purpose
Provides a declarative tabs component for organizing Solim views into selectable tab panels with reactive active tab binding.
## Requirements
### Requirement: Declarative Tabs Component
The system SHALL provide a Tabs component (solim.layout.Tabs, Ui.tabs) for organizing views into selectable tab panels.

#### Scenario: Switching active tab
- **WHEN** user clicks on a tab header or the active tab signal changes value
- **THEN** the active tab button updates its visual state and the corresponding tab content is displayed.

#### Scenario: Reactive active tab binding
- **WHEN** 	abs is constructed with a Signal<Integer> activeTab index
- **THEN** mutations to the signal change the displayed tab, and selecting a tab button writes the new index into the signal.

### Requirement: Lazy Tab Mounting and Inactive Tab Layout Isolation
The Tabs component SHALL support lazy tab mounting where tab content is instantiated only when first selected, and inactive tab containers SHALL have their layout updates disabled (`setLayoutEnabled(false)`) so inactive subtrees do not participate in scene layout validation or consume CPU cycles.

#### Scenario: Inactive tabs do not build until selected
- **WHEN** a tab container is constructed with multiple tabs
- **THEN** only the currently active tab's content builder is initially executed and mounted.

#### Scenario: Inactive tab layout disabled
- **WHEN** a tab is not active
- **THEN** its container element has `setLayoutEnabled(false)` and `setVisible(false)` applied, preventing layout propagation from affecting the parent scene.

