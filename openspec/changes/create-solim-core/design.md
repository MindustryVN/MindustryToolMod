## Context

`solim` module (`solim/src/solim/Solim.java`) is a placeholder with no UI code; root `build.gradle` already configures `subprojects` with `src`/`src/test/java` sourceSets, JUnit 5, and `compileOnly` Mindustry `v159.7` (Arc + Scene2D). All mod UI today uses imperative `Table`/`Element`/`Dialog` directly, with no shared reactive layer. `requirement.md` (1169 lines) defines full Solim spec: reactive system (Signal/Computed/Effect/ReactiveContext), component model (Component/BaseComponent), implicit parent stack, bindings, styling, layouts (Column/Row/Stack/Grid/Wrap/Scroll etc.), and widgets (Display/Input/Overlay/Feedback) with strict constraints (no Virtual DOM/diffing/CSS/React hooks, thin Arc wrappers, direct Element mutation, `try/finally` stack guarantee).

Stakeholders: mod developers (declarative API consumers), `solim` library users, translators (`assets/bundles/bundle.properties` if widgets add `solim.*` keys), test suite (`gradle :solim:test`). Mindustry runs single-threaded UI loop; Arc `Element` tree is the only render tree. Implementation must follow prescribed order: Signal → Computed → Effect → Reactive tracking → Subscription/disposal → Parent stack → Element resolution → Bindings → Row/Column → Text → Button → Style binding → remaining.

## Goals / Non-Goals

**Goals:**
- Implement Solim core architecture per `requirement.md` in `solim` with foundation-first order and proper tests for reactive dependency tracking, dynamic deps, disposal, and effect cleanup.
- Keep reactive core pure-Java (no Arc dependency) for fast JUnit tests; delegate layouts/widgets thinly to Arc where possible.
- Provide declarative `column(() -> { text(...); row(() -> {...}) })` API via implicit parent stack with guaranteed cleanup.
- Support reactive bindings (`Signal`/`Computed` → `Element` property) via direct mutation, not rebuild, with disposable lifecycle tied to `Component.dispose()`.
- Deliver lightweight immutable `Style` system with full re-apply on change.

**Non-Goals:**
- Virtual DOM, diffing, reconciliation, renderer, JSX, React hooks, or CSS engine/parser/flexbox replication — explicitly forbidden.
- Perfect CSS Grid/Flexbox parity; only app-oriented subsets (`Justify`/`Align` enums, `gap`, `growX`, `minCellWidth` later).
- `SplitPane` if Arc lacks primitives — implement only if `arc.scene.ui.SplitPane` available.
- Advanced input controls (rich text, date picker) until core architecture stable.
- `ThreadLocal` for reactive context unless single-threaded stack proves insufficient.
- Immediate migration of all existing mod UI to Solim (incremental adoption).

## Decisions

**1. Reactive core as pure-Java stack-based observer (no ThreadLocal).**
- Why: Mindustry UI runs on single game thread; a `static Deque<ReactiveObserver>` stack is deterministic, cheap, and testable. `ReactiveContext` holds `Deque<ReactiveObserver> stack`; `Computed` and `Effect` implement `ReactiveObserver { Set<Dependency> deps; void addDependency(...); void invalidate(); }`. `Signal.get()`/`Computed.get()` call `ReactiveContext.track(this)` if `current() != null`. Alternative `ThreadLocal` adds overhead and obscures single-thread assumption. Verification: no `ThreadLocal` import in `ReactiveContext.java`.
- Structure:
  ```
  solim.signal/
    Signal<T> { T get(); void set(T); Subscription subscribe(Consumer<T>); Computed<R> map(Function<T,R>); }
    Computed<T> implements ReactiveObserver { T get(); void dispose(); Subscription subscribe(...); }
    Effect implements ReactiveObserver { static Effect of(Supplier|Consumer<Cleanup>); void dispose(); }
    ReactiveObserver interface { void addDependency(Dependency d); }
    ReactiveContext { static void push/pop/current/track }
    Subscription { void dispose(); } + Disposable marker
  ```
