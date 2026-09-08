## 1. Solim Modifiers & Button Extensions

- [ ] 1.1 Add `opacity(float)` and `opacity(Readable<Float>)` / `alpha` to `ElementModifiers` and `LayoutModifiers`
- [ ] 1.2 Add `onLongClick(Runnable)` and `onLongClick(long, Runnable)` to `Button` with click suppression
- [ ] 1.3 Add `tooltip(String)` and `tooltip(Readable<String>)` to `Button`
- [ ] 1.4 Add unit tests for Button long click, tooltip, and opacity modifiers in `solim`

## 2. Solim Hud Component & Draggable Support

- [ ] 2.1 Implement `Hud` container in `solim.overlay.Hud` with `touchable = childrenOnly`, coordinates, and `keepInScreen()`
- [ ] 2.2 Add `Ui.hud(...)` facades in `solim.ui.Ui`
- [ ] 2.3 Implement `draggable()` modifier in `solim` supporting drag delta movement, screen boundary clamping, and signal syncing
- [ ] 2.4 Add unit tests for `Hud` viewport clamping and draggable behavior

## 3. QuickAccessHudView Solim Rewrite

- [ ] 3.1 Refactor `QuickAccessHudView` to extend `BaseComponent` returning declarative `hud()`
- [ ] 3.2 Build HUD layout with draggable move handle, vertical divider, and dynamic feature button grid
- [ ] 3.3 Connect `onLongClick` to feature settings dialog and `onClick` to feature enable/disable
- [ ] 3.4 Wire reactive bindings for scale, opacity, and coordinates
- [ ] 3.5 Verify full build and tests via `./gradlew test` and `./gradlew :mod:jar`
