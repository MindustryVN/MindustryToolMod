## MODIFIED Requirements

### Requirement: Quick Access Settings Configuration
The system SHALL provide a settings dialog to customize HUD opacity, scale, grid columns, and individual feature visibility on the Quick Access HUD.

#### Scenario: Changing HUD parameters updates HUD reactively
- **WHEN** the user modifies opacity, scale, columns, or feature visibility in the Quick Access settings dialog
- **THEN** the HUD updates its styling and layout reactively and saves settings immediately.

#### Scenario: Declarative Settings Dialog Structure
- **WHEN** `QuickAccessSettingsDialog` is constructed
- **THEN** it configures slider and checkbox inputs without manual `.subscribe()` calls or temporary single-use local variables, utilizing declarative chained row layouts (`row().gap(...).children(...)`).
