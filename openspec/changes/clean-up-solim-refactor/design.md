## Context

The first phase of the Solim migration introduced core reactive building blocks (`Signal`, `Computed`, `Effect`), layout primitives (`Column`, `Row`, `ParentStack`), and converted `FeatureSettingsView` and `FeatureCard`. However, the implementation relied on manual lifecycle management (`own()`, `ownChild()`), manual `Effect` declarations for simple layout/style property mutations, reactive reads inside Arc layout methods (`getPrefWidth()`), coarse-grained full list invalidation via `revision`, and full view recreation on dialog hide/show cycles.

This design establishes an architectural cleanup to ensure Solim components are declarative by default, lifecycle ownership is ambient and automatic during `build()`, and reactive bindings update existing Arc scene elements directly.

## Goals / Non-Goals

**Goals:**
- **Automatic Ambient Ownership**: Components, bindings, and Solim controls instantiated during `build()` automatically register with the current component's lifecycle without calling `own()` or `ownChild()`.
- **Direct Element Property Bindings**: Provide concise reactive bindings for Arc elements (width, color, text, visibility, enabled state) without manual `Effect` definitions in user code.
- **Clean Separation of Reactive Graph and Arc Layout**: Remove all reactive `.get()` invocations from Arc lifecycle overrides (`getPrefWidth`, `getPrefHeight`, `layout`, `draw`, `act`). Reactive subscriptions push property changes to Arc elements and trigger normal layout invalidation.
- **Fine-Grained FeatureCard Reactivity**: Pass a reactive `Readable<Boolean>` enabled state to `FeatureCard` so background color, status text, and status color update in place without rebuilding or recreating cards.
- **Decoupled Reactivity Concerns**: Separate structural list changes (`filteredFeatures` driving `ReactiveGrid`) from property changes (feature state toggles driving existing card bindings).
- **Persistent View in Thin Dialog Host**: `FeatureSettingDialog` instantiates `FeatureSettingsView` once, preserves it across show/hide cycles, and disposes it only when the dialog itself is disposed.
- **Declarative DSL Adoption**: Refactor `FeatureSettingsView` and `FeatureCard` to construct their hierarchy through Solim's declarative builders (`column`, `row`, `scroll`, `reactiveGrid`, `textField`).
- **Declarative Grid Empty State**: Provide `.empty(Runnable)` on `ReactiveGrid` allowing inline declarative UI rather than requiring an anonymous `BaseComponent`.
- **Delete Legacy Imperative API**: Remove `FeatureCard.build(Table, Feature, float, Runnable)`.

**Non-Goals:**
- Do NOT introduce a Virtual DOM or global tree diffing algorithm.
- Do NOT rewrite or redesign the core `Signal` / `Computed` / `Effect` reactive primitives.
- Do NOT replace Arc's underlying retained-mode scene graph (`Element`, `Table`, `Button`, `Label`).
- Do NOT modify unrelated features or dialogs outside the Solim settings dialog and card components.

## Decisions

### 1. Ambient Component Build Context for Automatic Lifecycle Ownership
- **Decision**: Introduce an ambient `ComponentContext` (using a thread-safe stack pattern similar to `ParentStack`) active while `BaseComponent.build()` is executing.
- **Mechanism**:
  - In `BaseComponent.element()`, push `this` onto `ComponentContext.currentStack()` before `build()`, and pop it in a `finally` block.
  - When another `Component`, `SolimTextField`, or reactive binding is instantiated while a component is on top of `ComponentContext`, it automatically registers as an owned disposable of the ambient component.
  - `own()` and `ownChild()` are retained internally for low-level framework hooks if needed, but removed completely from application-level components (`FeatureCard`, `FeatureSettingsView`, `FeatureSettingDialog`).
- **Alternative considered**: Requiring a `BuildContext` parameter in `build(BuildContext ctx)`. Rejected because it increases boilerplate in declarative DSL and diverges from the clean zero-argument `build()` target.

