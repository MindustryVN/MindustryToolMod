## Why

`FeatureCard` currently constructs raw Arc Scene2D elements (`Button`, `Table`, `Label`) manually and invokes static methods on a separate `Binding` utility class (`Binding.bindWidth`, `Binding.bindColor`, `Binding.bindText`), violating Solim's declarative-first philosophy and component-owned reactive binding principles. Rewriting `FeatureCard` with pure Solim declarative UI and introducing fluent, chained component property bindings enables clean, reactive, and reusable card and widget patterns across the mod.

## What Changes

- **Solim Card Component**: Introduce `Card` (in `solim.layout` or `solim.ui`) as a declarative, clickable, styled container component supporting chained reactive property bindings (`width`, `height`, `prefHeight`, `color`, `style`, `padding`, `onClick`).
- **Solim IconButton Widget**: Introduce `IconButton` (in `solim.input`) with `Ui.iconButton(...)` facades, supporting icon drawables, custom button styles (such as `ImageButtonStyle` / `Styles.clearNonei`), sizing, tooltips, and click event bubbling control (`stopClickPropagation`).
- **Chained Reactive Bindings on Solim Components**: Extend Solim components (`Text`, `Button`, `IconButton`, `Card`) with fluent methods that internally wire reactive property bindings (`.color(Readable<Color>)`, `.width(Readable<Float>)`, `.text(Readable<String>)`, `.style(...)`, `.wrap(...)`, `.ellipsis(...)`), eliminating the need for callers to use an external `Binding` utility class directly.
- **Rewrite `FeatureCard`**: Rebuild `FeatureCard.java` entirely with Solim declarative components (`card`, `column`, `row`, `image`/`icon`, `text`, `iconButton`, `spacer`) and chained bindings, preserving all existing functional capabilities (state toggling, reactive width, reactive status color/text, shortcut dialog buttons, and help dialog).

## Capabilities

### New Capabilities
- `solim-card`: Reusable declarative card container component supporting child elements, sizing, reactive background color/width, click actions, and event propagation control.

### Modified Capabilities
- `solim-widgets`: Add `IconButton` widget and enrich widgets (`Text`, `Button`, `IconButton`) with fluent chained property modifiers and internal reactive bindings for styling, wrapping, truncation, and color/dimensions.
- `solim-declarative-ui`: Add `card(...)` and `iconButton(...)` facades to `Ui` supporting declarative composition.

## Impact

- `mod/src/mindustrytool/features/settings/FeatureCard.java`: Fully refactored to declarative Solim UI without raw Scene2D table manipulation or external `Binding` class calls.
- `solim`: Addition of `Card` component, `IconButton` widget, enriched `Text` and `Button` APIs, and new `Ui` facades.
- Backward compatibility: Existing `FeatureCard` constructors and methods (`enabled()`, `cardWidth()`, `element()`, `dispose()`) remain compatible with `FeatureSettingsView`.
