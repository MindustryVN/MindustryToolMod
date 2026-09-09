## Context

The legacy chat system (`old.mindustrytool.features.chat.global.ChatFeature`) relied on manual Arc widgets (`Table`, `Label`, `TextField`, `ScrollPane`, `Stack`, `BaseDialog`) and custom procedural methods to manage UI, async image fetching, scroll position tracking, and file dialogs.

The new Solim architecture requires all UI to be declarative, reactive, and automatically owned. While basic layout and component primitives exist, several patterns required to achieve 100% feature parity with the old chat system are missing from Solim:
1. Asynchronous remote image rendering (`NetworkImage`)
2. Scroll boundary detection (`Scroll` pagination / `onReachTop`)
3. Text input enter-key submission (`SolimTextField.onEnter`)
4. Declarative tab navigation (`Tabs`)
5. Compact badge/pill display (`Badge`)

Once these components are in place, the chat mod UI (`ChatMessageListView`, `ChatInputView`, `ChatChannelListView`, `ChatUserListView`, `ChatOverlayHudView`) must be updated to integrate them and fully implement all features from legacy `ChatFeature`.

## Goals / Non-Goals

**Goals:**
- Provide declarative Solim components and extensions for all missing capabilities:
  - `NetworkImage` for user avatars, map cards, schematic previews, and image attachments.
  - `Scroll` boundary listeners (`onReachTop`, `onReachBottom`, `onScroll`) to support infinite scroll history pagination.
  - `SolimTextField.onEnter` to allow typing and pressing Enter to send chat messages.
  - `Tabs` component for mobile 3-tab layout (Channels / Messages / Members) and dialog tab bars.
  - `Badge` component for unread counters, role tags, and status dots.
- Modernize and complete all chat features in `mod/src/mindustrytool/features/chat/`:
  - Avatars and user identity with highest role colors.
  - Sender grouping (collapse consecutive message headers from same user).
  - Infinite scroll history pagination on reach-top.
  - Rich message renderers: in-game schematics (preview, info, export, edit, use), MindustryTool link cards (maps, schematics), inline images, room connect cards.
  - Message action bar overlay (Copy, Reply, Translate).
  - File attachments dialog (`.msch`, `.msav`, clipboard) and content type detection.
  - Rate-limit handling and auth state gate (login prompt).
  - Responsive mobile tabs and unread badges.
- Ensure 100% Java 8 runtime compatibility.
- Ensure automatic lifecycle ownership and signal reactivity for all components.

**Non-Goals:**
- Modifying the legacy `old/` directory.

## Decisions

### 1. `NetworkImage` Architecture
- **Choice**: Implement `solim.display.NetworkImage` extending `BaseComponent`, with an in-memory texture cache (`Map<String, TextureRegion>`).
- **Rationale**: User avatars and message previews repeat frequently. Caching prevents redundant HTTP downloads and scene flickering.
- **Features**:
  - Accepts `String` URL or reactive `Readable<String>`.
  - Configurable placeholder drawable (`fallback` / `placeholder`).
  - Size constraints integration (`size`, `width`, `height`, `scaling`).
  - Uses `mindustrytool.services.Request` / Arc HTTP client.

### 2. Infinite Scroll Hooks in `Scroll`
- **Choice**: Add `onReachTop(float thresholdPx, Runnable onReachTop)` and `onReachBottom(float thresholdPx, Runnable onReachBottom)` directly to `solim.layout.Scroll`.
- **Rationale**: In Mindustry/Arc, `ScrollPane.update(() -> ...)` checks `getScrollY()`. Exposing declarative callbacks on `Scroll` allows `ChatMessageListView` to trigger `ChatService.fetchMessages(channelId, oldestMessageId)` without raw Arc event management.
- **Edge-Crossing Debounce**: Only trigger once when passing into the threshold area until the user scrolls away, preventing duplicate API fetches.

### 3. Enter-Key Submission on `SolimTextField`
- **Choice**: Add `onEnter(Consumer<String> onSubmit)` and `onEnter(Runnable onSubmit)` on `SolimTextField`.
- **Rationale**: Standard chat UX requires pressing Enter on desktop to send. Exposing `onEnter` directly keeps component definitions declarative and avoids manual `InputListener` / `keyDown` registration in application code.

### 4. `Tabs` Component
- **Choice**: Add `solim.layout.Tabs` component with declarative builder syntax:
  ```java
  tabs(activeTabSignal)
      .tab("Channels", () -> channelView())
      .tab("Messages", () -> messageView())
      .tab("Members", () -> memberView());
  ```
- **Rationale**: Simplifies mobile chat layout and multi-page settings dialogs while keeping tab bar styling cohesive.

### 5. `Badge` Component
- **Choice**: Add `solim.display.Badge` (`Ui.badge(label)`) extending `BaseComponent` with compact rounded styling and optional reactive text/number.

### 6. Rich Message Content & Sender Grouping
- **Choice**: In `ChatMessageListView`, structure messages into a list that tracks consecutive senders. For each message:
  - If previous message is from same user, hide author row and top avatar padding.
  - Parse content for Schematics (`Vars.schematicBaseStart`), MindustryTool links, and images, rendering interactive Solim cards.
  - Click on card opens action bar overlay with Copy, Reply, and Translate.

### 7. Attachments Dialog & Content Detection
- **Choice**: Create `AttachContentDialog` using Solim `dialog` with buttons for `.msch`, `.msav`, and clipboard, passing encoded base64 directly to `ChatService.sendMessage(..., ContentType)`.

## Risks / Trade-offs

- **[Risk] Texture memory accumulation in `NetworkImage`** → **Mitigation**: Use a bounded cache (e.g. max 100 textures) or weak references so unused downloaded textures can be collected.
- **[Risk] Multiple pagination fetches when scrolling fast near top** → **Mitigation**: Guard pagination fetch with an `isLoading` check and threshold edge-crossing latch.
