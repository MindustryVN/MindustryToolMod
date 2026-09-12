# Proposal: Solim Runtime Encapsulation & Ownership Hiding (Schema 1)

## Why

1. AI agents and mod developers can currently access and accidentally bypass internal framework plumbing (`ParentStack`, `ComponentContext`, `ReactiveContext`, `SignalDispatcher`, and `BaseComponent.own()`) because `:solim` transitively exports all of `:solim-core` via `api project(':solim-core')`.
2. Mod code should only see declarative public APIs (`solim.UI.*`, `BaseComponent`, `Signal`, `Column`, `Button`, etc.). Internal engine mechanics must be completely inaccessible to `:mod`—resulting in hard `javac` compile errors and IntelliJ IDEA red unresolved symbols/autocomplete suppression.
3. To prevent circular dependencies between public components and internal engine classes while preserving automatic UI attachment (`new MyComponent()` in `children(...)`) and lifecycle auto-ownership, Solim is restructured using **Schema 1 (Modern Reactive / Jetpack Compose Style)**.

## What Changes

- **Create `:solim-api` subproject**: Houses pure base contracts (`Component`, `Disposable`, `Readable`, `EventsUtil`) with zero dependencies on other Solim modules.
- **Create `:solim-runtime` subproject**: Houses internal engine plumbing (`ParentStack`, `ComponentContext`, `ReactiveContext`, `SignalDispatcher`, `StructuralReconciler`, `ElementResolver`, `Binding`, `Ui`) under package `solim.runtime.*`. Depends only on `:solim-api`.
- **Decouple `ParentStack` from widgets**: Remove leaky hardcoded `instanceof Text` and `instanceof SolimImage` checks from `ParentStack` to ensure a strict DAG.
- **Configure `:solim-core` & `:solim` with `implementation project(':solim-runtime')`**: `:solim-runtime` is an `implementation` (non-transitive) dependency, strictly hiding it from `:mod`'s `compileClasspath`.
- **Make `BaseComponent.own()` package-private**: Subclasses in `:mod` cannot call `this.own(...)` at the Java language level.
- **Reroute internal cross-package caller**: Reroute `ReactiveGrid.gap()` from `own(...)` to `ComponentContext.register(...)`.
- **Migrate `:mod` manual `own()` call sites**: Migrate the 3 remaining call sites (`BrowserSearchHeader`, `TeamResourceHudView`, `ChatOverlayHudView`) to declarative `effect()` / `listen()`.
- **Update documentation (`AGENTS.md`)**: Document the modular architecture, forbidding direct access to `:solim-runtime`.

## Capabilities

### New Capabilities
- `solim-runtime-encapsulation`: Internal engine classes (`ParentStack`, `ComponentContext`, `ReactiveContext`, `SignalDispatcher`) reside in `:solim-runtime` and are physically excluded from `:mod`'s compile classpath. Referencing them in `:mod` fails compilation (`package solim.runtime does not exist`).
- `ownership-encapsulation`: `BaseComponent.own()` is internal-only (package-private). Mod code has no manual ownership API and relies exclusively on ambient ownership (`listen()`, `effect()`, `createSignal()`).

### Modified Capabilities
- `unified-ownership-api`: `own()` is restricted to framework internals; public contract keeps `own(null)` safety and LIFO disposal semantics for framework callers.
- `solim-automatic-ownership`: Ambient ownership is the sole path for application code; manual ownership in application code is a compile error.

## Impact

- **Affected Subprojects**: `settings.gradle`, `solim-api/` (new), `solim-runtime/` (new), `solim-core/`, `solim/`, `solim-mcp/`, `mod/`.
- **APIs**:
  - `ParentStack`, `ComponentContext`, `ReactiveContext`, `SignalDispatcher` moved to `solim.runtime.*` in `:solim-runtime` (internal to framework; inaccessible to `:mod`).
  - `Component`, `Disposable` moved to `solim.api.*` (or kept aliased/re-exported).
  - `BaseComponent.own()` changed from `public` to package-private.
- **Backwards Compatibility**:
  - Existing declarative mod code (`UI.column()`, `UI.signal()`, `class MyView extends BaseComponent`) is 100% compatible.
  - Auto-attach (`new MyView()` inside `children(...)`) and auto-ownership inside `build()` work unchanged.
