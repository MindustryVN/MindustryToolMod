# lifecycle-correctness Specification

## Purpose
TBD - created by archiving change solim-architecture-refactor. Update Purpose after archive.
## Requirements
### Requirement: Disposal order is reversed
Resources owned by a `BaseComponent` SHALL be disposed in reverse registration order (LIFO). Each resource SHALL be attempted independently so that a failure in one does not prevent others from being disposed.

#### Scenario: LIFO disposal order
- **WHEN** a component owns resources A, B, C in that order
- **THEN** on `dispose()`, C is disposed first, then B, then A

#### Scenario: Error isolation during disposal
- **WHEN** one disposable throws during `dispose()`
- **THEN** the error is logged via `Log.err` and remaining disposables continue to be disposed

#### Scenario: Disposable list is cleared after disposal
- **WHEN** `dispose()` completes
- **THEN** the internal disposable list is empty

### Requirement: Disposed component cannot be rebuilt
A `BaseComponent` that has been disposed SHALL throw `IllegalStateException` if `element()` is called.

#### Scenario: element() after dispose() throws
- **WHEN** `element()` is called on a disposed component
- **THEN** `IllegalStateException` is thrown with a message identifying the component class

### Requirement: Double disposal is safe
Calling `dispose()` more than once on a `BaseComponent` SHALL be a no-op on subsequent calls.

#### Scenario: Second dispose() call
- **WHEN** `dispose()` is called a second time
- **THEN** no exception is thrown and no resources are disposed again

### Requirement: Partial builds are cleaned up on failure
If `build()` throws, all resources registered with the component up to that point SHALL be disposed.

#### Scenario: Exception during build() cleans up
- **WHEN** `build()` throws a `RuntimeException` after registering some resources
- **THEN** `dispose()` is called, those resources are released, and the exception propagates to the caller

#### Scenario: ComponentContext is always popped on build failure
- **WHEN** `build()` throws
- **THEN** `ComponentContext.pop()` is still called (the context stack is not corrupted)

### Requirement: build() returning null is detected
If `build()` returns `null`, the component SHALL throw `IllegalStateException` with a descriptive message.

#### Scenario: Null return from build()
- **WHEN** `build()` returns `null`
- **THEN** `IllegalStateException` is thrown and the component's resources are disposed

