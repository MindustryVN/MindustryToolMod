## Why

The legacy chat implementation (`old.mindustrytool.features.chat.global.ChatFeature`) includes a rich suite of game mod features: real-time streaming, collapsed/expanded HUD overlay, channel management, infinite scroll message history, role-colored authors, avatars, attachment dialogs (schematic, map, clipboard), inline schematic/map cards with in-game actions, rich link renderers, rate limiting, and mobile 3-tab responsive navigation.

While the modern chat feature was recently refactored into Solim declarative components, several core UI capabilities from the old chat system are either missing from Solim or were implemented as ad-hoc Arc workarounds. Providing standardized, declarative Solim components for these capabilities will allow the chat feature (and any other mod features) to achieve 100% feature parity with clean, reactive Solim code.

## What Changes

- **Comprehensive Analysis of Old `ChatFeature`**: Enumerate all features, UI layouts, and user interactions from `old/mindustrytool/features/chat/global/ChatFeature._java` and its dependencies.
- **`NetworkImage` Component (`solim.display.NetworkImage`, `Ui.networkImage`)**: New declarative Solim component for asynchronously loading images via HTTP/HTTPS with in-memory caching, reactive URL binding, placeholder drawable, and error fallback.
- **Scroll Pagination & Scroll State Hooks (`solim.layout.Scroll`)**: Add declarative boundary event hooks (`onReachTop`, `onReachBottom`, `onScroll`) to `Scroll` to power infinite-scroll message history pagination.
- **Input Action Extensions (`solim.input.SolimTextField`)**: Add `onEnter` submission listener and validator bindings to `SolimTextField` to support chat message sending and validation without raw Arc event listeners.
- **`Tabs` Component (`solim.layout.Tabs`, `Ui.tabs`)**: Declarative tab container for responsive multi-tab navigation (mobile chat views and feature setting dialogs).
- **`Badge` Component (`solim.display.Badge`, `Ui.badge`)**: Declarative pill/badge component for displaying unread message counters, status indicators, and role badges.

## Capabilities

### New Capabilities
- `solim-network-image`: Asynchronous image loading component with in-memory texture cache, reactive URL binding, placeholder while loading, error fallback, and sizing modifiers.
- `solim-scroll-pagination`: Scroll boundary triggers (`onReachTop`, `onReachBottom`) and scroll position tracking on `Scroll` for infinite list pagination.
- `solim-input-extensions`: Enter key trigger (`onEnter`) and validation feedback on `SolimTextField`.
- `solim-tabs`: Declarative tab bar and content switcher component supporting reactive active tab signals.
- `solim-badge`: Declarative counter and tag badge component with customizable styling and reactive label bindings.

### Modified Capabilities
<!-- Existing capabilities whose REQUIREMENTS are changing. Leave empty if no requirement changes. -->

## Impact

- `solim`: Adds `NetworkImage`, `Tabs`, `Badge`, and updates `Scroll` and `SolimTextField`.
- `mod`: Enables `ChatOverlayHudView`, `ChatMessageListView`, `ChatInputView`, and `ChatChannelListView` to fully replicate all legacy `ChatFeature` capabilities (async user avatars, infinite pagination, file attachments, enter-key submit, unread badges, and mobile tabs) using 100% pure Solim UI.
- Dependencies: Uses existing `mindustrytool.services.Request` / Arc HTTP client abstractions for image network requests; compatible with Java 8 runtime.
