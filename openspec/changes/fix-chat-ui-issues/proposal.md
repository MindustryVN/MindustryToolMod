## Why

The in-game chat interface exhibits several significant UI and UX issues that impede usability:
1. User avatars in the message list and member list lack enforced fixed dimensions, causing layout jumps or oversized avatar rendering when network images load.
2. Message list items and inner elements (avatars, message bodies, cards, and buttons) default to centered alignment, leading to an awkward visual layout instead of standard top-left chat alignment.
3. When new or older messages are loaded into the feed, scroll position is reset or disrupted instead of being preserved, and opening the message feed does not start scrolled to the bottom.
4. Typing into the chat text input causes it to immediately lose focus on every keystroke because the component is unmounted and recreated due to reactive dependency leakage inside `Dynamic` switching.

Resolving these issues ensures a smooth, stable, and visually polished chat experience.

## What Changes

- **Fix Input Unfocusing and Component Unmounting**:
  - Introduce `ReactiveContext.untracked(...)` to isolate reactive tracking during component tree creation.
  - Update `Dynamic` to evaluate child component factories inside untracked execution so signal reads during construction (such as text input initial values) do not register as dependencies of the dynamic switcher.
  - Refactor `ChatInputView` so the text input field remains mounted across keystrokes.
- **Enforce Fixed Size for User Avatars**:
  - Update `NetworkImage` and `SizedImage` so explicit width and height constraints are strictly preserved and override the intrinsic dimensions of downloaded textures.
  - Ensure avatar components in `ChatMessageListView` and `ChatUserListView` are locked to fixed unit dimensions (e.g., 32x32 / 24x24 px).
- **Top-Left Alignment for Message List**:
  - Align message items, rows, columns, and embedded content (avatars, text bodies, previews, action buttons) to the top-left rather than center.
  - Ensure `solim.layout.Row` and `solim.layout.Column` respect top-left layout alignment for content items.
- **Scroll Position Management & Initial Scroll to Bottom**:
  - Automatically scroll to the bottom when the chat message feed is first opened or when switching channels.
  - Preserve relative scroll position when older messages are prepended to the top of the message list.
  - Auto-scroll to the bottom when new messages arrive if the user was already near the bottom.

## Capabilities

### New Capabilities
<!-- None: all changes improve existing chat and Solim capabilities -->

### Modified Capabilities
- `chat-overlay`: Add requirements for top-left alignment of message elements, fixed avatar sizing, initial scroll-to-bottom, scroll position preservation on message list updates, and stable chat input focus without unmounting.
- `solim-network-image`: Add requirement that explicit size constraints on `NetworkImage` and `SizedImage` must constrain the rendered element regardless of downloaded texture dimensions.
- `solim-reactivity`: Add requirement for untracked execution isolation in dynamic component factories to prevent unintended signal subscription leakage during child builds.

## Impact

- `solim.signal.ReactiveContext` & `solim.ui.Dynamic`: Added `untracked()` execution helper to prevent signal dependency leaks into dynamic switcher effects.
- `solim.display.NetworkImage` & `solim.display.SolimImage`: Fixed size constraints and prefWidth/prefHeight handling.
- `mindustrytool.features.chat.ChatInputView`: Guaranteed continuous mounting and input focus.
- `mindustrytool.features.chat.ChatMessageListView`: Proper scroll anchoring, initial scroll-to-bottom, and top-left alignment.
- `mindustrytool.features.chat.ChatUserListView`: Avatar sizing consistency.
