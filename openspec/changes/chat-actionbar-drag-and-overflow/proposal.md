## Why

In the in-game chat overlay, three user experience issues currently degrade usability:
1. The chat window cannot be dragged across the full surface of the action bar header — clicking or dragging the header background or non-button areas either fails to initiate drag or interferes with child buttons.
2. The action bar header uses a dark background (`Styles.black6`) instead of a distinct white bar requested for visual styling and clarity.
3. Long chat text messages overflow horizontally beyond the message card and out of the chat window bounds because cells containing wrapping text don't constrain minimum width to zero in the Arc layout system.

## What Changes

- **Full Action Bar Drag with Button Passthrough**: Update `ElementModifiers.draggable()` so that the entire handle container is touchable and draggable from any empty background or non-button element (title, spacers), while touch events targeted at child buttons (Settings, Collapse) are handled by the buttons without initiating drag.
- **White Action Bar Styling**: Change the action bar background in `ChatOverlayHudView` to white (`Tex.whiteui`) and style title text and action buttons with high contrast against the white background.
- **Message Text Overflow Fix**: Update `Text.wrap()` / `Text.growX()` and message layout in `ChatMessageListView` to ensure `cell.minWidth(0f)` is enforced on wrapping text cells, preventing text from blowing out the horizontal dimensions of cards and scroll panes.

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `solim-hud`: Container drag handles support full-surface dragging while excluding interactive descendant buttons from drag capture.
- `chat-overlay`: Expanded chat window features a white action bar with full-surface drag handling and button interaction, and the message list wraps long messages without overflowing.

## Impact

- `solim-core`: `ElementModifiers.draggable()` touch-target checking; `Text.java` cell minWidth zero on wrap.
- `mod`: `ChatOverlayHudView.java` action bar background and button styling; `ChatMessageListView.java` message card text layout.
