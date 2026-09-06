# feature-settings-dialog Specification

## Purpose
Declarative feature settings dialog and view built with Solim reactive signals and structural layout components to manage, filter, re-enable, and inspect mod features.
## Requirements
### Requirement: Declarative Search and Filter
The feature settings dialog SHALL provide a search input backed by a Solim reactive `Signal<String>` that filters mod features in real time across feature names, descriptions, and metadata IDs.

#### Scenario: User types search query
- **WHEN** the user enters text into the search field
- **THEN** the filter signal updates and the feature grid automatically updates to display only matching features using case-insensitive matching

#### Scenario: Empty search results state
- **WHEN** the search filter matches no registered features
- **THEN** an empty state indicator is rendered with localized text (`feature.search.empty`)

### Requirement: Bulk Feature Re-enablement
The feature settings dialog SHALL provide an action control to re-enable all registered features and reactively refresh the card display.

#### Scenario: Re-enable action triggered
- **WHEN** the user activates the re-enable button
- **THEN** `FeatureManager.reenable()` is invoked and all feature cards update their status reactively

### Requirement: Feature Card Display and Interaction
The dialog SHALL present each feature as an interactive card displaying its icon, title, description, enabled/disabled status, and action shortcuts.

#### Scenario: Toggle feature state
- **WHEN** the user clicks a feature card outside of its action shortcut buttons
- **THEN** the feature's enabled state is toggled and the card's visual status and border color update immediately

#### Scenario: Feature action shortcut invocation
- **WHEN** the user clicks an action shortcut on a card (main dialog, settings dialog, or help)
- **THEN** the corresponding dialog is opened and the click event does not propagate to toggle the card's enabled state

### Requirement: Responsive Grid and Lifecycle Disposal
The feature settings dialog SHALL adapt its grid layout dynamically to screen size and cleanly dispose of reactive effects upon dismissal.

#### Scenario: Viewport size changed
- **WHEN** the window is resized while the dialog is visible
- **THEN** the layout column count is recalculated reactively and the grid width re-flows

#### Scenario: Dialog closed
- **WHEN** the dialog is closed or hidden
- **THEN** all associated Solim effects, reactive bindings, and event listeners are properly disposed

