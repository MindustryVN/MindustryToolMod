## 1. Scaffolding and Package Structure

- [x] 1.1 Create package `solim.{core,signal,ui,layout,display,input,overlay,feedback,style}` under `solim/src/solim/` and placeholder `Solim` facade
- [x] 1.2 Verify `solim/build.gradle` inherits root `subprojects` sourceSets/test config; add test-only deps if needed (no runtime deps)
- [x] 1.3 Spike Arc headless harness: check `arc.backend.headless.HeadlessApplication` or mock `Element` approach for UI tests and document chosen strategy in `design.md` open question resolution

## 2. Reactive Core — Subscription and ReactiveContext

- [x] 2.1 Implement `solim.core.Disposable` (`void dispose()`) and `solim.signal.Subscription extends Disposable` with idempotent guard
- [x] 2.2 Implement `solim.signal.ReactiveObserver` interface (`addDependency`, `invalidate`, `cleanup`) and `solim.signal.ReactiveContext` with `static Deque<ReactiveObserver> stack`, `push`/`pop`/`current`/`track` (no `ThreadLocal`)
- [x] 2.3 Add unit tests `ReactiveContextTest` (push/pop/current, nested evaluation, no ThreadLocal import via source check)

## 3. Signal Primitive

- [x] 3.1 Implement `solim.signal.Signal<T>` with `Signal.of(T)`, `get()` (tracks via `ReactiveContext.current()`), `set(T)` (equality-guarded via `Objects.equals`), `subscribe(Consumer<T>): Subscription`, `map(Function<T,R>): Computed<R>`
- [x] 3.2 Add tests `SignalTest`: equality-guarded notification, subscribe/dispose stops, map creates Computed, get tracks in Computed/Effect

## 4. Computed Primitive

- [x] 4.1 Implement `solim.signal.Computed<T>` with `Signal.computed(Supplier<T>)`, lazy recomputation (`dirty` flag + cached value), `get()` (recompute if dirty with dependency collection), automatic dependency tracking via `ReactiveContext`, dynamic dependency cleanup (unsubscribe old, subscribe new), invalidation propagation to dependents
- [x] 4.2 Implement cycle detection (computing flag / `currentlyComputing` set) and thread-safe error logging without infinite loop
- [x] 4.3 Implement `subscribe` + `dispose` (unsubscribe all deps, clear listeners, idempotent) and `Computed.map` chain
- [x] 4.4 Add tests `ComputedTest`: lazy recomputation only on get, dynamic deps cleanup (darkMode/username/email branch), cycle detection, invalidation propagation chain `a→b→c`, subscription and dispose, map reactive updates

## 5. Effect Primitive with Cleanup

- [x] 5.1 Implement `solim.signal.Effect` with `Effect.of(Runnable)`, `of(Supplier<Runnable|Disposable>)`, `of(Consumer<Cleanup>)`, `Cleanup { void add(Runnable); }`, auto-track `Signal`/`Computed` reads, subscribe to deps, re-run on change, remove old deps before collecting new, dynamic branches
- [x] 5.2 Implement effect `dispose()` (unsubscribe, run cleanups, idempotent), cleanup before re-run, cleanup errors logged not blocking, recursion guard
- [x] 5.3 Implement error handling (try/catch around supplier, `arc.util.Log.err` or `Log.info`) and ensure disposed effects no longer react
- [x] 5.4 Add tests `EffectTest`: auto-track signals/computeds, dynamic dependency branch switch (enabled/username/email), old deps removed before new, dispose stops reacting, cleanup runs before re-run and on dispose, cleanup failure not blocking others, errors logged safely, no leak after dispose

## 6. Component System

- [x] 6.1 Implement `solim.core.Component` interface (`Element element(); default void dispose(){}`)
- [x] 6.2 Implement `solim.core.BaseComponent` with lazy cached `build()` (single invocation), `final Element element()` caching, override `dispose()` default
- [x] 6.3 Add tests `ComponentTest`: build runs once, no automatic re-render on signal change, constructor props (UserCard example), Component field holding Signal/Computed/Effect, lifecycle `constructor→build→mounted→dispose` with effect disposal, no React hooks API via source check

