## 1. Solim Modifiers & Button Extensions

- [x] 1.1 Add `opacity(float)` and `opacity(Readable<Float>)` / `alpha` to `ElementModifiers` and `LayoutModifiers`
- [x] 1.2 Add `onLongClick(Runnable)` and `onLongClick(long, Runnable)` to `Button` with click suppression
- [x] 1.3 Add `tooltip(String)` and `tooltip(Readable<String>)` to `Button`
- [x] 1.4 Add unit tests for Button long click, tooltip, and opacity modifiers in `solim`

## 2. Solim Hud Component & Draggable Support

- [x] 2.1 Implement `Hud` container in `solim.overlay.Hud` with `touchable = childrenOnly`, coordinates, and `keepInScreen()`
- [x] 2.2 Add `Ui.hud(...)` facades in `solim.ui.Ui`
- [x] 2.3 Implement `draggable()` modifier in `solim` supporting drag delta movement, screen boundary clamping, and signal syncing
- [x] 2.4 Add unit tests for `Hud` viewport clamping and draggable behavior

## 3. QuickAccessHudView Solim Rewrite

- [x] 3.1 Refactor `QuickAccessHudView` to extend `BaseComponent` returning declarative `hud()`
- [x] 3.2 Build HUD layout with draggable move handle, vertical divider, and dynamic feature button grid
- [x] 3.3 Connect `onLongClick` to feature settings dialog and `onClick` to feature enable/disable
- [x] 3.4 Wire reactive bindings for scale, opacity, and coordinates
- [x] 3.5 Verify full build and tests via `./gradlew test` and `./gradlew :mod:jar`
