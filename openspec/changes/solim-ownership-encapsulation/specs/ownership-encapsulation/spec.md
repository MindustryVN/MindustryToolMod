## ADDED Requirements

### Requirement: Internal-only ownership primitive
The framework SHALL confine the raw ownership primitive `BaseComponent.own()` to package `solim.core` (no access modifier) so it is callable only by framework internals and rejected by the compiler in application modules.

#### Scenario: Mod code calling own() fails to compile
- **WHEN** application code outside `solim.core` calls `own(disposable)`
- **THEN** compilation fails with an access error (`own() has package access in BaseComponent`)

#### Scenario: Framework internals register via ComponentContext
- **WHEN** framework code outside `solim.core` (e.g. layouts, structural components) needs to own a resource
- **THEN** it calls `ComponentContext.register(disposable)` or `ComponentContext.registerChild(child)`, which delegates to the owning component

#### Scenario: Mod lifecycle needs are covered without own()
- **WHEN** mod code needs event unregistration or reactive effects tied to component disposal
- **THEN** it uses instance `listen()`, `createSignal()`, or `effect()` declared inside `build()`, which are auto-owned without any manual call

### Requirement: No manual ownership API on the mod path
Application components SHALL have no callable manual ownership method. `ComponentContext.register()/registerChild()` is the canonical internal path; `listen()`/`createSignal()`/`effect()` are the canonical mod-facing lifecycle-safe paths.

#### Scenario: Guard test rejects manual ownership references
- **WHEN** the ownership guard test scans `:mod` sources
- **THEN** zero references to `own(` or `ownChild(` exist outside comments, and the test fails otherwise
