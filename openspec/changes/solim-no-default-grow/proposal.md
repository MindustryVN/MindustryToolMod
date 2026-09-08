## Why

Solim layout containers currently impose automatic horizontal stretching (`growX`) or omnidirectional growth (`grow`) on child elements by default (e.g., Column auto-grows children that lack explicit width constraints, Card and Scroll auto-grow children horizontally, Row auto-grows text fields, and ForEach/Dynamic enforce grow). This default behavior violates the principle of least surprise, causes elements to stretch across the full width unintentionally, and contradicts declarative UI layout design where components size to their natural content unless explicitly instructed to expand.

## What Changes

- **BREAKING**: Layout containers no longer automatically apply `growX()` or `grow()` to child elements by default.
- In `Column.ATTACHER`, child elements will only grow horizontally if `growX` is explicitly set via `SizeConstraints` (`.growX()` or `.grow()`) or if the child is an expanding element (`Ui.isExpanding(child)`).
- In `Card.ATTACHER` and `Scroll.ATTACHER`, remove unconditional `cell.growX()` for attached children; children retain natural/specified constraints and grow only if marked to grow.
- In `Row.ATTACHER`, remove automatic `growX()` for `TextField`. Text fields will require explicit `.growX()` if expansion is desired.
- In `ForEach` and `Dynamic`, remove forced `growX()` / `grow()` on child components.
- Components wanting to fill container width or height must explicitly use `.growX()`, `.growY()`, or `.grow()`.
- Existing UI layouts and Solim tests that rely on children stretching to container bounds are updated to declare `.growX()` or `.grow()` explicitly.

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `solim-layout`: Modify container child attachment rules so components do not grow horizontally or vertically by default; grow must be explicitly specified via modifiers or expanding elements.

## Impact

- Layout containers in `solim.layout` (`Column`, `Row`, `Card`, `Scroll`) and structural components (`ForEach`, `Dynamic`).
- Existing views in `mindustrytool` and tests in `solim` that expect full-width stretching without an explicit `.growX()` call.
