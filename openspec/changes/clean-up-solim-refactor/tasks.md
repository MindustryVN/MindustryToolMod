## 1. Solim Automatic Ownership and Ambient Build Context

- [x] 1.1 Implement ambient `ComponentContext` stack in `BaseComponent` that tracks the currently active building component.
- [x] 1.2 Update `BaseComponent.element()` to push/pop the ambient component context in a try-finally block.
- [x] 1.3 Update child component instantiation and resolution hooks so child components created during a component's `build()` are automatically registered and owned.
- [x] 1.4 Update `SolimTextField` to automatically register its disposables with the ambient component context when instantiated during build without requiring manual `own()`.
- [x] 1.5 Update `ReactiveGrid` to automatically manage child components without parent components calling `ownChild(grid)`.

## 2. Solim Direct Property Bindings & Layout Isolation

- [x] 2.1 Implement direct element property bindings in `solim.ui.Binding` for `bindWidth`, `bindHeight`, `bindColor`, `bindText`, `bindVisible`, and `bindDisabled`.
- [x] 2.2 Ensure property bindings automatically register with the ambient component context and execute direct Arc element mutation plus layout invalidation (`invalidateHierarchy()`).
- [x] 2.3 Add `.empty(Runnable)` declarative builder support to `ReactiveGrid` to allow inline declarative empty states without anonymous `BaseComponent` boilerplate.
- [x] 2.4 Verify and enforce that no reactive `.get()` calls occur within Arc layout lifecycle overrides (`getPrefWidth()`, `layout()`, etc.).

## 3. FeatureCard Reactive Refactor

- [x] 3.1 Delete legacy static `FeatureCard.build(Table, Feature, float, Runnable)` API.
- [x] 3.2 Update `FeatureCard` constructor to accept `Feature feature`, `Readable<Float> cardWidth`, and `Readable<Boolean> enabled`.
- [x] 3.3 Replace manual `Effect` and `getPrefWidth()` override with direct reactive width binding.
- [x] 3.4 Bind card background color, status label text, and status label color directly to `enabled` without rebuilding the card or clearing tables.
- [x] 3.5 Remove all `own(...)` and `ownChild(...)` calls from `FeatureCard`.

## 4. FeatureSettingsView Declarative Cleanup

- [x] 4.1 Refactor `FeatureSettingsView.build()` to use Solim declarative DSL (`column`, `scroll`, `reactiveGrid`, `row`) instead of manually constructing intermediate `Table` instances.
- [x] 4.2 Decouple structural reactivity from individual feature state by maintaining reactive enabled states for features, updating individual feature signals on state changes instead of relying on full-list `revision` bumps.
- [x] 4.3 Replace manual `own(SolimTextField.of(filter))` with auto-owned search field.
- [x] 4.4 Convert `reactiveGrid(...).emptyView(...)` to declarative `reactiveGrid(...).empty(() -> { ... })`.
- [x] 4.5 Remove all `own(...)` and `ownChild(...)` calls from `FeatureSettingsView`.

## 5. FeatureSettingDialog Lifecycle Refactor

- [x] 5.1 Refactor `FeatureSettingDialog` into a thin host: instantiate `FeatureSettingsView` once and mount it to `cont`.
- [x] 5.2 Remove `hidden(() -> view.dispose())` and dynamic view recreation logic on `shown(...)`.
- [x] 5.3 Connect `shown(view::onShown)` and `resized(() -> view.updateWidth(calcWidth()))` directly to the persistent view instance.
- [x] 5.4 Ensure `view` is disposed only when `FeatureSettingDialog.dispose()` is invoked.

## 6. Verification and Compatibility

- [x] 6.1 Update existing tests in `FeatureSettingDialogTest` to verify persistent view lifecycle, fine-grained `FeatureCard` property reactivity, and absence of `own()`/`ownChild()` calls.
- [x] 6.2 Add unit tests for Solim ambient component ownership, direct property bindings, and `ReactiveGrid.empty(Runnable)`.
- [x] 6.3 Run full test suite via `./gradlew test` to ensure all tests pass.
- [x] 6.4 Verify compliance with project rules: Java 8 runtime API compatibility (no Java 9+ APIs), and i18n bundle integrity.
