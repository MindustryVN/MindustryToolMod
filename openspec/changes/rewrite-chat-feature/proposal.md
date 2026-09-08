## Why

The legacy global chat implementation (`old.mindustrytool.features.chat.global.*`) relies on deprecated Arc imperative UI (`Table`, `Label`, `Cell`, `Stack`), legacy networking, manual event/state subscriptions, and raw `Core.settings` keys. Rewriting `ChatFeature` under modern `mindustrytool.features.chat` using 100% Solim declarative components, `ConfigGroup`/`ConfigValue` for configuration, Solim signals for reactivity, and `mindustrytool.services.MindustryTool` will align the chat system with project standards, modernize the UI, and eliminate legacy dependencies.

## What Changes

- **New `ChatFeature`**: Implement modern feature extending `mindustrytool.features.Feature` with metadata, `ConfigGroup`, and `ConfigValue` fields.
- **Config Management**: Replace `ChatConfig` with type-safe `ConfigValue<Float>`, `ConfigValue<Boolean>`, and `ConfigValue<Long>` bound to reactive signals.
- **Chat State Management**: Implement a reactive chat store/service using Solim signals (`Signal<T>`, `Readable<T>`, `Computed<T>`) integrated with `MindustryTool` APIs and event streams.
- **100% Solim Chat Overlay HUD**: Implement `ChatOverlayHudView` entirely with declarative Solim components (`hud`, `column`, `row`, `card`, `scroll`, `dynamic`, `forEach`, `text`, `button`, `textField`), supporting both collapsed badge mode and expanded responsive window.
- **Chat Subviews in Solim**:
  - `ChatChannelListView`: Reactive list of channels with unread badges.
  - `ChatMessageListView`: Virtualized/scrollable reactive message feed with sender badges, replies, and timestamps.
  - `ChatInputView`: Solim text input field with send button, emoji trigger, and attachment support.
  - `ChatUserListView`: Online channel members list with statuses.
- **Solim Settings Dialog**: Implement `ChatSettingsDialog` and `ChatSettingsView` with declarative Solim sliders, checkboxes, and reset buttons.
- **Internationalization**: Add all user-facing strings to `assets/bundles/bundle.properties` with clear contextual comments.

## Capabilities

### New Capabilities
- `chat-feature`: Feature lifecycle, `ConfigGroup`/`ConfigValue` configurations, reactive chat store, and integration with `MindustryTool` chat services and SSE stream.
- `chat-overlay`: 100% Solim declarative HUD overlay providing collapsed badge view, expanded window, channel selection, message feed, user list, and message composer.
- `chat-settings`: Declarative Solim settings dialog and view allowing configuration of chat opacity, scale, dimensions, and positioning.

### Modified Capabilities
<!-- None. Legacy chat in old/ is untouched; no existing openspec specs require requirement changes. -->

## Impact

- **New classes**: `mindustrytool.features.chat.*` (`ChatFeature`, `ChatStore`, `ChatService`, `ChatOverlayHudView`, `ChatSettingsDialog`, `ChatSettingsView`, and subcomponents).
- **Existing services**: Utilizes existing `mindustrytool.services.MindustryTool` chat API endpoints and `mindustrytool.models.*`.
- **Localization**: Updates `assets/bundles/bundle.properties` with `feature.chat.*` keys.
- **Legacy code**: `old/mindustrytool/features/chat/global/*` remains untouched as required by legacy rules.
