## 1. Solim Reactivity & Dynamic Isolation

- [ ] 1.1 Implement `ReactiveContext.untracked(Supplier<T>)` and `ReactiveContext.untracked(Runnable)` in `solim/src/solim/signal/ReactiveContext.java`
- [ ] 1.2 Update `Dynamic.java` to invoke child component factory inside `ReactiveContext.untracked(...)` so child signal evaluations do not register as dependencies of the dynamic switcher
- [ ] 1.3 Add unit tests in `solim` verifying that signals read during dynamic component construction do not trigger the dynamic switcher when updated

## 2. Image Sizing & Constraint Enforcement

- [ ] 2.1 Update `SolimImage.SizedImage`'s `getPrefWidth()` and `getPrefHeight()` to prioritize `constraints.prefWidth` and `constraints.prefHeight` over intrinsic drawable size
- [ ] 2.2 Update `ElementModifiers.width` and `ElementModifiers.height` to set `customPrefWidth` and `customPrefHeight` when target element is a `SizedImage`
- [ ] 2.3 Add unit tests in `solim` verifying that setting size on `NetworkImage` / `SizedImage` preserves dimensions after a texture/drawable is loaded

## 3. Chat Input Field Stability

- [ ] 3.1 Refactor `ChatInputView.java` to ensure `SolimTextField` remains continuously mounted without being recreated when typing
- [ ] 3.2 Ensure reply preview banner updates cleanly without unmounting or resetting the text field

## 4. Message List Alignment & Avatar Sizing

- [ ] 4.1 In `ChatMessageListView.java`, apply `.top().left()` alignment to message cards, message rows, avatars, and content columns
- [ ] 4.2 In `ChatMessageListView.java`, lock user avatars to fixed size `unit(8), unit(8)` aligned at the top-left with `.top().left()`
- [ ] 4.3 In `ChatMessageListView.java`, align embedded previews (replies, links, maps/schematics cards, action buttons) to the top-left
- [ ] 4.4 In `ChatUserListView.java`, ensure user avatars are locked to fixed dimensions `unit(6), unit(6)` with proper top-left alignment

## 5. Scroll Position Anchoring & Initialization

- [ ] 5.1 In `ChatMessageListView.java`, automatically scroll to the bottom when the message list is first mounted or when switching active channels
- [ ] 5.2 In `ChatMessageListView.java`, implement scroll position preservation when older messages are prepended to the top of the feed by adjusting `scrollY` with the content height delta
- [ ] 5.3 In `ChatMessageListView.java`, automatically scroll to the bottom when a new incoming message is appended if the user was already near the bottom (within threshold)

## 6. Verification & End-to-End Validation

- [ ] 6.1 Run `./gradlew test` to ensure all Solim unit tests and mod tests pass
- [ ] 6.2 Build and launch Mindustry with `./run.bat` and verify chat UI live via Solim MCP tools: test typing in chat input without losing focus, verify avatar sizes, verify top-left alignment, and check scroll behaviors
