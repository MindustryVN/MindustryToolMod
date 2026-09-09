## ADDED Requirements

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
