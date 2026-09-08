## ADDED Requirements

### Requirement: Feature Lifecycle and Registration
The system SHALL provide a `ChatFeature` extending `mindustrytool.features.Feature` with metadata (`id: "chat"`, name, description, icon, enabled by default, quick-access enabled), managing lifecycle activation and deactivation cleanly.

#### Scenario: Feature enabled
- **WHEN** `ChatFeature.onEnable()` is called
- **THEN** the chat overlay HUD is instantiated and added to the scene, and network connections/streams are initialized

#### Scenario: Feature disabled
- **WHEN** `ChatFeature.onDisable()` is called
- **THEN** the chat overlay HUD is removed and disposed, and network connections/streams are disconnected

### Requirement: Configuration via ConfigValue
The system SHALL manage all persistent chat settings using `ConfigGroup` and `ConfigValue<T>` instances, exposing reactive signals for each setting.

#### Scenario: Reading and updating opacity configuration
- **WHEN** `opacityConfig` value is changed
- **THEN** the underlying setting is persisted and its reactive signal notifies observers with the new value

#### Scenario: Collapsed state persistence
- **WHEN** the chat is collapsed or expanded
- **THEN** `collapsedConfig` updates and persists the boolean state

### Requirement: Reactive Chat State Management
The system SHALL maintain single-source-of-truth chat state in `ChatStore` using Solim `Signal<T>` instances for channel list, active channel, messages per channel, unread count, and connection state.

#### Scenario: New message received in inactive channel
- **WHEN** a message is received for a channel that is not currently active
- **THEN** the message is appended to that channel's message list and unread count is incremented

#### Scenario: Selecting an active channel
- **WHEN** a channel is selected as active
- **THEN** active channel signal updates, unread count for that channel is cleared, and its last read message is recorded

### Requirement: MindustryTool Service Integration
The system SHALL interact with chat REST endpoints and SSE event streams via `mindustrytool.services.MindustryTool` and marshal state updates to the main thread via `Core.app.post()`.

#### Scenario: Initializing chat data
- **WHEN** `ChatService.init()` is invoked
- **THEN** channels and initial messages are fetched via `MindustryTool.getChatChannels()` and `MindustryTool.getChatMessages()`, and the live SSE stream is connected
