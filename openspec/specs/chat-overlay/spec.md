# chat-overlay Specification

## Purpose
Provides a declarative Solim HUD overlay for in-game chat with multi-pane desktop layout, tabbed mobile view, and collapsed draggable badge mode.

## Requirements
### Requirement: Declarative Solim HUD Overlay
The system SHALL provide a ChatOverlayHudView implemented exclusively using declarative Solim components (solim.ui.Ui.*), with zero direct Arc scene widgets (Table, Label, Button, Cell, Stack).

#### Scenario: Overlay rendering
- **WHEN** the chat overlay builds its view hierarchy
- **THEN** it produces a root Hud component with reactive bindings for opacity, position, scale, and visibility

#### Scenario: Visibility synchronization
- **WHEN** the game HUD is hidden or not visible
- **THEN** the chat overlay automatically hides without tearing down reactive bindings

### Requirement: Collapsed Badge Mode
The system SHALL provide a collapsed floating badge display when collapsedConfig is true, showing a draggable pill with unread message count and connection status indicator.

#### Scenario: Expanding from collapsed badge
- **WHEN** the user clicks on the collapsed badge
- **THEN** collapsedConfig is set to false and the expanded chat window is displayed

#### Scenario: Unread badge count update
- **WHEN** unread messages arrive while collapsed
- **THEN** the badge label updates reactively to reflect the current unread count

### Requirement: Expanded Chat Window
The system SHALL provide an expanded chat view consisting of a header, channel navigation, message feed, user roster, and composer input area.

#### Scenario: Collapsing the chat window
- **WHEN** the user clicks the collapse button or presses the Escape key
- **THEN** the overlay transitions to the collapsed badge mode

#### Scenario: Sending a chat message
- **WHEN** the user enters message text in ChatInputView and clicks send or presses Enter
- **THEN** MindustryTool.sendChatMessage() is executed, the input is cleared, and the message appears in the feed

### Requirement: Responsive Mobile and Desktop Layout
The system SHALL provide a multi-pane layout on desktop screens and a tabbed navigation interface (Channels, Messages, Members) on mobile devices (Vars.mobile).

#### Scenario: Switching tabs on mobile
- **WHEN** a user on a mobile device clicks a tab in the mobile tab bar
- **THEN** the corresponding view panel is displayed using the Solim Tabs component