## 7. Declarative UI — ParentStack and Child Resolution

- [x] 7.1 Implement `solim.ui.ParentStack` with `static Deque<Group/Table>` stack, `push`/`pop`/`current`/`clear`, `attachToParent(Element)`, `add(Element|Component)` resolver, `try/finally` guaranteed cleanup
- [x] 7.2 Implement `solim.ui.ElementResolver` (`resolve(Object): Element` handling `Component→element()` passthrough) and `solim.ui.Ui` facades `column(Runnable)`, `row(Runnable)`, `stack(Runnable)`, `grid(int, Runnable)`, `wrap(Runnable)`, `scroll(Runnable)`, `container(Runnable)` returning layout `Element` and chaining modifiers
- [x] 7.3 Ensure no `startColumn`/`endColumn` or `startComponent`/`endComponent` APIs exist
- [x] 7.4 Add tests `ParentStackTest`: push/pop with try/finally, auto-attach children (column→text→row nesting), exception cleanup (stack restored), add outside parent behavior, mixed Element/Component children, lambda returns layout Element with modifiers

## 8. Reactive Binding System

- [x] 8.1 Implement `solim.ui.Binding` helper (via `Effect.of(() -> target.set(prop.get()))` or `Subscription`), with `Binding.of(Consumer<T>, Signal|Computed<T>)` that applies immediately, subscribes, updates `Element` directly, and is disposable
- [x] 8.2 Wire widget reactive overloads `text(Signal|Computed)`, `button(...).visible(Signal)`, `.enabled(Signal)`, `.checked` to use `Binding`/`Effect` without rebuilding widget; ensure immediate apply and no Element recreation on change (identity check)
- [x] 8.3 Add tests `BindingTest`: static vs reactive text immediate apply, future updates, no Element recreation, dispose stops updates, visible/enabled bindings, Computed chain via dirty.map, Component dispose disposes bindings

## 9. Styling System

- [x] 9.1 Implement immutable `solim.style.Style` (fields: background drawable, colors, padding, etc., or wrapper over Arc style) with builder `Style.builder().from(...).pad(...).build()` and `withPad`/`withBackground` composition; provide `solim.style.Styles` constants `PRIMARY`, `GHOST`, `black6` etc.
- [x] 9.2 Implement `widget.style(Style)` static apply and `widget.style(Signal<Style>|Computed<Style>)` reactive binding via `Effect` with full re-apply on change (no diffing, no CSS parser)
- [x] 9.3 Add tests `StyleTest`: immutability (builder post-build not affecting instance), constants exist, static style apply, reactive style toggle darkMode.map, reactive dispose, no CSS engine via source check

## 10. Layout Primitives

- [x] 10.1 Implement `solim.layout.Column` and `Row` as `Table` wrappers with modifiers `gap(float)` `justify(Justify)` `align(Align)` `padding(float)` `grow` mapping to Arc `Table` alignment/pad/grow
- [x] 10.2 Implement enums `Justify {START,CENTER,END,BETWEEN,AROUND,EVENLY}` and `Align {START,CENTER,END,STRETCH}`
- [x] 10.3 Implement `growX`/`growY`/`grow` on cells and convenience `textField(input).growX()` / `cell(...).growX()` delegation to `Cell.growX()`
- [x] 10.4 Implement `solim.layout.Spacer`, `Divider`, `Container`, `Stack`, `Scroll`, `Wrap` (wrapping), `Grid` (`grid(int columns, Runnable)` + `grid().columns(n).gap(g).minCellWidth`) delegating to `Table`/`Stack`/`ScrollPane`; ensure `Spacer` consumes remaining space via `grow`
- [x] 10.5 Add tests `LayoutTest`: Row justify BETWEEN + align CENTER + gap, Column gap+padding, growX on TextField, cell wrapper grow, spacer between buttons, grid 3 columns wrapping, grid gap, wrap flow, stack overlay, scroll overflow, divider rendering, all layouts delegate to Arc (source check, mocked Element verification)

