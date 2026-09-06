## Why

The initial Solim refactor established signals, computeds, and basic declarative components, but still retained imperative patterns that conflict with Solim's intended design. Specifically, components relied on explicit lifecycle ownership (`own()`, `ownChild()`), manual `Effect` instances for basic element property bindings, reactive `.get()` reads inside Arc layout methods (`getPrefWidth`), and coarse list-wide revision rebuilds to reflect individual feature state changes. Additionally, `FeatureSettingDialog` needlessly disposed and reconstructed `FeatureSettingsView` on every dialog hide/show cycle.

Cleaning up these patterns now ensures Solim components are truly declarative by default, lifecycle ownership is automatic within component build contexts, structural reactivity is decoupled from property reactivity, and reactive values update existing Arc elements directly.

## What Changes

- **Automatic Lifecycle Ownership**: BaseComponent and the build context automatically track child components, subscriptions, and Solim controls (e.g., `SolimTextField`) instantiated during `build()`, eliminating manual `own()` and `ownChild()` calls in component code.
- **Direct Element Property Bindings**: Introduce direct, declarative property binding primitives in Solim (such as `.width(Readable<Float>)`, `.color(Readable<Color>)`, `.text(Readable<String>)`, and element-level property binders) so that simple UI properties update Arc elements directly without manual `Effect` boilerplate.
- **Elimination of Reactive Reads in Arc Layout Lifecycle**: Remove reactive reads (`.get()`) from `getPrefWidth()`, `getPrefHeight()`, `layout()`, `draw()`, and `act()`. Arc layout operates solely on standard element properties, which are modified reactively via Solim bindings.
- **Removal of Legacy Imperative FeatureCard API**: **BREAKING**: Remove static `FeatureCard.build(Table, Feature, float, Runnable)`. `FeatureCard` must be instantiated via constructor and mounted by the parent layout or `ReactiveGrid`.
- **Property-Level Reactivity for FeatureCard**: `FeatureCard` accepts a reactive `Readable<Boolean> enabled` and updates card background color, status text, and status label color directly on existing Arc elements without rebuilding the card or clearing the container table.
- **Decoupled Structural vs Property Reactivity**: Structural changes (filtering, adding, removing, reordering features) are handled by `ReactiveGrid`, while individual feature state toggles update existing `FeatureCard` property bindings directly without requiring global `revision` bumps that recreate cards.
- **Persistent View Lifecycle in FeatureSettingDialog**: `FeatureSettingDialog` acts as a thin host that instantiates and mounts `FeatureSettingsView` once; the view is reused across show/hide cycles (`shown(view::onShown)`), updating width on resize, and only disposed when the dialog itself is permanently disposed.
- **Declarative ReactiveGrid Empty State**: Support a declarative `.empty(Runnable)` DSL block in `ReactiveGrid` instead of requiring anonymous `BaseComponent` instances.
- **Declarative DSL in FeatureSettingsView**: Refactor `FeatureSettingsView` to use Solim's declarative DSL (`column`, `scroll`, `reactiveGrid`, `textField`) rather than manually constructing intermediate `Table` elements.

## Capabilities

### New Capabilities
- `solim-automatic-ownership`: Component and build-context automatic tracking and disposal of child components, reactive property bindings, and Solim widgets without manual `own()` or `ownChild()` calls.
- `solim-property-bindings`: Direct reactive property binding mechanisms for Arc scene elements (width, height, color, text, visibility, enabled state) that mutate existing elements on signal changes without standalone `Effect` definitions or layout-time signal reads.

### Modified Capabilities
- `feature-settings-dialog`: Update dialog and view specifications to require a persistent view instance across show/hide events, fine-grained property reactivity within `FeatureCard`, removal of the static `build` API, declarative empty state specification, and elimination of manual ownership registration.

## Impact

- `mindustrytool.features.settings.FeatureCard`: Constructor updated to accept reactive enabled state; static `build()` method removed; `getPrefWidth()` reactive reads removed in favor of direct width binding; direct color and status bindings applied.
- `mindustrytool.features.settings.FeatureSettingsView`: Rebuilt with declarative Solim DSL; manual `own()` and `ownChild()` removed; grid empty view uses `.empty(...)`; feature list reactivity separated from individual feature state.
- `mindustrytool.features.settings.FeatureSettingDialog`: Converted to thin host; view created once and reused across show/hide cycles; view disposed only on dialog disposal.
- `solim.core.BaseComponent` & `solim.ui.ParentStack` / build context: Enhanced to provide automatic ambient ownership of child components and bindings created during `build()`.
- `solim.layout.ReactiveGrid`: Added declarative `.empty(Runnable)` support alongside existing component empty view, with automatic child ownership.
- Tests in `FeatureSettingDialogTest` and Solim test suite updated to verify automatic ownership, persistent dialog view lifecycle, and direct property updates.
