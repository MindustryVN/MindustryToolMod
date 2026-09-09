## ADDED Requirements

### Requirement: SolimDialog component
The test suite SHALL verify SolimDialog provides modal dialog behavior.

#### Scenario: Dialog construction
- **WHEN** a SolimDialog is created and built
- **THEN** it SHALL extend BaseDialog and implement Component

#### Scenario: Dialog show
- **WHEN** `show()` is called
- **THEN** the dialog SHALL be added to the scene

#### Scenario: Dialog close
- **WHEN** the dialog is closed
- **THEN** its dispose method SHALL be called

### Requirement: Hud component
The test suite SHALL verify Hud renders an overlay on the game HUD.

#### Scenario: Hud construction
- **WHEN** a Hud is created and built
- **THEN** it SHALL return a container element

#### Scenario: Hud with children
- **WHEN** children are added to the Hud
- **THEN** they SHALL be rendered as HUD overlays

#### Scenario: Hud layout modifiers
- **WHEN** layout modifiers are applied
- **THEN** the HUD container SHALL respect those constraints
