## Context

The chat feature (`mindustrytool.features.chat`) is built on top of Solim declarative UI components and reactive signals. While the chat HUD overlay and backend WebSocket connection are functional, several UI/UX bugs degrade user experience:
1. `ChatInputView` loses focus every time a key is pressed because the text field is recreated.
2. `ChatMessageListView` and `ChatUserListView` avatars expand or collapse based on downloaded texture size.
3. Messages and elements within `ChatMessageListView` center vertically and horizontally rather than aligning top-left.
4. `ChatMessageListView` opens scrolled to the top and disrupts scroll position when new or older messages load.

## Goals / Non-Goals

**Goals:**
- Eliminate input unfocusing by providing untracked execution in `ReactiveContext` and ensuring dynamic component factories do not capture signal reads during child tree construction.
- Fix avatar dimensions in `NetworkImage` and `SizedImage` so explicit size constraints override intrinsic texture dimensions.
- Align all message feed rows, columns, avatars, content items, and actions top-left.
- Initialize chat scroll position at the bottom and preserve relative scroll position when older messages are prepended. Auto-scroll to bottom on new messages if already at the bottom.

**Non-Goals:**
- Changing backend chat service APIs or protocols.
- Refactoring desktop multi-pane or mobile tab navigation architectures.

## Decisions

### 1. `ReactiveContext.untracked(...)` and `Dynamic` Child Factory Isolation
- **Problem**: When `Dynamic` executes its switching effect, it runs with its `ReactiveObserver` on the `ReactiveContext` stack. When `ChatInputView` builds its children inside `dynamic(store.loggedIn(), ...)`, `SolimTextField` calls `signal.get()` to read initial text. This registers `messageText` as a dependency of `Dynamic`'s effect. Every keystroke updates `messageText`, causing `Dynamic` to unmount and recreate the entire input subtree, losing focus.
- **Decision**:
  - Add `ReactiveContext.untracked(Supplier<T>)` and `ReactiveContext.untracked(Runnable)` which temporarily removes the current observer from the tracking stack.
  - In `Dynamic.java`, invoke `factory.apply(value)` inside `ReactiveContext.untracked(...)`.
  - In `ChatInputView.java`, decouple the persistent message composer row from conditional login prompts or use separate visibility toggling.
- **Alternative Considered**: Manually calling `.peek()` in `SolimTextField`. Rejected because `SolimTextField` needs to read the initial value and user components frequently read signals during `build()`; framework-level untracking in `Dynamic` is the robust architectural solution.

### 2. Enforcing Fixed Dimensions in `SizedImage` and `NetworkImage`
- **Problem**: When a network image finishes downloading, `Image.setDrawable()` updates preferred width and height to the texture's native dimensions. `SizedImage.getPrefWidth()` and `getPrefHeight()` only checked `customPrefWidth` and completely ignored `constraints.prefWidth/Height`. Furthermore, `ElementModifiers.width/height` set `element.setWidth()` but did not update `customPrefWidth/Height` on `SizedImage`.
- **Decision**:
  - In `SizedImage.getPrefWidth()` / `getPrefHeight()`, check `constraints.prefWidth` / `constraints.prefHeight` first, then `customPrefWidth/Height`, then fallback to `super.getPrefWidth/Height()`.
  - In `ElementModifiers.width` and `height`, if `element instanceof SizedImage`, update its `customPrefWidth` / `customPrefHeight`.
  - In `ChatMessageListView` and `ChatUserListView`, ensure avatars use explicit `.size(unit(8), unit(8))` and `.size(unit(6), unit(6))`.

### 3. Top-Left Message Feed Alignment
- **Problem**: Table cells in Arc UI default to `Align.center`. Messages with short lines, embedded cards, and avatars get vertically and horizontally centered in their respective rows and columns.
- **Decision**:
  - Apply `.top().left()` to the message list container, message card, and message item row.
  - Set `.top().left()` on the avatar component and the content column.
  - Ensure `solim.layout.Row` and `solim.layout.Column` support `.top()` and `.left()` modifiers cleanly and apply them to child cells where appropriate.

### 4. Scroll Position Initialization and Anchoring
- **Problem**: `ChatMessageListView` mounts with `scrollY = 0` (top of list), which immediately triggers `onReachTop` and loads older messages. When older messages are prepended to `activeMessages`, the content height expands upwards, causing the viewport to jump to the newly prepended items.
- **Decision**:
  - On mount or active channel change in `ChatMessageListView`, trigger `scrollToBottom()`.
  - Track the previous scroll pane content height and message count before message updates. When older messages are prepended, adjust `scrollY` by the difference in content height (`newHeight - oldHeight`) using `Core.app.post()` so the user's viewport stays on the same messages.
  - When new messages arrive, if the user was within a 100px threshold of the bottom, automatically scroll to the bottom.

## Risks / Trade-offs

- **[Risk]** Layout height calculation timing before Arc packs the table.
  - *Mitigation*: Run scroll adjustment in `Core.app.post()` after Arc validates hierarchy and recomputes pref heights.
- **[Risk]** Potential recursion in `ReactiveContext.untracked`.
  - *Mitigation*: Use a clean `try-finally` block that restores the exact observer popped from the stack.
