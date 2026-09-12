## MODIFIED Requirements

### Requirement: Single own() method on BaseComponent
`BaseComponent` SHALL expose a single internal-only `own(T extends Disposable)` method (package-private, no access modifier) that adds the given resource to the component's disposal list and returns it. It SHALL be callable only within package `solim.core`; application modules SHALL NOT be able to call it.

#### Scenario: own() adds to disposal list
- **WHEN** `own(disposable)` is called on a component from within `solim.core`
- **THEN** the disposable is added to the component's internal list and disposed when the component is disposed

#### Scenario: own() returns the disposable
- **WHEN** `T result = own(disposable)` is called from within `solim.core`
- **THEN** `result` is the same instance as `disposable`

#### Scenario: own(null) is safe
- **WHEN** `own(null)` is called
- **THEN** no exception is thrown and nothing is added to the disposal list

#### Scenario: own() is not callable from application code
- **WHEN** code outside package `solim.core` attempts to call `own(disposable)`
- **THEN** a compile-time access error is raised and compilation fails
