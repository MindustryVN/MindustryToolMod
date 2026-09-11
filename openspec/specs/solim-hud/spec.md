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
The Solim framework SHALL provide a `.draggable()` modifier (or handle) that attaches touch drag handling to an element. When the handle is a `Table` container, the modifier SHALL set `touchable = childrenOnly` so that child elements (e.g. buttons) still receive touch events, while drag gestures on the empty background area of the handle are handled by the drag listener. When the handle is a non-Table element, `touchable = enabled` SHALL be set. When dragged from a background (non-child) area, it SHALL translate the parent `Hud` container by the drag delta, clamp coordinates to the screen, and optionally update reactive `Signal<Float>` x and y positions.

#### Scenario: Dragging a handle moves the Hud
- **WHEN** a user touches down and drags an element configured with `.draggable()`
- **THEN** the parent `Hud` element's position moves by the touch displacement vector and updates the associated coordinate signals

#### Scenario: Clamping during drag movement
- **WHEN** a drag movement attempts to push the `Hud` outside screen bounds
- **THEN** the movement is clamped at the screen border

#### Scenario: Table drag handle does not block child button clicks
- **WHEN** a `Table` element is configured as a drag handle via `.draggable()` and contains child buttons
- **THEN** clicking a child button fires the button's click listener and does NOT initiate dragging

#### Scenario: Non-Table drag handle is touchable
- **WHEN** a non-Table element (e.g. a plain `Element`) is configured as a drag handle
- **THEN** `touchable` is set to `enabled` so the element itself receives touch events

