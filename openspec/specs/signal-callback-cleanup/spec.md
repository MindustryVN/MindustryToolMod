# signal-callback-cleanup Specification

## Purpose
TBD - created by archiving change solim-architecture-refactor. Update Purpose after archive.
## Requirements
### Requirement: createSignal registrar returns a cleanup handle
The `createSignal` method on `BaseComponent` SHALL accept a `Function<Runnable, Disposable>` as the registrar, where the returned `Disposable` represents the callback subscription. The returned disposable SHALL be owned by the component.

#### Scenario: Registrar disposable is owned
- **WHEN** `createSignal(registrar, supplier)` is called inside a component
- **THEN** `registrar.apply(callback)` is called and its returned `Disposable` is added to the component's ownership list

#### Scenario: Component disposal unregisters callback
- **WHEN** the component is disposed
- **THEN** the registrar's returned `Disposable.dispose()` is called, unregistering the callback

### Requirement: No false automatic cleanup claims
APIs that cannot actually unregister a callback SHALL NOT claim automatic cleanup. If an Arc event API does not support unsubscription, the caller MUST be informed via documentation or a runtime assertion.

#### Scenario: No-op cleanup is explicit
- **WHEN** a registrar cannot provide real cleanup and returns `() -> {}`
- **THEN** this is documented at the call site and does not silently claim cleanup capability

