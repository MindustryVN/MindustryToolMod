# scoped-context-api Specification

## Purpose
TBD - created by archiving change solim-architecture-refactor. Update Purpose after archive.
## Requirements
### Requirement: withoutAutoOwnership replaces pause/resume
`ComponentContext` SHALL provide a `withoutAutoOwnership(Runnable)` method that temporarily suspends auto-registration for the duration of the runnable. The suspension SHALL always be lifted after the runnable completes, even if it throws.

#### Scenario: Auto-ownership is suspended inside withoutAutoOwnership
- **WHEN** `ComponentContext.withoutAutoOwnership(() -> Effect.of(...))` is called
- **THEN** the Effect is NOT registered with the active component's ownership list

#### Scenario: Auto-ownership is restored after the runnable throws
- **WHEN** the runnable passed to `withoutAutoOwnership` throws an exception
- **THEN** auto-ownership is restored for subsequent calls

#### Scenario: Auto-ownership is restored after normal completion
- **WHEN** the runnable passed to `withoutAutoOwnership` completes normally
- **THEN** subsequent `Effect.of(...)` calls inside the same component scope are again auto-registered

### Requirement: Raw pause/resume is not part of the public API
`ComponentContext.pause()` and `ComponentContext.resume()` SHALL be package-private or removed. External callers SHALL use `withoutAutoOwnership(Runnable)` instead.

#### Scenario: pause() is not accessible to application code
- **WHEN** application code attempts to call `ComponentContext.pause()`
- **THEN** a compile-time access error is raised

