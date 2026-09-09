# automatic-effect-ownership Specification

## Purpose
TBD - created by archiving change solim-architecture-refactor. Update Purpose after archive.
## Requirements
### Requirement: Effects auto-register inside component scope
When `Effect.of(...)` is called while a component's `build()` is executing (i.e., `ComponentContext` has an active component), the Effect SHALL be registered with that component's ownership list before its initial execution.

#### Scenario: Effect created during build() is auto-owned
- **WHEN** `Effect.of(() -> ...)` is called inside `build()`
- **THEN** the Effect is added to the component's disposable list before `runEffect()` is called

#### Scenario: Effect is disposed when owning component is disposed
- **WHEN** the owning component is disposed
- **THEN** the Effect is disposed and unsubscribes from all its signal dependencies

### Requirement: Effects outside component scope are caller-managed
When `Effect.of(...)` is called outside any active component context, it SHALL not be auto-registered anywhere and SHALL be the caller's responsibility to dispose.

#### Scenario: Effect outside component scope has no implicit owner
- **WHEN** `Effect.of(...)` is called outside a `build()` execution
- **THEN** no component owns the Effect

### Requirement: All Effect.of() overloads share the same registration path
All three `Effect.of(Runnable)`, `Effect.of(Supplier<Runnable>)`, and `Effect.of(Consumer<Cleanup>)` SHALL use a single internal `create(...)` factory that performs registration before execution.

#### Scenario: Runnable overload registers before execution
- **WHEN** `Effect.of(runnable)` is called inside a build scope
- **THEN** the Effect is owned before `runnable` runs

#### Scenario: Supplier overload registers before execution
- **WHEN** `Effect.of(supplier)` is called inside a build scope
- **THEN** the Effect is owned before the supplier function runs

#### Scenario: Consumer overload registers before execution
- **WHEN** `Effect.of(cleanupConsumer)` is called inside a build scope
- **THEN** the Effect is owned before the consumer function runs

