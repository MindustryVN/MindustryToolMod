## 1. Network Image Component (Solim)

- [x] 1.1 Implement `solim.display.NetworkImage` with async texture loading, placeholder, error fallback, and in-memory texture caching
- [x] 1.2 Expose `Ui.networkImage(url)` and reactive `Ui.networkImage(Readable<String>)` in `Ui.java`
- [x] 1.3 Add unit tests for `NetworkImage` in `solim`

## 2. Scroll Pagination Hooks (Solim)

- [x] 2.1 Add `onReachTop(float thresholdPx, Runnable callback)` and `onReachBottom` to `solim.layout.Scroll`
- [x] 2.2 Add edge-crossing debounce to prevent repeat trigger during continuous scrolling
- [x] 2.3 Add unit tests for `Scroll` reach-top/reach-bottom triggers in `solim`

## 3. Text Input Enter-Key Submission & Validation (Solim)

- [x] 3.1 Add `onEnter(Consumer<String> onSubmit)` and `onEnter(Runnable onSubmit)` to `solim.input.SolimTextField`
- [x] 3.2 Add validator hook and reactive validity state binding to `SolimTextField`
- [x] 3.3 Add unit tests for `SolimTextField` enter key and validation

## 4. Tabs Component (Solim)

- [x] 4.1 Implement `solim.layout.Tabs` component with declarative builder syntax and reactive active tab binding
- [x] 4.2 Expose `Ui.tabs(Signal<Integer> activeTab)` in `Ui.java`
- [x] 4.3 Add unit tests for `Tabs` component

## 5. Badge Component (Solim)

- [x] 5.1 Implement `solim.display.Badge` component with pill styling and reactive text/integer binding
- [x] 5.2 Expose `Ui.badge(...)` in `Ui.java`
- [x] 5.3 Add unit tests for `Badge` component

## 6. Message List & Rich Content Features (Mod UI)

- [x] 6.1 Integrate `NetworkImage` for user avatars in `ChatMessageListView` with fallback to `Icon.players`
- [x] 6.2 Implement sender grouping in `ChatMessageListView` (collapse author row and avatar spacing for consecutive messages from same user)
- [x] 6.3 Connect `Scroll.onReachTop` in `ChatMessageListView` to trigger `ChatService.fetchMessages` pagination when scrolled near top
- [x] 6.4 Implement rich message content renderers: in-game schematics (preview, info, export, edit, use), MindustryTool link cards (schematic/map), inline network images, and room connect cards
- [x] 6.5 Add interactive message click action bar overlay (Copy text, Reply to message, Translate message)

## 7. Chat Input & Attachments (Mod UI)

- [x] 7.1 Connect `SolimTextField.onEnter` in `ChatInputView` for enter-key message submission
- [x] 7.2 Implement payload character & size validation per content type in `ChatInputView`
- [x] 7.3 Implement `AttachContentDialog` using Solim dialog with `.msch`, `.msav` file choosers and clipboard paste
- [x] 7.4 Add authentication gate in `ChatInputView` displaying a Login button when unauthenticated

## 8. Channels, Users & Mobile Navigation (Mod UI)

- [x] 8.1 Integrate `Badge` component in `ChatChannelListView` for per-channel unread counts
- [x] 8.2 Integrate `Badge` component in `ChatOverlayHudView` for collapsed badge unread counter
- [x] 8.3 Use `NetworkImage` in `ChatUserListView` for user avatars
- [x] 8.4 Integrate `Tabs` component in `ChatOverlayHudView` for mobile 3-tab responsive view (Channels / Messages / Members)

## 9. Verification & Localization

- [x] 9.1 Add all new user-facing translation keys and comments to `assets/bundles/bundle.properties`
- [x] 9.2 Run `./gradlew test` and verify that all unit tests pass