- Equality: `Objects.equals(old, new)` guard before notify. Dependency cleanup: on re-evaluate, `oldDeps - newDeps` unsubscribe, `newDeps - oldDeps` subscribe. Lazy: `Computed` caches `value` + `dirty` flag; `invalidate()` sets dirty and propagates to dependents. Cycle: maintain `currentlyComputing` Set or `computing` flag; throw `IllegalStateException("Cycle detected")` if `get()` re-enters same `Computed`.

**2. Subscription/Disposable unified as functional interface.**
- Why: Keep API small. `Subscription extends Disposable { void dispose(); }` returned from `subscribe`. `Effect.of(...)` returns `Effect` which implements `Disposable`. `Computed` holds `Set<Subscription>` to dependencies and `Set<Consumer<T>>` listeners. Dispose is idempotent via `AtomicBoolean` or `boolean disposed` guard. Alternative separate `AutoCloseable` complicates lambda usage; unified is simpler for `cleanup.add(subscription::dispose)`.
- Cleanup API choice: support both `Effect.of(() -> { Subscription s=...; return s::dispose; })` (return `Runnable`/`Disposable`) and `Effect.of(cleanup -> { cleanup.add(s::dispose); })` via overloaded factory that inspects functional interface arity. Keep overloads explicit: `of(Runnable)`, `of(Supplier<Runnable>)`, `of(Consumer<Cleanup>)` where `Cleanup { void add(Runnable); }`.

**3. Component model: interface + BaseComponent lazy cache.**
- Why: `Component { Element element(); default void dispose(){} }` is minimal contract. `BaseComponent` adds lazy single-build: `private Element cached; final Element element() { if (cached==null) cached=build(); return cached; } protected abstract Element build();`. Alternative eager build in constructor breaks parent-stack nesting (child not yet attached). Lazy ensures `build()` runs inside parent stack lambda scope if `new MyComponent()` is added via `add(component)`. Keep `dispose()` explicit — no scopes/owners — components dispose own `Effect`/`Subscription` fields. Verification: build count test asserts single invocation.

**4. ParentStack as static ArrayDeque with lambda scopes.**
- Why: Direct mapping to spec's `push Column → add Text → push Row → ... → pop`. Implementation:
  ```java
  public final class ParentStack {
    private static final Deque<Group> stack = new ArrayDeque<>();
    public static <T extends Element> T column(Runnable c) { Table t=new Table(); push(t); try{c.run();}finally{pop();} attachToParent(t); return t; }
    // similar for row/stack/grid/wrap/scroll
    private static void attachToParent(Element el) { if(!stack.isEmpty()) stack.peek().addChild(el); }
  }
  public final class Ui {
    public static Table column(Runnable r) { return ParentStack.column(r); }
    public static Element text(String s) { Label l=new Label(s); ParentStack.add(l); return l; }
    // reactive overloads: text(Signal<String> s) -> Effect binding
  }
  ```
  Alternative `start/end` methods rejected per requirement — `try/finally` guarantee is critical. Element vs Group: use `arc.scene.Group` or `Table` as parent type; `ElementResolver.resolve(Object)` handles `Component → element()`.
- Children overloads: `Ui.add(Component|Element|Object)` delegates to `ElementResolver`. `Ui.text(...)` itself calls `ParentStack.current().addActor(label)` internally.

**5. Binding via Effect for multi-deps, Subscription for single Signal.**
- Why: `Binding.of(Label::setText, Computed<String>)` is naturally `Effect.of(() -> label.setText(computed.get()))`. For `Signal<String>` single source, `Subscription` listening to signal is cheaper but `Effect` is uniform and handles `Computed` chains. Use `Effect` for all reactive widget props to minimize code paths. Each widget factory creating reactive binding stores `Effect` in a `Binding` holder or returns widget with attached effect reference via `UserData` or wrapper object (`BoundWidget<T extends Element>` with `dispose()`). For `TextField` two-way, need listener on Arc `ChangeListener` to update `Signal` plus `Effect` from `Signal` to `TextField` — guard against feedback loop with equality check and `isUpdating` flag.
- Disposal: widget's binding `Effect` is stored in `BoundWidget` or in caller's `Component` field; `Component.dispose()` disposes. For ephemeral widgets outside components, binding lifetime is parent `Component` lifetime.

