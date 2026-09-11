## Context

Solim was originally conceived as a declarative, reactive wrapper over Arc UI for Mindustry mod development. However, an earlier revision introduced `ConstrainedElement`, `SizeConstraints`, and custom subclasses of Arc widgets:
- `SizedTable extends Table implements ConstrainedElement`
- `SizedButton extends arc.scene.ui.Button implements ConstrainedElement`
- `SizedLabel extends Label implements ConstrainedElement`
- `SizedImage extends Image implements ConstrainedElement`
- `SizedTextField extends TextField implements ConstrainedElement`
- `SizedCheckBox extends CheckBox implements ConstrainedElement`
- `CardButton extends Button implements ConstrainedElement`
- `SplitBar extends Element implements ConstrainedElement`

These classes overrode Arc layout and measurement methods (`getPrefWidth`, `getPrefHeight`, `getMinWidth`, `getMinHeight`, `getMaxWidth`, `getMaxHeight`) and stored synthetic constraint objects directly on Arc scene graph elements. In addition, containers (`ParentStack`, `ForEach`, `Dynamic`, `ReactiveGrid`) and utilities (`ElementModifiers`) checked for `instanceof ConstrainedElement` and altered Arc's standard cell and element layout behavior.

This directly conflicts with the project's core rule: **Solim components must only wrap Arc without changing its behavior**.

## Goals / Non-Goals

**Goals:**
- Completely remove the `solim.layout.ConstrainedElement` interface.
- Remove all `Sized*` subclasses that override Arc layout calculation methods or implement `ConstrainedElement`.
- Ensure Solim components (`Button`, `Text`, `SolimImage`, `SolimTextField`, `Checkbox`, `Card`, `Row`, `Column`, `Grid`, `Scroll`, `Dynamic`, `ForEach`, `ReactiveGrid`) wrap standard Arc widgets directly (`Table`, `arc.scene.ui.Button`, `Label`, `Image`, `TextField`, `CheckBox`, `ScrollPane`).
- Ensure layout properties (`width`, `height`, `size`, `grow`, `growX`, `growY`, `pad`, `margin`, `align`) operate through Arc's native layout mechanics (`Cell` methods on the parent `Table`, `setSize()` on elements) rather than overriding Arc's measurement methods.
- Remove all `instanceof ConstrainedElement` checks throughout `ParentStack`, `ForEach`, `Dynamic`, `ReactiveGrid`, and `ElementModifiers`.
- Update `mindustrytool.features.teamresource.SplitBar` to be a standard Arc `Element` without `ConstrainedElement`.
- Keep existing component public APIs and declarative fluent syntax fully functional.

**Non-Goals:**
- Reimplementing or modifying Solim's core reactive signals (`Signal`, `Computed`, `Effect`, `Readable`).
- Introducing a custom layout or constraint solver engine.
- Modifying legacy code in `old/`.

## Decisions

### Decision 1: Remove `ConstrainedElement` and `SizeConstraints`
- **Choice**: Delete `solim.layout.ConstrainedElement` entirely. Transition `LayoutModifiers` and layout containers away from `SizeConstraints` storing on elements, and rely on Arc's native `Cell` and `Element` configuration.
- **Rationale**: Storing artificial constraint objects on Arc elements and checking `instanceof ConstrainedElement` fundamentally breaks the "wrap, don't change" contract.
- **Alternatives Considered**: Keeping `ConstrainedElement` as a deprecated no-op interface. Rejected: Dead code introduces confusion and violates clean architecture.

### Decision 2: Use Native Arc Widgets Directly in Solim Components
- **Choice**:
  - `Row`, `Column`, `Grid`, `SolimStack`, `Divider`, `Dynamic`, `ForEach`, `ReactiveGrid`: wrap `arc.scene.ui.layout.Table`.
  - `Button`: wraps `arc.scene.ui.Button`.
  - `Text`: wraps `arc.scene.ui.Label`.
  - `SolimImage`: wraps `arc.scene.ui.Image`.
  - `SolimTextField`: wraps `arc.scene.ui.TextField`.
  - `Checkbox`: wraps `arc.scene.ui.CheckBox`.
  - `Card`: wraps `arc.scene.ui.Button` (as card button) and `Table` (as container).
  - `Scroll`: wraps `arc.scene.ui.ScrollPane` and `Table`.
- **Rationale**: Standard Arc widgets are fully tested and optimized within Mindustry's UI pipeline. Overriding `getPrefWidth` etc. causes unexpected measurement conflicts with Arc's retained layout tree.
- **Alternatives Considered**: Subclassing without `ConstrainedElement`. Rejected: If no methods need to be overridden, subclassing is unnecessary and prevents standard Arc usage.

### Decision 3: Configure Layout via Native Arc `Cell` and `Element`
- **Choice**:
  - When a component is added to a parent `Table`, Arc creates a `Cell<?>`.
  - Layout modifiers on components (`growX()`, `growY()`, `width()`, `height()`, `pad()`, `margin()`, `align()`) configure the parent `Cell` when attached (`((Table) element.parent).getCell(element)`), and configure the element directly (`element.setSize()`, `element.setWidth()`, `element.setHeight()`).
  - Containers that manage children during `ParentStack` attachment apply cell properties natively during attachment and when chained after attachment.
  - Spacing, margins, and gaps continue to delegate to `ElementModifiers` and Arc `Cell.pad(...)`.
- **Rationale**: Arc's layout model is cell-driven. Sizing inside a `Table` is meant to be configured on the `Cell`, leaving the element's internal layout calculations pure.
- **Alternatives Considered**: Reimplementing CSS box model inside Solim. Rejected: The mod runs in Mindustry, not a browser; adhering to Arc's native model ensures reliable layout.

### Decision 4: Update `SplitBar` in `mod`
- **Choice**: In `SplitBar.java`, remove `implements ConstrainedElement` and the `constraints` field. Maintain its standard Arc `Element` overrides (`draw()`, `getPrefWidth()`, `getPrefHeight()`) and `this.userObject = "expanding"`.
- **Rationale**: `Ui.isExpanding()` already checks `child.userObject == "expanding"`, which allows `ParentStack` to automatically expand the `SplitBar` cell without `ConstrainedElement`.

## Risks / Trade-offs

- **[Risk]** Layout tests that specifically tested `ConstrainedElement` or `SizedTable` overrides will need adjustment.
  → **Mitigation**: Update test assertions in `LayoutTest` and component tests to verify cell and element dimensions using Arc's standard layout lifecycle (`table.validate()`, `table.layout()`).
- **[Risk]** Chained modifiers called before an element is attached to a parent `Table`.
  → **Mitigation**: Ensure modifiers set element dimensions (`setWidth`, `setHeight`, `setSize`) immediately so natural size reflects the requested dimensions, and when attached, cell respects them. For containers that record cell modifiers, apply them when the cell is created in `ParentStack.doAttach`.
- **[Risk]** Reactive width/height signals needing to update parent cells.
  → **Mitigation**: Reactive width/height bindings in components update both the element and its parent cell (if attached) and invalidate the table hierarchy.
