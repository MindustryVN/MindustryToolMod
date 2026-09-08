## Context

The legacy chat feature (`old.mindustrytool.features.chat.global.*`) was constructed using direct Arc UI widgets (`arc.scene.ui.Table`, `Label`, `Button`, `Cell`, `Stack`), imperative layout mutations (`rebuild()`, `clearChildren()`), manual event subscriptions, and raw `Core.settings` calls without type-safe configuration.

The project now mandates:
1. Feature-oriented architecture under `mindustrytool.features.*`.
2. 100% declarative Solim UI (`solim.ui.Ui.*`) with zero direct Arc scene widgets.
3. Centralized type-safe configuration via `ConfigGroup` and `ConfigValue`.
4. Reactive bindings via Solim `Signal<T>`, `Readable<T>`, and `Computed<T>`, avoiding static `.get()` unwrapping in `build()`.
5. Modern HTTP networking via `mindustrytool.services.MindustryTool`.
6. Full internationalization in `assets/bundles/bundle.properties`.

## Goals / Non-Goals

**Goals:**
- Implement `ChatFeature` under `mindustrytool.features.chat` extending `mindustrytool.features.Feature`.
- Manage chat configuration using `ConfigGroup` and `ConfigValue<T>` for opacity, scale, width, height, coordinates, and collapsed state.
- Implement `ChatStore` and `ChatService` managing chat channels, message history, online users, unread counts, and connection state with Solim reactive signals.
- Implement `ChatOverlayHudView` as a 100% Solim HUD overlay supporting collapsed floating badge mode and expanded responsive modal/window mode.
- Break down the chat interface into cohesive declarative Solim subcomponents: `ChatChannelListView`, `ChatMessageListView`, `ChatInputView`, `ChatUserListView`.
- Implement `ChatSettingsDialog` and `ChatSettingsView` using 100% Solim components.
- Provide responsive mobile and desktop view modes (tabbed for mobile, multi-column for desktop).
- Add all required bundle translation keys and comments to `assets/bundles/bundle.properties`.

**Non-Goals:**
- Modifying legacy code in `old/mindustrytool/features/chat/*` (preserved per project rules).
- Implementing translation services (e.g., DeepL/Gemini translation) within this change (separate feature).
- Changing backend API specifications.

## Decisions

### 1. Architecture & Component Decomposition
We organize the chat feature under `mindustrytool.features.chat`:
- `ChatFeature`: Feature entry point, owns `ConfigGroup`, `ConfigValue` definitions, lifecycle (`init`, `onEnable`, `onDisable`), and HUD mounting.
- `ChatStore`: Holds single-source-of-truth reactive state (`Signal<List<ChannelDto>>`, `Signal<String> activeChannelId`, `Signal<Map<String, List<ChatMessage>>>`, `Signal<Integer> unreadCount`, `Signal<Boolean> connected`, `Signal<Boolean> collapsed`).
- `ChatService`: Coordinates REST requests (`MindustryTool.getChatChannels`, `getChatMessages`, `sendChatMessage`, `getChatUsers`) and SSE event stream (`MindustryTool.chatStream`). Marshals updates to `ChatStore` on the main thread via `Core.app.post()`.
- `ChatOverlayHudView`: Root Solim HUD component extending `BaseComponent`, rendering the collapsed floating pill/badge or the full expanded chat dialog.
- `ChatChannelListView`: Declarative Solim component rendering channel items with unread badges and active selection.
- `ChatMessageListView`: Declarative Solim component rendering messages with user avatars/names, reply quotes, and auto-scrolling.
- `ChatInputView`: Declarative Solim component with `SolimTextField`, send action button, reply-cancel chip, and key event listeners.
- `ChatUserListView`: Declarative Solim component listing online users in the active channel.
- `ChatSettingsDialog` & `ChatSettingsView`: Solim dialog offering sliders for opacity, scale, dimensions, and reset actions.

### 2. 100% Solim UI with Zero Direct Arc Scene Widgets
- All layout structure uses `hud()`, `column()`, `row()`, `card()`, `scroll()`, `divider()`, `spacer()`.
- Interactive controls use `button()`, `textField()`, `slider()`, `checkbox()`.
- Text rendering uses `text()`.
- Collections and conditionals use `dynamic()` and `forEach()`.
- No `new Table()`, `new Label()`, `new Cell()`, or `new Button()` from Arc.

### 3. Reactive State via Solim Signals
- All mutable state is encapsulated in `Signal<T>` and exposed as `Readable<T>`.
- Components bind directly to `Readable<T>` or derive values with `.map()`.
- No component calls `.get()` inside `build()`, maintaining strict continuous reactivity.
- Settings are bound to `ConfigValue.signal()`.

### 4. Responsive Layout (Mobile vs Desktop)
- On desktop: Multi-pane layout (Channel List | Message Feed + Input | User List).
- On mobile (`Vars.mobile`): Tabbed navigation (Channels / Messages / Members) driven by a `Signal<MobileTab>` and `Ui.dynamic()`.

## Risks / Trade-offs

- **[Risk] SSE streaming connection lifecycle and network drops.**
  - *Mitigation*: Implement reconnect timer loop in `ChatService` and reflect live connection state in `ChatStore.connectedSignal` so UI displays offline/connecting indicators.
- **[Risk] Large message list memory and layout overhead.**
  - *Mitigation*: Cap in-memory history per channel (e.g. 100 messages max) and load older messages incrementally when scrolled to top.
- **[Risk] Focus and keyboard navigation (Escape to close, Enter to send).**
  - *Mitigation*: Solim `SolimTextField` provides key listener bindings; attach escape handler to collapse chat when active.