**6. Styling as immutable value + full re-apply.**
- Why: Keep simple, avoid diffing. `Style` is immutable data class with `Drawable background`, `Color fontColor`, `int pad`, etc., plus `Type` enum (button/label/field) or generic. `Styles.PRIMARY = new Style(...)` constants. `widget.style(Style s)` calls `apply(widget.element, s)` which sets all relevant Arc style fields (e.g., `TextButton.TextButtonStyle`). Reactive overload `widget.style(Computed<Style> s)` does `Effect.of(() -> apply(element, s.get()))` with immediate apply. Alternative CSS-like cascade rejected as overkill. Builder pattern `Style.builder().from(Styles.PRIMARY).pad(16).build()` supports composition without mutation.

**7. Layouts delegate to Arc Table/Stack/ScrollPane.**
- Why: Requirement says prefer Arc-native. `Column` is `Table` with `defaults().top().left().growX()` + `row()` between children; `Row` is `Table` with `defaults()` horizontal. `Stack` wraps `arc.scene.ui.Stack`. `Scroll` wraps `ScrollPane`. `Grid(columns)` is `Table` with manual `cell` column count and `row()` after `columns` children. `Gap` implemented via `table.defaults().pad*` or `cell.pad()`. `Justify` maps to `Table` alignment: `START→left`, `CENTER→center`, `END→right`, `BETWEEN→ distribute via expandX`, etc. — approximate, not pixel-perfect flex. `Align` maps to `top/center/bottom` vs `growY`. `growX` maps to `cell.growX()`. Keep modifiers fluent returning `Table` via wrapper or extending `Table` with self-type.

**8. Package structure matching requirement §14.**
- Why: Prescribed structure improves discoverability; keep `signal`, `core`, `ui`, `layout`, `display`, `input`, `overlay`, `feedback`, `style` packages. Root `solim` may re-export `Ui` facade for ergonomic `import static solim.ui.Ui.*`. Implementation order enforced via Gradle module internal dependencies (e.g., `display.Text` depends on `signal` + `ui` but not vice versa).

**9. Testing strategy: pure-Java reactive tests + headless Arc for UI.**
- Why: Requirement mandates tests for dependency tracking, dynamic deps, Computed disposal, Effect disposal/cleanup, disposed effects no longer react. Reactive core has zero Arc deps — test with JUnit 5 in `solim/src/test/java/solim/signal/`. UI/layout bindings need Arc `Application` headless — use `arc.ApplicationCore` or mock `Element`/`Label` with Mockito or minimal stub that records `setText` calls. Prefer pure-Java stubs over full Mindustry launch for speed. Test matrix:
  - `SignalTest`: equality, subscribe/dispose, map
  - `ComputedTest`: lazy, dynamic deps, cycle, propagation, dispose, map chain
  - `EffectTest`: auto-track, dynamic branch, cleanup before re-run, cleanup on dispose, disposal stops, error logging, no infinite loop
  - `ReactiveContextTest`: stack push/pop, nested
  - `ParentStackTest`: try/finally, auto-attach, resolve
  - `BindingTest`: immediate apply, future updates, no rebuild, dispose
  - `ComponentTest`: build once, no re-render, Component child resolution
  - `LayoutTest`/`WidgetTest`: gap/justify/align/grow, spacer, grid (headless or mocked Arc)
- Alternative: full `HeadlessApplication` via `arc.backend.headless.HeadlessApplication` if available in `dependencies.jar` — verify at implementation time.

**10. i18n and HTTP notes.**
- Why: `AGENTS.md` requires all user-visible text via `Core.bundle.get/format` and `assets/bundles/bundle.properties` with per-key comments. Solim widgets like `Alert`/`Dialog` titles may expose `solim.*` keys; add with comments. No HTTP in Solim — `mindustrytool.services.Request` rule irrelevant here, but ensure no direct `HttpClient` usage in `solim`.

## Risks / Trade-offs

