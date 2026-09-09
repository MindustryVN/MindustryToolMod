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
