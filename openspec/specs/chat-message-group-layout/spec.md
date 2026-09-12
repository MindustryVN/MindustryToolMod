# chat-message-group-layout Specification

## Purpose
Groups consecutive chat messages from the same author into visual message groups sharing a single 48px avatar, with stacked message rows and distinct inter-group spacing in the virtualized chat list.

## Requirements
### Requirement: Consecutive message grouping into MessageGroup
The system SHALL group consecutive chat messages from the same author into a composite `MessageGroup` object containing the author ID, the creation time of the first message, and the list of ordered messages.

#### Scenario: Consecutive messages from the same author are grouped
- **WHEN** multiple messages are received consecutively from author A
- **THEN** they are aggregated into a single `MessageGroup` containing author A's messages in order

#### Scenario: Message from different author starts a new group
- **WHEN** a message is received from author B after messages from author A
- **THEN** a new `MessageGroup` is started for author B

### Requirement: Group-level avatar and stacked message flow
The system SHALL render a fixed 48px avatar (`unit(12)`) at the top-left of each `MessageGroup` and stack all messages from that author in a right-hand vertical column with a uniform 3px gap (`unit(0.75f)`), allowing subsequent messages to flow naturally beside the avatar without internal voids.

#### Scenario: Multi-message group flows beside the avatar
- **WHEN** a group contains multiple messages from the same author
- **THEN** the avatar sits on the left while the messages stack tightly in the right column, with the second message appearing directly below the first

#### Scenario: Single-line first message does not create a dead void
- **WHEN** the first message in a group has a short single-line text and a second message follows
- **THEN** the second message is positioned immediately below the first message with a 3px gap beside the 48px avatar

### Requirement: Inter-group spacing in virtualized list
The system SHALL separate adjacent `MessageGroup` components in the virtual list with a distinct visual gap of 3px (`unit(0.75f)`).

#### Scenario: Adjacent groups separated by virtual list gap
- **WHEN** messages from different authors are rendered in the virtual list
- **THEN** a 3px gap separates the bottom of one author's group and the top of the next author's group

### Requirement: MessageGroup layout height calculation
The layout height calculator SHALL compute the height of a `MessageGroup` by taking the maximum of the avatar height (48px) and the right column height (header height + sum of message heights + internal message gaps) plus vertical padding.

#### Scenario: Group height reflects accumulated message contents
- **WHEN** the height of a `MessageGroup` is calculated for a given container width
- **THEN** the result accounts for the header, all contained messages, and internal gaps, with a minimum height bound by the 48px avatar

### Requirement: Individual message actions preserved in group view
The system SHALL preserve click handling on individual message elements within a `MessageGroup` to display action options (copy, reply, translate) for that specific message.

#### Scenario: Clicking a specific message in a group opens its action dialog
- **WHEN** the user clicks on any message row inside a multi-message group
- **THEN** the message action dialog opens targeting that specific message
