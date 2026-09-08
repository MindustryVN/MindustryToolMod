## Context

In Solim UI, components added to layout containers have historically been subjected to automatic expansion by default:
- `Column.ATTACHER` automatically called `cell.growX()` for any child that did not specify an explicit preferred or bounded width.
- `Card.ATTACHER` and `Scroll.ATTACHER` unconditionally applied `cell.growX()` to every attached child.
- `Row.ATTACHER` automatically applied `cell.growX()` to any `TextField` child.
- `ForEach` explicitly added children with `.growX().row()`.
- `Dynamic` added children with `.grow()`.

This default behavior is contrary to standard declarative layout principles (such as Flutter, Jetpack Compose, CSS flexbox, or standard Mindustry/Arc UI conventions), where child components retain their intrinsic content or explicit dimensions unless explicitly declared to expand (`grow`, `growX`, or `growY`). It caused buttons, cards, text fields, and custom components to stretch across available container width unexpectedly, requiring manual workarounds.

## Goals / Non-Goals

**Goals:**
- Eliminate default `grow` and `growX` behavior across all Solim containers (`Column`, `Row`, `Card`, `Scroll`, `ForEach`, `Dynamic`).
- Ensure child elements and components retain their natural content size (or explicit width/height constraints) unless explicitly configured to grow.
- Make growth an opt-in behavior via explicit modifiers (`.grow()`, `.growX()`, `.growY()`) or inherently expanding components (`Spacer`).
- Update existing views and test suites to explicitly declare `.growX()` or `.grow()` where stretching to container bounds is desired.

**Non-Goals:**
- Replacing or modifying Arc's underlying `Cell` / `Table` layout engine. Solim continues to translate reactive constraints into Arc cell attributes.
- Altering the behavior of `Spacer`, which exists specifically to expand and consume unused space (`Ui.isExpanding(child)`).
- Removing the `.grow()`, `.growX()`, or `.growY()` modifiers on `LayoutModifiers`.

## Decisions

### Decision 1: Remove automatic `growX` in `Column.ATTACHER`, `Card.ATTACHER`, `Scroll.ATTACHER`
- **Choice**: Only apply `cell.growX()` or `cell.growY()` when the child element explicitly requests it via `ConstrainedElement` size constraints (`growX == true` or `growY == true`) or when the child is an expanding element (`Ui.isExpanding(child)`).
- **Details**:
  - In `Column.ATTACHER`, remove the `!hasExplicitWidth` check that was calling `cell.growX()`. Children with `.growX()` will still receive `cell.growX()` when `SizeConstraints.applyToCell` is called in `ParentStack.doAttach`.
  - In `Card.ATTACHER`, remove unconditional `cell.growX()`.
  - In `Scroll.ATTACHER`, remove unconditional `cell.growX()`.
- **Alternatives Considered**: Keeping default grow for certain containers like `Scroll` or `Column`. Rejected because inconsistent growth rules between containers cause developer confusion.

### Decision 2: Remove automatic `growX` for `TextField` in `Row.ATTACHER`
- **Choice**: In `Row.ATTACHER`, remove `child instanceof TextField` check. Only expanding elements (`Ui.isExpanding(child)`) or elements with explicit `growX` will expand in a `Row`. Text fields that need to expand across the row must explicitly call `textField(...).growX()`.
- **Rationale**: Special-casing `TextField` violates uniformity. Developers may want fixed-width or natural-width text fields in rows (e.g. short number inputs or search bars with explicit sizes).

### Decision 3: Remove forced `growX` / `grow` in `ForEach` and `Dynamic`
- **Choice**:
  - In `ForEach`, attach children with `Cell<?> cell = container.add(comp.element()).row();` and apply any `ConstrainedElement` size constraints (or attach via standard attacher).
  - In `Dynamic`, attach child with `Cell<?> cell = container.add(currentComponent.element());` and apply size constraints rather than unconditionally calling `.grow()`.
- **Rationale**: Lists and dynamic views should respect the sizing declared on the child items themselves rather than forcing them to stretch.

### Decision 4: Divider and Spacer semantics
- **Choice**: `Spacer` remains an expanding element recognized by `Ui.isExpanding(child)` (expanding horizontally in `Row` and vertically in `Column`). `Divider` creates a horizontal divider line that spans the container width by having `growX` on its internal table cell, or implementing `LayoutModifiers<Divider>` / `growX()`.

### Decision 5: Explicit update of existing views
- **Choice**: In mod views (e.g., `FeatureSettingsView`, `FeatureCard`, `QuickAccessSettingsView`), add explicit `.growX()` or `.grow()` where full-width stretching was previously happening implicitly (e.g., search text field in toolbar, full-width headers or separators).

## Risks / Trade-offs

- **[Risk] Visual regressions in existing mod UI**: Some components that previously relied on implicit full-width stretching might collapse to their preferred/content width.
  - *Mitigation*: Audit existing views in `mindustrytool` (`FeatureSettingsView`, `FeatureCard`, `FeatureHelpView`, `QuickAccessSettingsView`) and add `.growX()` where stretching is intended.
- **[Risk] Broken existing tests**: Existing Solim layout tests may assert that child cells in `Column` or `Card` have `expandX > 0`.
  - *Mitigation*: Update unit tests in `LayoutTest` to assert that components do not grow by default, and that only elements with explicit `.growX()` or spacers expand.
