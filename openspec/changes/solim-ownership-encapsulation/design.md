# Design: Solim Runtime Encapsulation & Ownership Hiding (Schema 1)

## Context

`BaseComponent.own()` is currently `public` in `solim.core`, and internal engine classes (`solim.ui.ParentStack`, `solim.core.ComponentContext`, `solim.signal.ReactiveContext`, `solim.signal.SignalDispatcher`) are `public` and exported to `:mod` because `solim/build.gradle` uses `api project(':solim-core')`.

Consequently:
1. AI agents and mod developers can directly call `ParentStack` and `ComponentContext`, bypassing declarative Solim abstractions.
2. Mod developers can invoke `this.own(...)` in `BaseComponent` subclasses, introducing manual lifecycle bookkeeping and disposal bugs.

The user chose **Schema 1 (Modern Reactive / Jetpack Compose Style)** to physically enforce API boundaries at compile-time and in IDEs while preserving automatic UI mounting (`new MyComponent()` inside `children(...)`) and lifecycle auto-ownership without circular dependencies.

## Goals / Non-Goals

**Goals:**
- **Physical compile-time inaccessibility**: Any reference in `:mod` to internal classes (`ParentStack`, `ComponentContext`, `ReactiveContext`, `SignalDispatcher`, etc.) must fail `javac` compilation (`package solim.runtime does not exist` or `cannot find symbol`).
- **First-class IDE support**: In IntelliJ IDEA, internal engine classes must be marked in red as unresolved symbols, excluded from autocomplete, and disallowed from auto-import in `:mod`.
- **Method-level encapsulation**: `BaseComponent.own()` becomes package-private; calling `this.own(...)` in any `:mod` component fails `javac` compilation and shows red in IntelliJ.
- **Strict Directed Acyclic Graph (DAG)**: Zero circular dependencies between Gradle subprojects.
- **Preserve automatic features**:
  - UI auto-attach (`new MyView()` inside `children(...)` automatically attaches to the enclosing `Table`) remains 100% functional.
  - Ambient lifecycle ownership (`listen()`, `createSignal()`, `effect()`, child component disposal) remains 100% functional.

**Non-Goals:**
- No change to the public declarative DSL in `solim.UI`.
- No runtime sandboxing or classloader isolation (all jars package into the single fat mod jar for Mindustry runtime).
- No removal of `solim-mcp` introspection capabilities (MCP depends on `:solim-runtime` and `:solim-core`).

## Architecture: Schema 1 (Jetpack Compose Style)

### Subproject Breakdown

```text
               :mod (App)
                 │ (implementation)
                 ▼
            :solim (Facade)
            └── solim.UI
                 │ (api)
                 ▼
          :solim-core (Public Framework API)
                 │
         ┌───────┴────────────────────────┐
         │ (implementation)               │ (api)
         ▼                                ▼
   :solim-runtime (Internal Engine)   :solim-api (Contracts)
   - ParentStack                      - Component
   - ComponentContext                 - Disposable
   - ReactiveContext                  - Readable
   - SignalDispatcher                 - EventsUtil
   - StructuralReconciler                 ▲
   - ElementResolver                      │
   - Binding                              │ (implementation)
         │                                │
         └────────────────────────────────┘
```

### Module Responsibilities & Class Distribution

1. **`:solim-api`** (Base Contracts)
   - `solim.api.Component` (interface)
   - `solim.api.Disposable` (interface)
   - `solim.api.Readable` (interface)
   - `solim.api.EventsUtil`
   - *Dependencies*: Only Arc / Mindustry (no Solim dependencies).

2. **`:solim-runtime`** (Internal Engine — Hidden from `:mod`)
   - Package: `solim.runtime.*`
   - `solim.runtime.stack.ParentStack`
   - `solim.runtime.context.ComponentContext`
   - `solim.runtime.signal.ReactiveContext`
   - `solim.runtime.signal.SignalDispatcher`
   - `solim.runtime.signal.ReactiveObserver`
   - `solim.runtime.reconciler.StructuralReconciler`
   - `solim.runtime.binding.Binding`
   - `solim.runtime.util.ElementResolver`
   - `solim.runtime.util.Ui`
   - *Dependencies*: `implementation project(':solim-api')`, Arc.

3. **`:solim-core`** (Public Components & Reactive Primitives)
   - `solim.core.BaseComponent` (abstract class, `own()` is package-private)
   - `solim.signal.Signal`, `Computed`, `Effect`, `Subscription`, `Signals`
   - `solim.layout.*` (`Column`, `Row`, `Grid`, `Card`, `Scroll`, `Tabs`, `ReactiveGrid`, `Spacer`, `Divider`, `SolimStack`, `Container`, `Wrap`, `VirtualList`, etc.)
   - `solim.input.*` (`Button`, `Checkbox`, `SolimTextField`, `SolimSlider`, `Switch`, `SolimSelect`, `TwoWayBinding`)
   - `solim.display.*` (`Text`, `SolimImage`, `NetworkImage`, `Badge`, `Icon`)
   - `solim.feedback.*` (`Alert`, `Avatar`, `ProgressBar`, `Spinner`)
   - `solim.overlay.*` (`SolimDialog`, `Hud`, `Popup`)
   - `solim.config.*` (`ConfigGroup`, `ConfigValue`, `ConfigPersister`)
   - `solim.graphics.*` (`RoundedDrawable`, `CircleDrawable`, `ColoredDrawable`, `Drawables`)
   - *Dependencies*: `api project(':solim-api')`, `implementation project(':solim-runtime')`.