## 11. Core Widgets — Text, Image, Icon, Button

- [x] 11.1 Implement `solim.display.Text` (`text(String)` / `text(Signal|Computed<String>)` → `Label` + binding, style support)
- [x] 11.2 Implement `solim.display.Image` (`image(Drawable)`) and `Icon` (`icon(...)`) with static and reactive drawable overloads
- [x] 11.3 Implement `solim.input.Button` (`button(String|Signal|Computed, Runnable)`, modifiers `.enabled`, `.visible`, `.style`) and `IconButton` variant wrapping `TextButton`/`ImageButton` with ClickListener
- [x] 11.4 Add tests `TextTest`/`ButtonTest`: static text, reactive text via Computed, Image/Icon display, button click handler invokes runnable, button reactive text/style/enabled, disabled state, no Element recreation, thin wrapper via Binding/Effect delegation check

## 12. Input Widgets — TextField, TextArea, Checkbox, Switch, Slider, Select

- [x] 12.1 Implement `TextField`/`TextArea` two-way binding to `Signal<String>` (listener → signal, Effect signal → field with `isUpdating` guard and equality check to avoid feedback loop/cursor jump)
- [x] 12.2 Implement `Checkbox` (`checkbox(String, Signal<Boolean>)`), `Switch` (`switch(Signal<Boolean>)`), `Slider` (`slider(Signal<Float>, min, max, step)`), `Select<T>` (`select(Signal<T>, List<T>)`) with reactive value binding and change callbacks delegating to Arc widgets
- [x] 12.3 Add tests `InputWidgetsTest`: TextField two-way sync (type → signal, signal → field), TextArea multiline, Checkbox/Switch toggle binding, Slider drag + programmatic set, Select pick + programmatic set, feedback loop guard

## 13. Overlay and Feedback Widgets

- [x] 13.1 Implement `solim.overlay.Dialog` wrapping `arc.scene.ui.Dialog`/`BaseDialog` with `dialog(title, Runnable content)` + `show`/`hide` using `ParentStack` inside dialog container, plus `Popup` lightweight tooltip
- [x] 13.2 Implement `solim.feedback.Spinner`, `ProgressBar` (`progressBar(Signal<Float>)` 0..1), `Alert` (dismissible, types info/warning/error) reusing Binding
- [x] 13.3 Implement lightweight `Badge` and `Avatar` convenience composites (`Text`/`Image` + `Container`)
- [x] 13.4 Add tests `OverlayFeedbackTest`: Dialog show/hide with declarative content, ProgressBar reactive progress, Alert dismiss, Badge reactive count, widgets reuse Binding/ParentStack/Style (source check)

## 14. Integration, i18n, and Verification

- [x] 14.1 Implement `SettingsPanel` example from `requirement.md §16` (`darkMode`, `dirty`, `saveText`, `logger Effect`, `column→text→divider→row→button.style(map)→spacer→row.justify(END)`) as integration demo and manual verification
- [x] 14.2 Add bundle keys under `solim.*` (if widgets expose user-visible strings) to `assets/bundles/bundle.properties` with mandatory per-key comments directly above each key per `AGENTS.md`; verify via `grep "solim\."` and `Core.bundle.get/format("solim.*")` usage, no hardcoded display text in `solim/src`
- [x] 14.3 Ensure no Virtual DOM/diffing/reconciliation/React hooks/JSX/CSS parser in codebase (source checks) and no direct `HttpClient`/`HttpRequest` construction outside `mindustrytool.services.Request`
- [x] 14.4 Run `gradle :solim:test` (all reactive, component, parent-stack, binding, style, layout, widget tests) and `gradle build`; verify `ReactiveContext` has no `ThreadLocal`, parent stack `try/finally` coverage, and integration `SettingsPanel` renders without exception (headless or mocked)
