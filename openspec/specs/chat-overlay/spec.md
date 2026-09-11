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
The system SHALL provide a collapsed floating badge display when collapsedConfig is true, showing a single draggable pill button that also expands the chat on click, plus a floating (zero-layout-space) connection status indicator.

#### Scenario: Single button drags and expands
- **WHEN** the user drags the collapsed badge
- **THEN** the entire badge moves with the pointer via the draggable binding without opening the chat

#### Scenario: Expanding from collapsed badge
- **WHEN** the user clicks (without dragging) the collapsed badge
- **THEN** collapsedConfig is set to false and the expanded chat window is displayed

#### Scenario: Connection status dot is floating
- **WHEN** the collapsed badge is rendered
- **THEN** the connection-status indicator is positioned as a floating overlay on the chat icon and contributes zero width and zero height to the row layout

#### Scenario: No separate drag-handle icon
- **WHEN** the collapsed badge is displayed
- **THEN** only one icon (chat icon) is visible and no separate move/drag handle icon is rendered

#### Scenario: Unread badge count update
- **WHEN** unread messages arrive while collapsed
- **THEN** the badge label updates reactively to reflect the current unread count

### Requirement: Expanded Chat Window
The system SHALL provide an expanded chat view consisting of a header action bar, channel navigation, message feed, user roster, and composer input area. The action bar header SHALL render with a white background (`Tex.whiteui`) and SHALL be draggable across its entire area (including title text, status indicator, and spacer background) while keeping settings and collapse buttons fully clickable. The message feed SHALL wrap all message text cleanly within the message card width without horizontal overflow.

#### Scenario: Collapsing the chat window
- **WHEN** the user clicks the collapse button or presses the Escape key
- **THEN** the overlay transitions to the collapsed badge mode

#### Scenario: Sending a chat message
- **WHEN** the user enters message text in ChatInputView and clicks send or presses Enter
- **THEN** MindustryTool.sendChatMessage() is executed, the input is cleared, and the message appears in the feed

#### Scenario: Dragging from any non-button area of the action bar
- **WHEN** the user touches down and drags on the action bar background, title text, or spacer
- **THEN** the entire chat window moves with the drag gesture and updates the position signals

#### Scenario: Clicking action buttons on the white action bar
- **WHEN** the user clicks the settings button or collapse button on the white action bar
- **THEN** the respective button action fires (opens settings or collapses chat) and no drag is initiated

#### Scenario: Long text messages wrap without horizontal overflow
- **WHEN** a message with long unbroken text or lengthy paragraphs is rendered in the message feed
- **THEN** the text wraps cleanly within the bounds of the message list and does not expand the card or scroll pane horizontally


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

### Requirement: Reply Section Layout
The chat input composer SHALL render the reply-target row as a fixed layout element that is visible only when a reply target is set, with zero height and no consumed space when not replying.

#### Scenario: Reply row hidden when no reply target
- **WHEN** the store has no reply target (replyTarget is null)
- **THEN** the reply row has setVisible(false) and setLayoutEnabled(false) applied, occupying no vertical space in the composer

#### Scenario: Reply row shown when replying
- **WHEN** the store has a non-null reply target
- **THEN** the reply row becomes visible and shows the target author name with a cancel button

#### Scenario: Cancelling a reply
- **WHEN** the user clicks the cancel button in the reply row
- **THEN** store.setReplyTarget(null) is called and the reply row collapses from layout

