## ADDED Requirements

### Requirement: Declarative Badge Component
The system SHALL provide a `Badge` component (`solim.display.Badge`, `Ui.badge`) for compact tag and counter indicators.

#### Scenario: Rendering text badge
- **WHEN** `badge("5")` or `badge(unreadSignal)` is added to a layout
- **THEN** a pill-shaped badge element is rendered with formatted label content.

#### Scenario: Hiding badge on zero count
- **WHEN** a badge is configured with an unread integer signal that reaches 0
- **THEN** the badge automatically hides itself if `hideOnZero` is enabled.
