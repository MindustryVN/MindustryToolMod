# optimistic-message-send Specification

## Purpose
Provides optimistic message display with pending/failed states, temp-to-real message reconciliation, and retry on failure for the in-game chat feature.

## Requirements
### Requirement: Optimistic message display
The system SHALL display a temporary message in the chat list immediately when the user sends a message, before the server confirms delivery.

#### Scenario: Message appears instantly
- **WHEN** the user sends a message and the send request is in flight
- **THEN** a temporary message with the message content is appended to the active channel's message list at 50% opacity

#### Scenario: Temporary message has client-generated ID
- **WHEN** a temporary message is created for optimistic display
- **THEN** its ID SHALL be prefixed with `temp_` followed by a UUID, guaranteeing no collision with server-assigned IDs

### Requirement: Message confirmation on server response
The system SHALL replace the temporary message with the server-confirmed message when the HTTP response arrives.

#### Scenario: Successful send replaces temp message
- **WHEN** the server responds with a confirmed message and the temp message is still in the list
- **THEN** the temp message is replaced in-place with the real message and the opacity transitions to 100%

#### Scenario: SSE race — real message already delivered
- **WHEN** the server responds and the real message is already in the list (delivered via SSE)
- **THEN** the temp message is removed from the list without adding a duplicate

#### Scenario: Temp already removed (channel switch)
- **WHEN** the server responds but the temp message is no longer in the list (user switched channels)
- **THEN** the system performs no list modification

### Requirement: Send failure handling
The system SHALL keep the failed message visible with an error indication when the send request fails.

#### Scenario: Failed message stays visible
- **WHEN** the send request fails (network error, server error, rate limit)
- **THEN** the temp message remains in the list with 50% opacity and a red color tint

#### Scenario: Retry button on failed message
- **WHEN** a message has failed to send
- **THEN** a retry button is displayed on or near the failed message

#### Scenario: Retry transitions to pending
- **WHEN** the user taps the retry button on a failed message
- **THEN** the message transitions from failed state back to pending state (50% opacity, no red tint) and the send is re-attempted with the same content

#### Scenario: Retry success
- **WHEN** a retried send request succeeds
- **THEN** the message is confirmed (100% opacity) following the same replacement logic as a first-time send

### Requirement: One pending message at a time
The system SHALL prevent concurrent optimistic sends by disabling further sends while a message is pending or being retried.

#### Scenario: Send disabled while pending
- **WHEN** a message is currently in pending or failed state
- **THEN** the send button and enter-to-send are disabled until the current send completes or fails
