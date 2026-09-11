## MODIFIED Requirements

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
