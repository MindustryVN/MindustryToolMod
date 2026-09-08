## 1. Localization & Bundle Keys

- [x] 1.1 Add chat feature translation keys and comments to `assets/bundles/bundle.properties` (`feature.chat.name`, `feature.chat.description`, `feature.chat.help`, `feature.chat.settings.*`, `feature.chat.ui.*`).

## 2. Feature Configuration & State Management

- [x] 2.1 Implement `ChatFeature` under `mindustrytool.features.chat` extending `Feature` with `ConfigGroup` and `ConfigValue` definitions for opacity, scale, width, height, coordinates, and collapsed state.
- [x] 2.2 Implement `ChatStore` holding single-source-of-truth reactive state (`Signal`s for channels, active channel, messages, users, unread counts, connection state).
- [x] 2.3 Implement `ChatService` coordinating network calls to `MindustryTool` REST APIs, SSE stream subscription, periodic reconnection, and thread-safe store updates.

## 3. Solim Chat Subviews

- [x] 3.1 Implement `ChatChannelListView` as a declarative Solim component displaying channels with unread badges and active selection indicator.
- [x] 3.2 Implement `ChatMessageListView` as a declarative Solim component with scrollable message feed, sender badges, message cards, and auto-scroll behavior.
- [x] 3.3 Implement `ChatInputView` as a declarative Solim component featuring `SolimTextField`, send button, and keyboard shortcuts (Enter/Escape).
- [x] 3.4 Implement `ChatUserListView` as a declarative Solim component listing online members with status indicators.

## 4. Solim Chat Overlay HUD

- [x] 4.1 Implement `ChatOverlayHudView` as a declarative Solim HUD component with responsive multi-pane layout for desktop and tabbed view for mobile.
- [x] 4.2 Implement collapsed floating badge mode with unread counter, connection indicator, and drag handling.
- [x] 4.3 Integrate `ChatOverlayHudView` lifecycle with `ChatFeature` (`onEnable`, `onDisable`, `Vars.ui.hudGroup`).

## 5. Solim Settings Dialog

- [x] 5.1 Implement `ChatSettingsView` with declarative Solim sliders for opacity, scale, dimensions, and reset actions.
- [x] 5.2 Implement `ChatSettingsDialog` wrapping `ChatSettingsView` and link to `ChatFeature.getSettingDialog()`.

## 6. Registration & Verification

- [x] 6.1 Register `ChatFeature` in `FeatureManager` and verify build compatibility.
- [x] 6.2 Verify Java 8 runtime compatibility and adherence to Solim-only, zero direct Arc UI rules.
