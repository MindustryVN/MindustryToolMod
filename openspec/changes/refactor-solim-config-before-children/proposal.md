## Why

In the current Solim UI library, container configuration methods (such as `.grow()`, `.padding()`, `.align()`, `.gap()`) are chained after the child builder lambda (e.g. `column(() -> { ... }).grow()`). When container component bodies grow large, the configuration modifiers end up visually separated from the component declaration by tens or hundreds of lines of child code, making component structure and layout rules difficult to read and maintain.

Moving component configuration to appear before declaring children (e.g. `column().grow().children(() -> { ... })`) keeps the component's setup visually adjacent to its declaration while preserving declarative composition and automatic parent/child lifecycle binding.

## What Changes

- Refactor Solim UI layout containers (`Column`, `Row`, `Scroll`, `Card`, `Grid`, `SolimStack`, `SolimDialog`, `Wrap`, `Container`) to support a fluent `children(Runnable)` method for setting child elements after configuring container modifiers.
- Update `solim.ui.Ui` facade helper methods (`column()`, `row()`, `scroll()`, `card()`, `grid()`, `container()`, `stack()`, `wrap()`, `dialog()`) to support parameterless overload declarations that return fluent container builders, allowing configuration before children.
- Update `solim.ui.ParentStack` to defer attaching container layout elements to active parents until `.children(Runnable)` or `.element()` is invoked, ensuring modifier configurations (such as `.grow()`, `.growX()`, `.growY()`, `.padding()`, `.align()`) are fully applied prior to layout cell attachment.
- Migrate all existing Solim UI components, dialogs, views, and test suites across the repository to the new configuration-before-children style.
- Deprecate or remove old lambda-first overload constructors/facades (`column(Runnable)`, `row(Runnable)`, etc.) after clean migration.

## Capabilities

### New Capabilities

*(None)*

### Modified Capabilities

- `solim-declarative-ui`: Declarative UI container helpers now use configuration-before-children builder methods (`component().modifier().children(() -> { ... })`) instead of requiring children lambdas directly in static helper arguments.
- `solim-layout`: Layout containers (`Column`, `Row`, `Scroll`, `Card`, `Grid`, `SolimStack`) support fluent modifier chaining before child declaration via `.children(Runnable)`.

## Impact

- **Solim Library**: `solim.ui.Ui`, `solim.ui.ParentStack`, `solim.layout.*` (`Column`, `Row`, `Scroll`, `Card`, `Grid`, `SolimStack`, `Wrap`, `Container`), `solim.overlay.SolimDialog`.
- **MindustryTool Mod**: All UI views, cards, and dialogs using Solim (`FeatureSettingsView`, `FeatureHelpView`, `FeatureCard`, `BackgroundSettingsDialog`, `CrashReportDialog`, `UpdateDialog`, `AuthOverlay`, etc.).
- **Tests**: Solim layout unit tests (`LayoutTest`, `ParentStackTest`, `StructuralReactivityTest`, `ComponentTest`, `SignalTest`, `EffectTest`, etc.).
