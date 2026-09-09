## 1. Network Image Component (Solim)

- [ ] 1.1 Implement `solim.display.NetworkImage` with async texture loading, placeholder, error fallback, and in-memory texture caching
- [ ] 1.2 Expose `Ui.networkImage(url)` and reactive `Ui.networkImage(Readable<String>)` in `Ui.java`
- [ ] 1.3 Add unit tests for `NetworkImage` in `solim`

## 2. Scroll Pagination Hooks (Solim)

- [ ] 2.1 Add `onReachTop(float thresholdPx, Runnable callback)` and `onReachBottom` to `solim.layout.Scroll`
- [ ] 2.2 Add edge-crossing debounce to prevent repeat trigger during continuous scrolling
- [ ] 2.3 Add unit tests for `Scroll` reach-top/reach-bottom triggers in `solim`

## 3. Text Input Enter-Key Submission & Validation (Solim)

- [ ] 3.1 Add `onEnter(Consumer<String> onSubmit)` and `onEnter(Runnable onSubmit)` to `solim.input.SolimTextField`
- [ ] 3.2 Add validator hook and reactive validity state binding to `SolimTextField`
- [ ] 3.3 Add unit tests for `SolimTextField` enter key and validation

## 4. Tabs Component (Solim)

- [ ] 4.1 Implement `solim.layout.Tabs` component with declarative builder syntax and reactive active tab binding
- [ ] 4.2 Expose `Ui.tabs(Signal<Integer> activeTab)` in `Ui.java`
- [ ] 4.3 Add unit tests for `Tabs` component

## 5. Badge Component (Solim)

- [ ] 5.1 Implement `solim.display.Badge` component with pill styling and reactive text/integer binding
- [ ] 5.2 Expose `Ui.badge(...)` in `Ui.java`
- [ ] 5.3 Add unit tests for `Badge` component

## 6. Message List & Rich Content Features (Mod UI)

- [ ] 6.1 Integrate `NetworkImage` for user avatars in `ChatMessageListView` with fallback to `Icon.players`
- [ ] 6.2 Implement sender grouping in `ChatMessageListView` (collapse author row and avatar spacing for consecutive messages from same user)
- [ ] 6.3 Connect `Scroll.onReachTop` in `ChatMessageListView` to trigger `ChatService.fetchMessages` pagination when scrolled near top
- [ ] 6.4 Implement rich message content renderers: in-game schematics (preview, info, export, edit, use), MindustryTool link cards (schematic/map), inline network images, and room connect cards
- [ ] 6.5 Add interactive message click action bar overlay (Copy text, Reply to message, Translate message)

## 7. Chat Input & Attachments (Mod UI)

- [ ] 7.1 Connect `SolimTextField.onEnter` in `ChatInputView` for enter-key message submission
- [ ] 7.2 Implement payload character & size validation per content type in `ChatInputView`
- [ ] 7.3 Implement `AttachContentDialog` using Solim dialog with `.msch`, `.msav` file choosers and clipboard paste
- [ ] 7.4 Add authentication gate in `ChatInputView` displaying a Login button when unauthenticated

## 8. Channels, Users & Mobile Navigation (Mod UI)

- [ ] 8.1 Integrate `Badge` component in `ChatChannelListView` for per-channel unread counts
- [ ] 8.2 Integrate `Badge` component in `ChatOverlayHudView` for collapsed badge unread counter
- [ ] 8.3 Use `NetworkImage` in `ChatUserListView` for user avatars
- [ ] 8.4 Integrate `Tabs` component in `ChatOverlayHudView` for mobile 3-tab responsive view (Channels / Messages / Members)

## 9. Verification & Localization

- [ ] 9.1 Add all new user-facing translation keys and comments to `assets/bundles/bundle.properties`
- [ ] 9.2 Run `./gradlew test` and verify that all unit tests pass
