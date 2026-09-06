## Why

Mindustry mods currently build UIs directly with Anuken Arc / Mindustry Scene2D imperative APIs (`Table`, `Element`, `Dialog`), leading to duplicated layout code, manual state wiring, and no shared reactive primitive. `solim` exists as a placeholder UI library (`solim/src/solim/Solim.java`) with no implementation. A lightweight, reactive, Arc-native framework is needed that avoids Virtual DOM / diffing / reconciliation / React hooks / JSX and instead mutates `Element` properties directly via `Signal`/`Computed`/`Effect` bindings, while providing declarative layout (`column`/`row`), component composition, and testable reactive primitives.

## What Changes

- **BREAKING** Implement `solim` (library name `solim`, not `simpleui`) core architecture per `requirement.md` (16 sections) — new reactive system, component model, implicit parent stack, bindings, styling, layouts, and core widgets built on Arc `Element` tree only.
- Add reactive primitives: `Signal<T>` (equality-guarded, subscriptions), `Computed<T>` (lazy, dynamic deps, cycle-safe, `map`), `Effect` (auto-tracked, dynamic deps, cleanup `add` or returned disposable, error-safe), shared `ReactiveContext`/`ReactiveObserver` stack (no `ThreadLocal` unless necessary).
- Add component system: `Component` interface + `BaseComponent` (single `build()` call, no re-render, constructor props, explicit `dispose()`), child resolution `Element`/`Component → Element`, lifecycle `constructor → build → mounted → dispose`.
- Add implicit parent stack declarative API: `column(() -> {...})`, `row`, `stack`, `grid`, `wrap`, `scroll`, `container` with `try/finally` guaranteed cleanup, auto-attach children, spacer/divider support.
- Add reactive property binding + styling: static and reactive (`Signal`/`Computed`) values for `text`, `visible`, `enabled`, `style`; `Style` immutable objects applied entirely on change without CSS/diffing; direct `Element` mutation via `Effect`/`Binding`.
- Add layout primitives: `Column`, `Row` (`gap`, `justify START/CENTER/END/BETWEEN/AROUND/EVENLY`, `align START/CENTER/END/STRETCH`), `growX`/`grow`, `Spacer`, `Grid(columns, gap, minCellWidth)`, `Wrap`, `Stack`, `Scroll`, `Container`, `Divider`, `SplitPane` (if Arc permits) — all delegated to Arc layout where possible.
- Add core widget set: Layout (above), Display (`Text`, `Image`, `Icon`, `Badge`, `Avatar`), Input (`Button`, `IconButton`, `TextField`, `TextArea`, `Checkbox`, `Switch`, `Slider`, `Select`), Overlay (`Dialog`, `Popup` via Arc), Feedback (`Spinner`, `ProgressBar`, `Alert`) — starting with `Text`, `Button`, `TextField`, `Checkbox`, `Switch`, `Slider`, `Select`.
- Package structure `solim.{core,signal,ui,layout,display,input,overlay,feedback,style}` (root `solim`, not `solim.simpleui` / `simpleui`) and enforce implementation order: Signal → Computed → Effect → Reactive tracking → Subscription/disposal → Parent stack → Element resolution → Bindings → Row/Column → Text → Button → Style binding → remaining widgets, with tests for dependency tracking, dynamic deps, disposal, and effect cleanup.

## Capabilities

### New Capabilities
- `solim-reactivity`: Reactive primitives `Signal`, `Computed`, `Effect`, `ReactiveContext`/`ReactiveObserver`, `Subscription`/`Disposable`, equality check, lazy recomputation, dynamic dependency cleanup, cycle detection, `map`, cleanup `add` API, error-safe logging.
- `solim-component`: `Component` interface and `BaseComponent` base class, single `build()` semantics, no re-render, props via constructor, `Component → Element` resolution, explicit `dispose()` for effects/subscriptions.
- `solim-declarative-ui`: Implicit parent stack (`ParentStack`, `Ui` facades `column`/`row`/`stack`/`grid`/`wrap`/`scroll`), `ElementResolver`, `try/finally` guarantee, auto-attach, `add(Component|Element)` resolution.
- `solim-binding`: Reactive property binding system (`Binding`) for widget props (`text`, `visible`, `enabled`, etc.) — immediate apply, subscribe, direct Arc `Element` mutation, disposable bindings.
- `solim-styling`: Lightweight immutable `Style`/`Styles` system with static and reactive (`Signal`/`Computed`) style binding, full style re-apply on change without CSS or diffing.
- `solim-layout`: Layout primitives `Column`, `Row`, `Stack`, `Grid`, `Wrap`, `Scroll`, `Container`, `Spacer`, `Divider`, `SplitPane` plus modifiers `gap`/`justify`/`align`/`growX`/`padding` mapped to Arc layout.
- `solim-widgets`: Core widget implementations — Display (`Text`, `Image`, `Icon`, `Badge`, `Avatar`), Input (`Button`, `IconButton`, `TextField`, `TextArea`, `Checkbox`, `Switch`, `Slider`, `Select`), Overlay (`Dialog`, `Popup`), Feedback (`Spinner`, `ProgressBar`, `Alert`) built on binding + layout + style foundations.

### Modified Capabilities
- None — `solim` is currently empty placeholder; existing specs (`http-client`, `mindustrytool-api`, etc.) unchanged.

## Impact

- Affected code: `solim/src/**` (new packages `solim.{core,signal,ui,layout,display,input,overlay,feedback,style}`), `solim/build.gradle` (test deps already via root `subprojects`, may add AssertJ/mockito if needed), `assets/bundles/bundle.properties` (add `solim.*` keys with per-key comments per `AGENTS.md` if widgets expose user-visible strings), no `src/mod` or `src/mindustrytool` breakage.
- APIs/Systems: Arc `arc.scene.Element`, `arc.scene.ui.layout.Table`, `arc.scene.style.Drawable`, `mindustry.ui.Styles`/`Dialog`, Mindustry `Vars.ui`; no new HTTP/network deps; `solim` remains `compileOnly` Mindustry dependency via `solim: v159.7`.
- Dependencies: No new runtime deps; tests use `org.junit.jupiter:junit-jupiter:5.10.2` already configured; may add `arc` headless harness or `org.mockito` for Element mocking if needed — otherwise pure Java unit tests for reactive core.
- Breaking: None for consumers yet (`solim` unused); future mod UI code will migrate to `solim` declarative API.
