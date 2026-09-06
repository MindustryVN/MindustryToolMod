## MODIFIED Requirements

### Requirement: Feature Card Display and Interaction
The dialog SHALL present each feature as an interactive `FeatureCard` component displaying its icon, title, description, enabled/disabled status, and action shortcuts, updating its visual state reactively in-place without component recreation.

#### Scenario: Toggle feature state
- **WHEN** the user clicks a feature card outside of its action shortcut buttons
- **THEN** the feature's enabled state is toggled and the card's background color, status label text, and status label color update immediately via direct reactive property bindings on the existing elements without rebuilding the card

#### Scenario: Feature action shortcut invocation
- **WHEN** the user clicks an action shortcut on a card (main dialog, settings dialog, or help)
- **THEN** the corresponding dialog is opened and the click event does not propagate to toggle the card's enabled state

#### Scenario: Instantiation without parent table
- **WHEN** a `FeatureCard` is created
- **THEN** it is instantiated via constructor without passing a parent `Table`, and its lifecycle and dimensions are managed reactively by its parent container

### Requirement: Responsive Grid and Lifecycle Disposal
The feature settings dialog SHALL adapt its grid layout dynamically to screen size and preserve its view instance across show and hide events, disposing of reactive resources only upon permanent dialog disposal.

#### Scenario: Viewport size changed
- **WHEN** the window is resized while the dialog is visible
- **THEN** the layout column count is recalculated reactively, the grid reflows, and existing card instances are preserved

#### Scenario: Dialog hidden and reopened
- **WHEN** the dialog is hidden and subsequently shown again
- **THEN** the existing `FeatureSettingsView` instance is reused without disposal or recreation, and its width and data are refreshed via `onShown`

#### Scenario: Permanent dialog disposal
- **WHEN** the dialog is permanently disposed
- **THEN** all associated view components, reactive property bindings, and event listeners are cleanly disposed
