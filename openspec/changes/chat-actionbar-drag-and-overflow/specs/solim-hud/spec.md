## MODIFIED Requirements

### Requirement: Draggable modifier for Hud repositioning
The Solim framework SHALL provide a `.draggable()` modifier that attaches touch drag handling to an element. The handle element SHALL have `touchable = enabled` so its entire surface area can capture drag gestures. When a touch occurs on an interactive descendant button or clickable control within the handle, the drag listener SHALL NOT consume the event and SHALL return `false` on `touchDown` so the child control receives the click. When a touch occurs on the container background or non-button elements (such as text, images, or spacers), the drag listener SHALL return `true`, translate the parent `Hud` container by the drag delta, clamp coordinates to the screen, and optionally update reactive `Signal<Float>` x and y positions.

#### Scenario: Dragging a handle moves the Hud
- **WHEN** a user touches down on a non-button area of a handle configured with `.draggable()` and drags
- **THEN** the parent `Hud` element moves by the touch displacement vector and updates the associated coordinate signals

#### Scenario: Clamping during drag movement
- **WHEN** a drag movement attempts to push the `Hud` outside screen bounds
- **THEN** the movement is clamped at the screen border

#### Scenario: Interactive descendant button receives click without drag
- **WHEN** a user touches down and clicks a `Button` inside a handle container configured with `.draggable()`
- **THEN** the button action fires and no `Hud` dragging is initiated

#### Scenario: Dragging from label or spacer in handle moves Hud
- **WHEN** a user touches down and drags on a `Label`, `Image`, or empty background area within a handle container
- **THEN** the parent `Hud` moves with the drag gesture
