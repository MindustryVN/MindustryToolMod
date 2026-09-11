## 1. Full-surface action bar dragging with button passthrough

- [x] 1.1 In `ElementModifiers.draggable()`, keep `handle.touchable = Touchable.enabled` on all handles, and in `InputListener.touchDown()`, check if `event.target` is an interactive descendant control (`Button` or element with `ClickListener`); return `false` if interactive, otherwise `true`
- [x] 1.2 Update unit tests in `HudTest.java` to verify that clicking a button inside a Table handle fires the button without dragging, while clicking on empty background, label, or spacer initiates dragging
- [x] 1.3 Run `:solim-core:test` to confirm all drag and HUD tests pass

## 2. White action bar styling in ChatOverlayHudView

- [x] 2.1 In `ChatOverlayHudView.java`, change the action bar row background from `Styles.black6` to `Tex.whiteui`
- [x] 2.2 In `ChatOverlayHudView.java`, update channel title and action button icons (Settings and Collapse) to use dark contrasting colors (`Pal.darkMetal` / `Color.darkGray`) for clear visibility on the white background

## 3. Fix text message overflow in ChatMessageListView and Text

- [x] 3.1 In `solim.display.Text`, update `growX()` and `wrap()` so that when applied to an element inside a `Table`, `cell.minWidth(0f)` is set along with `cell.growX()`
- [x] 3.2 In `ChatMessageListView.java`, ensure message body containers enforce `cell.minWidth(0f)` and avoid unconstrained nested rows that cause horizontal expansion beyond the card width
- [x] 3.3 Add or update tests to verify that `Text.wrap()` properly sets `cell.minWidth(0f)` when inside a Table

## 4. Verification and In-Game Testing

- [x] 4.1 Run `./gradlew test` to ensure all tests across all modules pass cleanly
- [x] 4.2 Run `run.bat` to launch Mindustry with the mod and verify in-game: (a) action bar is white with legible title/icons, (b) whole action bar is draggable from background/title/spacer, (c) settings and collapse buttons are clickable, (d) long text messages wrap without horizontal overflow
- [x] 4.3 Stop Mindustry using the MCP `stop` tool
