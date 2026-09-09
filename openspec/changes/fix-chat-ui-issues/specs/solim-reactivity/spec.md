## ADDED Requirements

### Requirement: Untracked Execution in ReactiveContext
`ReactiveContext` SHALL provide an `untracked(Supplier<T>)` and `untracked(Runnable)` mechanism that temporarily suspends active dependency tracking so that any signal or computed reads occurring inside the block are not recorded as dependencies of the active `ReactiveObserver`.

#### Scenario: Reading signals inside untracked block
- **WHEN** an `Effect` is executing and invokes `ReactiveContext.untracked(() -> signal.get())`
- **THEN** `signal` is not added as a dependency to the running `Effect`, and changes to `signal` do not trigger the effect to re-run.

#### Scenario: Dynamic component factory isolation
- **WHEN** `Dynamic` executes its child component factory
- **THEN** the factory is invoked within an untracked scope, preventing child signal evaluations during component construction from leaking into the `Dynamic` switcher effect.
