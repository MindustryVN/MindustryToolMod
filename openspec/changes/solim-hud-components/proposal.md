## Why

`QuickAccessHudView` is currently implemented using raw Arc `Table`, imperative drag listeners, manual long-press timing in `update()`, and manual `rebuild()` cycles on screen resize and state changes. Rewriting it completely in Solim requires declarative primitives for floating draggable HUD containers, gesture interactions (long click), reactive opacity, and button tooltips.

## What Changes

- Introduce `Hud` (`hud()`) component in Solim: a floating overlay container positioned on the screen/HUD with automatic screen bounds clamping and resize listening.
- Introduce draggable interaction support (`draggable()`) allowing any Solim element or handle to drag its parent container and synchronize coordinates with `Signal<Float>`.
- Add `onLongClick(Runnable)` and `onLongClick(long durationMs, Runnable)` gesture support to `Button`.
- Add `tooltip(String)` and `tooltip(Readable<String>)` modifiers to `Button` and other interactive components.
- Add reactive `opacity(float)` / `opacity(Readable<Float>)` modifier to `ElementModifiers` and Solim layout containers.
- Rewrite `QuickAccessHudView` using pure Solim components (`BaseComponent`, `hud()`, `row()`, `grid()`, `button()`, reactive signals).

## Capabilities

### New Capabilities
- `solim-hud`: Floating HUD overlay container component supporting absolute screen coordinates, viewport boundary clamping on screen resize, drag handle behaviors, and touchable control.

### Modified Capabilities
- `solim-widgets`: Extend `Button` with `onLongClick` gestures (coexisting with `onClick` without false triggers) and declarative `tooltip` binding.
- `solim-shared-modifiers`: Add reactive opacity/alpha modifiers (`opacity(float)`, `opacity(Readable<Float>)`) across Solim elements.

## Impact

- `solim`: New `solim.overlay.Hud` component, `Button` gesture/tooltip extensions, and `ElementModifiers` opacity helpers.
- `mod`: `QuickAccessHudView` can be completely rewritten from an Arc `Table` subclass into a pure Solim `BaseComponent`.
- No breaking changes to existing Solim APIs.
