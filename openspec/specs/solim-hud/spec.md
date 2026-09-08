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
The `Hud` container SHALL support a `keepInScreen()` method and automatically register an Arc `ResizeEvent` listener on construction to clamp its $(x, y)$ coordinates within $[0, \text{screenWidth} - \text{hudWidth}]$ and $[0, \text{screenHeight} - \text{hudHeight}]$.

#### Scenario: Clamping Hud coordinates when positioned outside screen
- **WHEN** a `Hud` has width 200, height 100, and $(x, y)$ is set to $(1200, 900)$ on a $1024 \times 768$ screen
- **THEN** `keepInScreen()` clamps $(x, y)$ to $(824, 668)$

#### Scenario: Screen resize event keeps Hud within viewport
- **WHEN** a `ResizeEvent` occurs in the game runtime
- **THEN** `Hud` updates its layout and clamps coordinates within the new viewport dimensions

### Requirement: Draggable modifier for Hud repositioning
The Solim framework SHALL provide a `.draggable()` modifier (or handle) that attaches touch drag handling to an element. When dragged, it SHALL translate the parent `Hud` container by the drag delta, clamp coordinates to the screen, and optionally update reactive `Signal<Float>` x and y positions.

#### Scenario: Dragging a handle moves the Hud
- **WHEN** a user touches down and drags an element configured with `.draggable()`
- **THEN** the parent `Hud` element's position moves by the touch displacement vector and updates the associated coordinate signals

#### Scenario: Clamping during drag movement
- **WHEN** a drag movement attempts to push the `Hud` outside screen bounds
- **THEN** the movement is clamped at the screen border

