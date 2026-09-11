## Why

Solim components should be clean, declarative, reactive wrappers around Arc elements that do not alter Arc's internal layout calculations or runtime behavior. The introduction of `ConstrainedElement` and subclasses overriding Arc layout methods (`SizedTable`, `SizedButton`, `SizedLabel`, `SizedImage`, `SizedTextField`, `SizedCheckBox`, `CardButton`) interfered with Arc's native layout engine, violated the project's principle that Solim components must only wrap Arc rather than change its behavior, and caused architectural coupling across containers and widgets.

## What Changes

- **BREAKING**: Remove the `solim.layout.ConstrainedElement` interface.
- Remove or eliminate `SizedTable`, `SizedButton`, `SizedLabel`, `SizedImage`, `SizedTextField`, `SizedCheckBox`, and `CardButton` subclasses that override Arc sizing/layout methods (`getPrefWidth`, `getPrefHeight`, `getMinWidth`, etc.) or store `SizeConstraints` on Arc elements.
- Solim components (`Button`, `Text`, `SolimImage`, `SolimTextField`, `Checkbox`, `Row`, `Column`, `Grid`, `Card`, `Scroll`, etc.) wrap standard Arc widgets directly (`Table`, `arc.scene.ui.Button`, `Label`, `Image`, `TextField`, `CheckBox`, `ScrollPane`) without overriding Arc layout calculations.
- Remove `instanceof ConstrainedElement` checks from `ParentStack`, `ForEach`, `Dynamic`, `ReactiveGrid`, and `ElementModifiers`.
- Sizing and layout configuration (e.g. `width`, `height`, `growX`, `growY`, `pad`, `align`) apply directly to Arc elements or their parent `Cell` via Arc's native layout mechanisms without altering Arc element behaviors.
- Update `mindustrytool.features.teamresource.SplitBar` in `mod` to remove `ConstrainedElement` implementation.

## Capabilities

### New Capabilities

### Modified Capabilities
- `solim-layout`: Remove `ConstrainedElement` and `SizedTable` overrides; ensure layout containers wrap standard Arc `Table` and configure cells directly using Arc's native layout engine.
- `pure-solim-components`: Solim components wrap standard Arc widgets directly and SHALL NOT subclass Arc elements to override layout methods or attach synthetic constraint objects.

## Impact

- **Affected code**: `solim.layout.ConstrainedElement` (removed), `SizedTable` (removed), `Button`, `Card`, `Text`, `SolimImage`, `SolimTextField`, `Checkbox`, `ParentStack`, `ElementModifiers`, `ForEach`, `Dynamic`, `ReactiveGrid`, `Row`, `Column`, `Grid`, `Scroll`, and `mindustrytool.features.teamresource.SplitBar`.
- **APIs**: Removal of `ConstrainedElement`. Fluent component modifiers continue to work, delegating directly to Arc elements and Arc cells.
- **Dependencies**: None.
