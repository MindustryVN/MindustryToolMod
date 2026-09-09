# unified-ownership-api Specification

## Purpose
TBD - created by archiving change solim-architecture-refactor. Update Purpose after archive.
## Requirements
### Requirement: Component extends Disposable
The `Component` interface SHALL extend `Disposable` so that components can be passed directly to `own()` and similar ownership APIs.

#### Scenario: Component is Disposable
- **WHEN** a class implements `Component`
- **THEN** it compiles as a `Disposable` and can be passed to any method accepting `Disposable`

### Requirement: Single own() method on BaseComponent
`BaseComponent` SHALL expose a single `own(T extends Disposable)` method that adds the given resource to the component's disposal list and returns it.

#### Scenario: own() adds to disposal list
- **WHEN** `own(disposable)` is called on a component
- **THEN** the disposable is added to the component's internal list and disposed when the component is disposed

#### Scenario: own() returns the disposable
- **WHEN** `T result = own(disposable)` is called
- **THEN** `result` is the same instance as `disposable`

#### Scenario: own(null) is safe
- **WHEN** `own(null)` is called
- **THEN** no exception is thrown and nothing is added to the disposal list

### Requirement: Legacy ownership APIs are removed or deprecated
`registerDisposable(...)`, `ownChild(...)`, and any other ownership alias methods SHALL be deprecated and all internal call sites SHALL be migrated to `own(...)`.

#### Scenario: Deprecated API still compiles but warns
- **WHEN** a deprecated ownership method is called
- **THEN** a compiler deprecation warning is emitted but the call still functions identically to `own(...)`

