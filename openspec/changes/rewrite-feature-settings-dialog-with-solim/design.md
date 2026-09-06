## Context

`FeatureSettingDialog` serves as the primary configuration and status hub for all mod features in MindustryTool. Users can search for features by name, ID, or description, toggle their activation status, trigger bulk re-enable actions, and navigate to individual feature settings or help dialogs.

Historically, this dialog was written using standard imperative Arc UI table-building code. State synchronization between search filters, the global `FeatureManager`, event bus events (`FeatureStateChanged`), and window resize events required manual invocation of `rebuild()` and `rebuildPane()`.

With the introduction of the `:solim` module, the project now has a declarative and reactive UI foundation featuring:
- Reactive state primitives: `Signal<T>`, `Computed<T>`, and `Effect`.
- Two-way bound widgets: `SolimTextField`, `Button`, `Switch`, etc.
- Structured declarative layouts: `Ui.column`, `Ui.row`, `Grid`, and `ParentStack`.
- Lifecycle management: `Disposable` and `Subscription`.

Rewriting `FeatureSettingDialog` with Solim brings clean reactive data flow, simplifies state tracking, and establishes a best-practice reference implementation for future UI rewrites across the mod.

## Goals / Non-Goals

**Goals:**
- Re-architect `FeatureSettingDialog` using Solim's declarative components and reactive primitives.
- Replace manual rebuild tracking with reactive signals (`Signal<String>` for filter, `Computed<Seq<Feature>>` for filtered results, reactive column computation).
- Leverage Solim's widget abstractions (such as `SolimTextField` and declarative layout wrappers) for the search bar and action controls.
- Maintain full parity with existing UX, styling, Discord report flow, and localization bundle keys.
- Ensure proper lifecycle cleanup (disposing effects and unregistering event listeners when the dialog is closed).
- Adhere strictly to project AGENTS.md rules (Java 8 runtime compatibility, no hardcoded strings).

**Non-Goals:**
- Rewriting child dialogs (`FeatureHelpDialog` or feature-specific settings dialogs) in this change.
- Altering the underlying `Feature` or `FeatureManager` public APIs.
- Replacing Mindustry's top-level window management (`BaseDialog` / `Dialog`) with headless overlays.

## Decisions

### Decision 1: Hybrid BaseDialog integration with Solim declarative content
- **Choice**: Keep `FeatureSettingDialog` extending `BaseDialog`, but construct its content hierarchy (`cont`) and reactive state using Solim.
- **Rationale**: Mindustry's `BaseDialog` handles essential top-level windowing behaviors: stage layering, modal backdrop, input capture, `closeOnBack()`, and mobile platform navigation. Solim focuses on reactive state and declarative element assembly. Hosting Solim within `BaseDialog.cont` provides the best of both worlds without reimplementing windowing mechanics.
- **Alternatives Considered**:
  - *Full `SolimDialog`*: `SolimDialog` is currently an unanchored table overlay without complete Mindustry modal lifecycle or back button handling.

### Decision 2: Reactive State Architecture
- **Choice**:
  - `Signal<String> filter = Signal.of("")` for the search query.
  - `Signal<Integer> revision = Signal.of(0)` incremented whenever features change (`FeatureStateChanged` or re-enable).
  - `Computed<Seq<Feature>> filteredFeatures` derived from `filter` and `revision`.
  - `Signal<Float> contentWidth` updated on dialog resize/shown.
  - `Computed<Integer> columnCount` derived from `contentWidth`.
  - An `Effect` tracking `filteredFeatures` and `columnCount` to reconcile the card grid pane.
- **Rationale**: Decouples input events and window sizing from UI rendering. Re-filtering occurs automatically when the query changes without manual calls to `rebuildPane()`.

### Decision 3: Componentizing Feature Cards
- **Choice**: Encapsulate card creation with Solim components or declarative builders that bind feature enabled status reactively.
- **Rationale**: Isolates card styling (border colors, icon sizing, button click bubbling prevention) into modular UI functions. Clicking a card updates feature state and updates `revision`, triggering reactive updates.

### Decision 4: Lifecycle & Resource Cleanup
- **Choice**: Implement `Disposable` in `FeatureSettingDialog`. Register the `FeatureStateChanged` event listener and instantiate reactive `Effect` instances during dialog initialization/show, and clean them up via `dispose()` when hidden or removed.
- **Rationale**: Prevents memory leaks and dangling subscriptions when dialogs are closed or reused.

## Risks / Trade-offs

- **[Risk] Input Event Propagation on Card Buttons**: Clicking child buttons (settings, help, open dialog) inside a card might trigger the parent card click listener (toggling feature state).
  - **Mitigation**: Maintain explicit click listener event stopping (`event.stop()`) on all nested action buttons.
- **[Risk] Java 8 Runtime Incompatibility**: Solim or rewritten dialog code might accidentally use Java 9+ APIs (`List.of`, `stream.toList()`).
  - **Mitigation**: Strictly use Arc's `Seq`, Java 8 collections, and verify against AGENTS.md requirements.
- **[Risk] Reactive Re-render Churn**: Typing in search field could trigger excessive re-renders.
  - **Mitigation**: Solim's `Signal` only fires updates when values change (`equals` check), and `SolimTextField` incorporates an equality guard.
