# solim-hud Specification

## Purpose
TBD - created by archiving change solim-hud-components. Update Purpose after archive.
## Requirements
### Requirement: Floating Hud container
The Solim framework SHALL provide a `Hud` container (`hud()`, `hud(Runnable children)`) in `solim.overlay` representing a floating, non-modal screen overlay. The `Hud` root element SHALL default to `touchable = childrenOnly` so unconsumed touches pass through to underlying game elements.

#### Scenario: Creating a basic Hud container
- **WHEN** `hud(() -> { button("Click", () -> {}); })` is constructed
- **THEN** a `Hud` component is created with its content wrapped and added to the overlay root

#### Scenario: Touch pass-through on Hud container
- **WHEN** touches occur outside the HUD children but within the HUD container bounds
- **THEN** touches are not consumed by the HUD container and pass through to the scene below

### Requirement: Hud viewport boundary clamping and resize adaptation
The `Hud` container SHALL support a `keepInScreen()` method and automatically register an Arc `ResizeEvent` listener on construction to clamp its $(x, y)$ coordinates within $[0, \text{screenWidth} - \text{hudWidth}]$ and $[0, \text{screenHeight} - \text{hudHeight}]$. When scale is applied to the Hud container, `root.pack()` SHALL be called after the scale change so the root cell tracks the container's scaled visual bounds and hit-testing remains accurate.

#### Scenario: Clamping Hud coordinates when positioned outside screen
- **WHEN** a `Hud` has width 200, height 100, and $(x, y)$ is set to $(1200, 900)$ on a $1024 \times 768$ screen
- **THEN** `keepInScreen()` clamps $(x, y)$ to $(824, 668)$

#### Scenario: Screen resize event keeps Hud within viewport
- **WHEN** a `ResizeEvent` occurs in the game runtime
- **THEN** `Hud` updates its layout and clamps coordinates within the new viewport dimensions

#### Scenario: Hit region matches visual bounds after scale change
- **WHEN** `hud.scale(0.8f)` is applied
- **THEN** hovering and clicking within the visually rendered area of the Hud triggers correct element hover and click states, with no offset between visual position and hit region

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


