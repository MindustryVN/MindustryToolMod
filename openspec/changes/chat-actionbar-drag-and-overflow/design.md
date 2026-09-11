## Context

In the Mindustry Tool mod chat overlay:
1. `ElementModifiers.draggable()` was previously configured with `handle.touchable = (handle instanceof Table) ? Touchable.childrenOnly : Touchable.enabled;`. In Arc's scene graph, `Touchable.childrenOnly` causes `hit()` to return `null` on empty table regions, making background clicks impossible and rendering the action bar undraggable.
2. The action bar header is styled with `Styles.black6`, which blends into the dark chat card background. The user wants a clean, contrasting white header bar.
3. Chat messages with long text (especially sentences, paragraphs, or links) overflow horizontally outside the message card and viewport because Arc's `Table` layout calculates column minimum width from child preferred/min width when `minWidth(0)` is not explicitly declared on cells with wrapping text.

## Goals / Non-Goals

**Goals:**
- Make the entire action bar surface draggable (background, title text, spacers, icons).
- Ensure buttons (Settings, Collapse) remain directly clickable without initiating a window drag.
- Render the action bar header with a solid white background and clear contrasting foreground icons/text.
- Fix text overflow in the chat message list so long messages wrap within the card bounds.

**Non-Goals:**
- Altering the desktop/mobile three-pane layout architecture.
- Changing chat backend APIs or message serialization.

## Decisions

### Decision 1: Full-surface draggable with interactive child exclusion
- **Choice**: In `ElementModifiers.draggable()`, keep `handle.touchable = Touchable.enabled` on all handles (including `Table`).
- In the `InputListener.touchDown()` callback:
  ```java
  Element target = event != null ? event.target : null;
  if (target != null && isInteractiveDescendant(target, handle)) {
      return false; // Let the button handle the touch
  }
  return true; // Claim touch for HUD dragging
  ```
- `isInteractiveDescendant(target, handle)` walks up the hierarchy from `target` to `handle`. If it encounters an `arc.scene.ui.Button` or an element with a `ClickListener`, it returns `true`.
- **Rationale**: This gives full drag affordance across empty background, spacers, and labels, while strictly delegating touches on buttons to the button click handlers.

### Decision 2: White action bar styling with contrasting controls
- **Choice**: Apply `Tex.whiteui` as the action bar background in `ChatOverlayHudView`.
- Update channel title to dark contrast (e.g. `Pal.darkMetal` or `Color.black` with `Pal.accent`).
- Tint action buttons (Settings and Collapse icons) with `Pal.darkMetal` / `Color.darkGray` so they are easily visible and clickable against the white background.
- **Rationale**: `Tex.whiteui` is Arc's standard clean white solid drawable, and dark icons provide accessibility and visual clarity.

### Decision 3: Enforce `minWidth(0)` on wrapping text and message cells
- **Choice**: In `solim.display.Text`, when `wrap(true)` or `growX()` is invoked:
  - If the label is attached to a `Table`, call `cell.minWidth(0f).growX()`.
  - In `ChatMessageListView.java`, render `text(text).color(Color.white).wrap().left().growX()` directly in the content column (or ensure the enclosing cell has `minWidth(0f)`).
- **Rationale**: In Arc's layout algorithm, without `minWidth(0f)`, a table column's minimum width expands to the natural unwrapped text width. `minWidth(0f)` allows the cell to shrink to the container width, forcing `Label.setWrap(true)` to wrap lines at the container boundary.

## Risks / Trade-offs

- **[Risk]**: Touch events near button edges might trigger button or drag ambiguously.
  → **Mitigation**: Arc hit-testing routes precisely to the innermost element. Touching a button always selects the button; touching outside selects the action bar.
- **[Risk]**: Unbroken long words (e.g. 200 consecutive characters without spaces) may still not break mid-word in standard Arc font rendering.
  → **Mitigation**: Arc `Label.setWrap(true)` splits on words. If needed, soft wrapping or font scale can be adjusted, but setting `cell.minWidth(0f)` ensures the container itself never widens past the scroll view.
