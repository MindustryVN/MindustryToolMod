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

### Requirement: Top-Left Alignment of Message List Items
The message list and all item contents SHALL align to the top-left rather than being centered.

#### Scenario: Rendering message feed items
- **WHEN** messages are displayed in `ChatMessageListView`
- **THEN** message cards, author headers, text bodies, cards, and action buttons align to the top and left edges of the viewport.

#### Scenario: Avatar vertical alignment
- **WHEN** a multi-line message is rendered in `ChatMessageListView`
- **THEN** the author avatar is aligned to the top-left of the message row and does not center vertically within the row.

### Requirement: Enforced Avatar Dimensions
User avatars in the message list and member list SHALL have fixed dimensions regardless of downloaded image resolution.

#### Scenario: Displaying avatars with network images
- **WHEN** user avatars are rendered in `ChatMessageListView` or `ChatUserListView`
- **THEN** the avatar widget maintains a fixed size (unit(8) in message list, unit(6) in user list) preventing layout shifting or resizing.

### Requirement: Scroll Position Initialization and Preservation
The chat message list SHALL initialize scroll position at the bottom and preserve relative scroll position when messages update.

#### Scenario: Opening message list or switching channel
- **WHEN** a user opens the chat overlay or switches to a different channel
- **THEN** the scroll view is scrolled to the bottom displaying the most recent messages.

#### Scenario: Older messages prepended
- **WHEN** older messages are loaded into the message feed
- **THEN** the scroll position adjusts by the height of newly prepended content so the user's view remains anchored to their previous position.

#### Scenario: New message received while at bottom
- **WHEN** a new incoming message is appended to the message feed and the user was scrolled near the bottom
- **THEN** the view automatically scrolls down to display the new message.

### Requirement: Persistent Chat Input Focus
The chat composer input field SHALL maintain focus while typing and avoid unmounting on keypress.

#### Scenario: User types message text
- **WHEN** the user types characters into the chat text field
- **THEN** the input component remains continuously mounted and does not lose focus between keystrokes.

