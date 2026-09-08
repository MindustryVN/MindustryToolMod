## ADDED Requirements

### Requirement: Solim Settings View and Dialog
The system SHALL provide `ChatSettingsDialog` and `ChatSettingsView` built exclusively with declarative Solim components (`solim.ui.Ui.*`) to configure chat preferences.

#### Scenario: Opening settings dialog
- **WHEN** `ChatFeature.getSettingDialog()` is requested
- **THEN** a Solim dialog hosting `ChatSettingsView` is returned

### Requirement: Interactive Settings Controls
The system SHALL provide reactive sliders for adjusting chat opacity (0.2 - 1.0), scale (0.5 - 1.5), width (0.4 - 1.0), and height (0.4 - 1.0), directly bound to `ConfigValue.signal()`.

#### Scenario: Adjusting opacity slider
- **WHEN** the user slides the opacity control in `ChatSettingsView`
- **THEN** the opacity configuration updates immediately and updates the chat overlay HUD

### Requirement: Reset Chat Preferences
The system SHALL provide a reset action to restore chat window position, dimensions, opacity, and scale back to default values.

#### Scenario: Triggering reset position and settings
- **WHEN** the user clicks the reset button in `ChatSettingsView`
- **THEN** default position and dimensions are applied to the configurations and reflected in the overlay
