# chat-overlay Delta Specification

## ADDED Requirements

### Requirement: Mobile Frame Rate Stability and Viewport Windowing
The chat overlay SHALL maintain a minimum target of 55+ FPS on mobile devices when expanded, by restricting the active instantiated element hierarchy in `ChatMessageListView` to a bounded viewport window of recent messages instead of retaining unbounded off-screen message cards.

#### Scenario: Expanding chat window on mobile
- **WHEN** the chat overlay transitions from collapsed to expanded on a mobile device (`Vars.mobile == true`)
- **THEN** framerate remains stable above 55 FPS and does not drop by half.

#### Scenario: Viewport windowing in message feed
- **WHEN** more than 30 messages are loaded in the active channel on mobile
- **THEN** only the most recent visible messages (up to a bounded window of 25–30 items) are actively mounted in the Scene2D table hierarchy, while preserving scroll offset and pagination triggers.

### Requirement: Chat Layout Thrashing Prevention
The expanded chat overlay SHALL NOT trigger cyclic `invalidateHierarchy()` or size mutation during Scene `validate()` passes.

#### Scenario: Stable HUD root validation
- **WHEN** `ChatOverlayHudView` is rendered and position signals are updated
- **THEN** `HudRootTable.validate()` converges in a single pass without triggering re-entrant layout invalidations or setting position repeatedly.
