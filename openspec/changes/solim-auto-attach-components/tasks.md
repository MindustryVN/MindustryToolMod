## 1. solim-core: BaseComponent auto-attach

- [x] 1.1 In `BaseComponent` constructor, after `ComponentContext.registerChild(this)`, check if `ParentStack.current() != null` and call `ParentStack.registerPendingComponent(this, ParentStack.current())` to schedule element attachment to the active parent
- [x] 1.2 Verify that the double-attach guard in `ParentStack.doAttach` (`child.parent != parent && !parent.getChildren().contains(child, true)`) correctly prevents duplicates when `component()` is also called explicitly on the same instance
- [x] 1.3 Add a unit test: constructing a `BaseComponent` subclass inside a `children()` scope auto-attaches its element to the parent table
- [x] 1.4 Add a unit test: constructing a `BaseComponent` outside any `children()` scope does NOT attach anything
- [x] 1.5 Add a unit test: calling `component()` explicitly on an auto-registered component does not produce duplicate children

## 2. solim facade: UI.arc(Element el)

- [x] 2.1 Add `public static Element arc(Element el)` to `UI.java` that calls `ParentStack.attachToParent(el)` and returns `el`
- [x] 2.2 Add a unit test: `arc(new Label("hi"))` inside a `children()` block attaches the label to the parent

## 3. mod: Remove redundant component() calls

- [x] 3.1 In `ChatOverlayHudView.java`, remove all `component(new Chat*View(...))` calls inside `children()` blocks — rely on auto-attach
- [x] 3.2 In `ChatMessageListView.java`, remove `component(new ChatAvatar(...))` call inside `children()` — rely on auto-attach
- [x] 3.3 In `ChatMessageListView.java`, replace `component(() -> new SchematicImage(schematic).setScaling(Scaling.fit))` with `arc(new SchematicImage(schematic).setScaling(Scaling.fit))` using the new facade
- [x] 3.4 In `ChatUserListView.java`, remove `component(new ChatAvatar(...))` call inside `children()` — rely on auto-attach
- [x] 3.5 In `ChatAvatar.java`, remove `component(stack)` call inside `build()` — rely on auto-attach
- [x] 3.6 In `ChatChannelListView.java`, remove `component(stack)` call inside `build()` — rely on auto-attach
- [x] 3.7 In `TeamResourceHudView.java`, replace `component(() -> satisfactionBar)` and `component(() -> storedBar)` with `arc(satisfactionBar)` / `arc(storedBar)`

## 4. Verification

- [x] 4.1 Build mod: `gradlew :mod:compileJava` — confirm no compile errors
- [x] 4.2 Build solim-core: `gradlew :solim-core:test` — confirm all tests pass
- [ ] 4.3 Run Mindustry (`run.bat`), open chat overlay — verify all panels render correctly (channel list, message list, user list, input)
- [ ] 4.4 Verify schematic card buttons in chat messages still render the `SchematicImage`
- [ ] 4.5 Stop Mindustry using MCP `stop` tool after verification
