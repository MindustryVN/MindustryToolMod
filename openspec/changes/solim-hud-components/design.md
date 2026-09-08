## Context

`QuickAccessHudView` is an in-game HUD overlay in Mindustry that displays floating quick-access feature buttons. Currently, it is implemented as a raw Arc `Table` subclass that manually registers Arc `InputListener` instances for drag moving, uses `update()` timers for long-press detection, and executes imperative `rebuild()` cycles on `ResizeEvent` and configuration changes.

To bring `QuickAccessHudView` into idiomatic Solim architecture, Solim needs first-class primitives for floating draggable HUD containers, long-press gesture support on buttons, declarative tooltips, and reactive opacity.

## Goals / Non-Goals

**Goals:**
- Provide a declarative `Hud` component (`hud()`) in `solim.overlay` for floating, positioned screen overlays.
- Provide a `draggable()` modifier / handle capability that moves the HUD overlay and synchronizes position with `Signal<Float>` x and y.
- Automatically clamp HUD overlays within screen bounds on drag and `ResizeEvent`.
- Provide `onLongClick(Runnable)` and `onLongClick(long durationMs, Runnable)` on `Button` that cleanly disambiguates single clicks from long presses.
- Provide `tooltip(String)` and `tooltip(Readable<String>)` on `Button`.
- Provide `opacity(float)` and `opacity(Readable<Float>)` modifiers on Solim elements and layout components.
- Rewrite `QuickAccessHudView` as a declarative Solim `BaseComponent`.

**Non-Goals:**
- Creating a full desktop-style docking or multi-window manager.
- Modifying Mindustry's core HUD rendering pipeline or scene graph.

## Decisions

### Decision 1: `Hud` Component Architecture
- Place `Hud` in `solim.overlay.Hud`.
- Root element is a `SizedTable` defaulting to `touchable = childrenOnly`, with content container `touchable = enabled`.
- Supports reactive `.x(Readable<Float>)`, `.y(Readable<Float>)` or static coordinates.
- Listens to screen resize events (`ResizeEvent`) to ensure HUD remains clamped inside screen bounds (`keepInScreen()`).
- Alternatives considered: Subclassing `SolimDialog`. Rejected because dialogs are centered modals that dim/block background input, whereas HUDs are non-modal, transparent, and floating.

### Decision 2: Draggable Behavior
- Provide `.draggable()` on Solim components / elements.
- When an element (e.g. move icon button) has `.draggable(hud, xSignal, ySignal)`:
  - An `InputListener` tracks `touchDown` and `touchDragged`.
  - Shifts HUD position by `(deltaX, deltaY)` clamped to `[0, screenWidth - hudWidth]` and `[0, screenHeight - hudHeight]`.
  - Updates `xSignal` and `ySignal` reactively.
- Alternatives considered: Making the entire HUD table draggable. Rejected because clicking child buttons inside the HUD would conflict with drag tracking; having a dedicated drag handle or explicit draggable modifier is much more reliable and standard for game HUDs.

### Decision 3: `Button` Long-Click Gesture
- Add `onLongClick(Runnable action)` and `onLongClick(long durationMs, Runnable action)` to `Button`.
- Uses a threshold (default 300ms). When pressed for >= 300ms, triggers `onLongClick` and marks a `longPressed` flag.
- Normal `onClick` checks `!longPressed` on release, preventing accidental trigger of regular click when a long-press finishes.
- Alternatives considered: External gesture recognizer. Rejected as over-engineered; integrating cleanly into `Button` matches how `onClick` works and handles state disposal cleanly.

### Decision 4: Declarative Tooltip on `Button`
- Add `tooltip(String)` and `tooltip(Readable<String>)` to `Button`.
- Wraps Arc's `Tooltip` and automatically updates text when the reactive signal emits.

### Decision 5: Reactive Opacity / Alpha
- Add `opacity(float)` and `opacity(Readable<Float>)` (and alias `alpha`) to `ElementModifiers` and `LayoutModifiers`.
- Updates `element.setColor(r, g, b, alpha)` or table color, re-evaluated when the signal changes.

## Risks / Trade-offs

- [Touch drag event coordinate scaling on high-DPI / mobile] → Use delta movement `(x - lastX, y - lastY)` in screen space and clamp to `Core.graphics.getWidth()` / `getHeight()`.
- [Long click triggering regular click upon release] → Track `longPressed` boolean; if long press fired during hold, suppress the `onClick` release callback.
- [Screen resize pushing HUD offscreen] → Auto-register `ResizeEvent` listener in `Hud` that calls `keepInScreen()`.

## Migration Plan

1. Implement `Hud` and `draggable` in `solim`.
2. Implement `onLongClick` and `tooltip` on `Button`.
3. Implement `opacity` modifiers.
4. Add comprehensive unit tests in `solim`.
5. Rewrite `QuickAccessHudView.java` in `mod/src/mindustrytool/features/quickaccess/QuickAccessHudView.java` to extend `BaseComponent`.
