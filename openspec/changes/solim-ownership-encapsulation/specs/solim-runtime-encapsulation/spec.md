## ADDED Requirements

### Requirement: Physical compile-time isolation of Solim runtime
The framework SHALL place all internal engine classes (`ParentStack`, `ComponentContext`, `ReactiveContext`, `SignalDispatcher`, `StructuralReconciler`, `ElementResolver`, `Binding`, `Ui`) inside the `:solim-runtime` subproject under package `solim.runtime.*`. The `:solim-runtime` subproject SHALL NOT be on `:mod`'s compile classpath.

#### Scenario: Mod referencing ParentStack fails compilation
- **WHEN** application code in `:mod` imports or references `solim.runtime.ParentStack` or any class from `solim.runtime.*`
- **THEN** compilation fails with a missing symbol or package error (`package solim.runtime does not exist`)

#### Scenario: Mod referencing ComponentContext fails compilation
- **WHEN** application code in `:mod` imports or references `solim.runtime.ComponentContext`
- **THEN** compilation fails with a missing symbol or package error

### Requirement: Strict Directed Acyclic Graph across Solim subprojects
The framework subprojects SHALL follow a strict DAG dependency hierarchy:
- `:solim-api` depends on no other Solim module.
- `:solim-runtime` depends only on `:solim-api`.
- `:solim-core` depends on `:solim-api` (via `api`) and `:solim-runtime` (via `implementation`).
- `:solim` depends on `:solim-core` (via `api`) and `:solim-runtime` (via `implementation`).
- `:mod` depends on `:solim` and `:solim-mcp`.

#### Scenario: Zero circular dependencies in Gradle
- **WHEN** `./gradlew projects` or `./gradlew build` runs
- **THEN** Gradle resolves all task graphs without circular dependency warnings or errors

### Requirement: Preservation of automatic UI mounting
Application components extending `BaseComponent` SHALL retain automatic UI mounting when instantiated within container `children(...)` blocks without requiring explicit wrapper calls.

#### Scenario: Component constructed in children block auto-attaches
- **WHEN** `new MyCustomView()` is called inside a `UI.column(() -> { ... })` block
- **THEN** the component's element is automatically attached to the enclosing container Table