### 2. Direct Property Bindings vs Manual Effects
- **Decision**: Introduce explicit property binding helpers (e.g. `Binding.bindWidth(element, readable)`, `Binding.bindColor(element, readable)`, `Binding.bindText(label, readable)`) and fluent methods on Solim wrappers.
- **Mechanism**:
  - `Binding.bindWidth(Element el, Readable<Float> width)` subscribes to `width` and updates `el.setWidth(val)` followed by `el.invalidateHierarchy()`.
  - When called inside a component build, the subscription is automatically owned.
  - The developer writes:
    ```java
    Binding.bindWidth(card, cardWidth.map(w -> w - 10f));
    Binding.bindColor(card, enabled.map(v -> v ? Color.green : Color.scarlet));
    ```
    or uses declarative builders that accept `Readable<T>`.
- **Alternative considered**: Keeping `Effect.of(...)` everywhere. Rejected because `Effect` is meant for side effects (logging, analytics, external synchronization), not basic UI property reflection.

### 3. Removal of Reactive Reads in Arc Layout Lifecycle
- **Decision**: Eliminate `@Override public float getPrefWidth() { return cardWidth.get() - 10f; }` from `Button` and all Arc element subclasses.
- **Mechanism**:
  - The card button is a standard Arc `Button` with a fixed pref height (180f) and standard sizing.
  - `cardWidth` updates the element's width via reactive binding and calls `invalidate()` / `invalidateHierarchy()`, letting Arc execute normal layout on plain values.
  - This prevents Arc layout passes from accidentally becoming dependencies of reactive computations.

### 4. Per-Feature Reactive State Adapter
- **Decision**: Maintain reactive enabled state for each feature in `FeatureSettingsView` so toggling a feature only updates that specific card's bindings.
- **Mechanism**:
  - Maintain a cache of `Signal<Boolean>` per feature in `FeatureSettingsView` (or an adapter function `getFeatureEnabledSignal(feature)`).
  - When `feature.setEnabled(!feature.isEnabled())` is called or a `FeatureStateChanged` event is received, only the specific feature's `Signal<Boolean>` is updated.
  - `revision` is retained strictly for structural feature list modifications (e.g., re-enabling all features or feature additions/removals) to update `filteredFeatures`.
  - `FeatureCard` receives `Readable<Boolean> enabled` and wires its background color, status label text, and status label color directly to `enabled`.

### 5. Persistent View in `FeatureSettingDialog`
- **Decision**: Create `FeatureSettingsView` once during `FeatureSettingDialog` initialization and reuse it across multiple `show()` and `hide()` actions.
- **Mechanism**:
  - In `FeatureSettingDialog`:
    ```java
    public FeatureSettingDialog() {
        super(...);
        view = new FeatureSettingsView();
        cont.add(view.element()).grow();
        shown(view::onShown);
        resized(() -> view.updateWidth(calcWidth()));
    }
    ```
  - Remove `hidden(() -> view.dispose())`.
  - In `FeatureSettingDialog.dispose()`, call `view.dispose()` and `super.dispose()`.

### 6. Declarative Grid Empty View
- **Decision**: Enhance `ReactiveGrid` with `.empty(Runnable builder)`.
- **Mechanism**:
  - In `ReactiveGrid`:
    ```java
    public ReactiveGrid<T, K> empty(Runnable emptyContentBuilder)
    ```
  - When the collection is empty, `emptyContentBuilder` executes inside a nested table, automatically populating the empty state without requiring an anonymous `BaseComponent`.

## Risks / Trade-offs

- **[Risk] Thread-local or ambient stack leak if build throws**:
  - *Mitigation*: Ensure `BaseComponent.element()` pushes the ambient component context in a `try ... finally` block, guaranteeing the stack is always popped even on exception.
- **[Risk] Reusing FeatureSettingsView across show/hide might retain stale filter or stale width**:
  - *Mitigation*: `shown(view::onShown)` updates the view width and triggers `refresh()`, ensuring proper layout and up-to-date data upon being reopened.
- **[Risk] Java 8 runtime compatibility requirement**:
  - *Mitigation*: Strictly avoid Java 9+ APIs (`List.of`, `Map.of`, `Stream.toList`, etc.) in all new and modified code. Use Java 8 compatible collections and Arc types.
