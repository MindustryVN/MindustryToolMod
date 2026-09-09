# structural-reconciler Specification

## Purpose
TBD - created by archiving change solim-architecture-refactor. Update Purpose after archive.
## Requirements
### Requirement: Shared structural reconciler
A shared reconciler utility SHALL exist (e.g., `StructuralReconciler`) that implements the keyed child lifecycle: create new keys, preserve existing keys (reuse component), remove missing keys (dispose component), and mount/unmount elements in the container.

#### Scenario: New key creates a new component
- **WHEN** reconciliation runs and a new key is present that was not in the previous set
- **THEN** a new component is created via the factory and its element is added to the container

#### Scenario: Existing key reuses component
- **WHEN** reconciliation runs and a key is present in both the previous and current set
- **THEN** the existing component is reused (not rebuilt) and remains in the container

#### Scenario: Removed key disposes component
- **WHEN** reconciliation runs and a key from the previous set is absent from the current set
- **THEN** the corresponding component is disposed and its element is removed from the container

#### Scenario: Order changes move elements without rebuilding
- **WHEN** reconciliation runs and the order of existing keys changes
- **THEN** existing components are not rebuilt; only their elements are reordered in the container

### Requirement: Dynamic, ForEach, and ReactiveGrid delegate to StructuralReconciler
`Dynamic`, `ForEach`, and `ReactiveGrid` SHALL share the same child-lifecycle implementation provided by `StructuralReconciler`. They SHALL NOT independently duplicate create/preserve/remove/dispose logic.

#### Scenario: ForEach uses StructuralReconciler
- **WHEN** `ForEach` reconciles a new list
- **THEN** it delegates to `StructuralReconciler` for lifecycle management

#### Scenario: ReactiveGrid uses StructuralReconciler
- **WHEN** `ReactiveGrid` reconciles a new data set
- **THEN** it delegates to `StructuralReconciler` for lifecycle management

