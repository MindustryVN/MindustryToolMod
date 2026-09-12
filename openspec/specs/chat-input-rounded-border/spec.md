# chat-input-rounded-border Specification

## Purpose
Styles the chat composer message input with subtle rounded corners and a dark gray outline, including while focused, so it reads as a distinct modern input.

## Requirements
### Requirement: Rounded bordered chat input field
The chat composer message input SHALL render with 12px rounded corners and a 1.5px dark gray border.

#### Scenario: Composer input shows rounded bordered styling
- **WHEN** the chat composer is displayed while logged in
- **THEN** the message text field has 12px rounded corners with a 1.5px dark gray outline

#### Scenario: Rounding persists while typing
- **WHEN** the message text field gains focus
- **THEN** the field keeps the same 12px rounded corners and border instead of reverting to a square background