- **Signal/Computed lazy + propagation correctness** → Mitigation: thorough graph tests; use topological invalidation via `dirty` flag plus `version` counter; keep dependency sets as `LinkedHashSet` for determinism.
- **ParentStack static state leaks across tests** → Mitigation: `ParentStack.clear()` for tests, ensure `try/finally` always pops; add `@AfterEach` cleanup in tests.
- **Arc headless testing fragility (no Application)** → Mitigation: keep reactive core pure-Java and test UI via mocked `Element` recording setters; gate Arc-dependent tests with `Assumptions.assumeTrue(arc.Core.app != null)` or use `HeadlessApplication` stub if available.
- **Style full re-apply may flicker or lose Arc state (e.g., pressed-over)** → Mitigation: apply only stable fields; document that custom `TextButtonStyle` subclasses should be re-created per style change; test that existing `ClickListener` survives style re-apply.
- **Layout Justify/Align approximation not pixel-perfect CSS** → Mitigation: document as app-oriented subset; map to Arc `Table` best-effort and note limitations in design doc and widget javadoc.
- **TextField two-way binding feedback loop** → Mitigation: equality guard + `isUpdating` boolean to prevent echo; test that programmatic `signal.set(...)` does not trigger `ChangeListener` loop.
- **SplitPane conditional on Arc availability** → Mitigation: feature-detect at build time; if `arc.scene.ui.SplitPane` missing, provide no-op stub and document.

## Migration Plan

1. Scaffold `solim.*` packages under `solim/src/solim/` and create empty `Ui`, `ParentStack`, `ElementResolver`, `Binding`, `ReactiveContext` classes.
2. Implement reactive core in order: `Subscription`/`Disposable` → `Signal` → `ReactiveContext`/`ReactiveObserver` → `Computed` → `Effect` with cleanup overloads; add `solim/src/test/java/solim/signal/*Test.java` per step and verify `gradle :solim:test` green.
3. Implement `Component`/`BaseComponent` + `ElementResolver` + `ParentStack` + `Ui` facades; add `ParentStackTest`/`ComponentTest`.
4. Implement `Binding` helper + reactive widget modifiers (`visible`, `enabled`) and `Style`/`Styles`; add `BindingTest`/`StyleTest`.
5. Implement layouts `Column`/`Row`/`Spacer`/`Divider` → `Grid`/`Wrap`/`Stack`/`Scroll`/`Container`; add `LayoutTest` (mocked Arc).
6. Implement core widgets in priority order: `Text` → `Button`/`IconButton` → `TextField`/`TextArea` → `Checkbox`/`Switch` → `Slider`/`Select` → `Image`/`Icon` → `Badge`/`Avatar` → `Dialog`/`Popup` → `Spinner`/`ProgressBar`/`Alert`; each with widget test covering static + reactive cases.
7. Add `solim.*` bundle keys with per-key comments if widgets expose user-facing strings; run `grep -R "Core.bundle" solim/src` to ensure no hardcoded text.
8. Update `solim/README` or `docs/` with example `SettingsPanel` from `requirement.md §16` as integration test / demo component.
9. Verify: `gradle :solim:test` full suite passes, `gradle build` succeeds for `:mod`, no `ThreadLocal` in `ReactiveContext`, no hardcoded bundle text, no `HttpClient` usage outside `Request.java`.

**Rollback:** Delete `solim/src/solim/` packages and revert `solim/build.gradle`/`bundle.properties` additions; reactive core is isolated, no impact on `mod` or existing `mindustrytool.services`.

## Open Questions

- Should `grid().columns(n)` be builder-style returning `Grid` self-type vs static `grid(3, Runnable)`? Support both per requirement but confirm API symmetry with `column`/`row`.
- Should `Binding` be exposed as public API or internal only via widget overloads? Lean internal + `Effect` for advanced users.
- Exact `Style` fields: does `Style` wrap Arc drawable per widget type or generic `background` + `pad`? Defer to spike with `TextButtonStyle`/`LabelStyle` mapping.
- Headless Arc test harness availability in `Anuken.Mindustry:v159.7` dependencies.jar — needs spike to confirm `HeadlessApplication` can be instantiated in JUnit without `Vars`.
- `solim` package prefix already `solim.*`; confirm import ergonomics `import static solim.ui.Ui.*` works without extra nesting.