4. **`:solim`** (Public DSL Facade)
   - `solim.UI`
   - *Dependencies*: `api project(':solim-core')`, `implementation project(':solim-runtime')`.

5. **`:mod`** (Mindustry Mod Application)
   - *Dependencies*: `implementation project(':solim')`, `implementation project(':solim-mcp')`.
   - Compile Classpath: Has `:solim`, `:solim-core`, `:solim-api`.
   - **Excludes**: `:solim-runtime`!

## Key Decisions

### D1: How Gradle `implementation` Enforces Compile & IDE Inaccessibility
In Gradle's `java-library` plugin:
- `api` dependencies are transitively exposed to downstream consumers on `compileClasspath`.
- `implementation` dependencies are **strictly excluded** from downstream consumers' `compileClasspath`.
Because `:solim-core` and `:solim` depend on `:solim-runtime` via `implementation`:
- `:mod`'s `compileClasspath` physically does not contain `:solim-runtime`.
- In `javac`: Any import of `solim.runtime.*` fails immediately with `package solim.runtime does not exist`.
- In IntelliJ IDEA: IntelliJ maps `implementation` dependencies to `Runtime` scope in downstream consumers. Classes in `:solim-runtime` are shown in red (unresolved symbol) and excluded from autocomplete.
- At runtime: Gradle packages `runtimeClasspath` (which includes `:solim-runtime`) into the mod jar.

### D2: Decoupling `ParentStack` to Eliminate Circular Dependencies
`ParentStack` in the current codebase contained two leaky couplings:
1. `if (comp instanceof Text) ((Text) comp).applySpacing();`
2. `solim.layout.SizeConstraints.find(child);`

To maintain a strict DAG where `:solim-runtime` only depends on `:solim-api`:
- Spacing is handled on element attachment or inside `Text`/`SolimImage` directly.
- `ParentStack` only holds `Map<Table, List<Component>>` using the pure `Component` interface from `:solim-api`.
- This ensures `:solim-runtime` never imports classes from `:solim-core`.

### D3: `BaseComponent.own()` Package-Private
`BaseComponent.own()` visibility changes from `public` to package-private.
- Internal callers in `solim.core` (like `listen()` and `createSignal()`) can still call `own()`.
- Framework callers outside `solim.core` (like `ReactiveGrid`) use `ComponentContext.register(...)`.
- Subclasses in `:mod` (`package mindustrytool.*`) cannot see or call `own()`, failing compilation.

### D4: Preserving UI Auto-Attach
`BaseComponent` constructor retains:
```java
ComponentContext.registerChild(this);
Table parent = ParentStack.current();
if (parent != null) {
    ParentStack.registerPendingComponent(this, parent);
}
```
Because `:solim-core` depends on `:solim-runtime`, `BaseComponent` has full access to `ParentStack` and `ComponentContext`. Calling `new MyCustomView()` inside `children(() -> ...)` auto-attaches to the parent `Table` exactly as before.

## Migration Plan

1. **Subproject Setup**:
   - Register `:solim-api` and `:solim-runtime` in `settings.gradle`.
   - Create `solim-api/build.gradle` and `solim-runtime/build.gradle`.
2. **Move Base Contracts to `:solim-api`**:
   - Move `Component.java`, `Disposable.java`, `Readable.java`, `EventsUtil.java`.
3. **Move Engine Plumbing to `:solim-runtime`**:
   - Move `ParentStack`, `ComponentContext`, `ReactiveContext`, `SignalDispatcher`, `StructuralReconciler`, `Binding`, `ElementResolver`, `Ui` to `solim.runtime.*`.
   - Clean up leaky `instanceof Text`/`SolimImage` from `ParentStack`.
4. **Update `:solim-core`**:
   - Configure dependencies: `api project(':solim-api')`, `implementation project(':solim-runtime')`.
   - Make `BaseComponent.own()` package-private.
   - Reroute `ReactiveGrid.gap()` to `ComponentContext.register()`.
5. **Update `:solim` & `:solim-mcp`**:
   - Update imports in `UI.java` to point to `solim.runtime.*`.
   - Update `:solim-mcp` dependencies to include `:solim-runtime`.
6. **Migrate `:mod`**:
   - Update 3 call sites in `:mod` (`BrowserSearchHeader`, `TeamResourceHudView`, `ChatOverlayHudView`) removing manual `own()` calls.
   - Update `mod/build.gradle` packaging to include `:solim-api` and `:solim-runtime` in runtime jar.
7. **Verify**:
   - Test that `:mod` compiling against `solim.runtime.ParentStack` fails compilation.
   - Run test suite across all subprojects.
