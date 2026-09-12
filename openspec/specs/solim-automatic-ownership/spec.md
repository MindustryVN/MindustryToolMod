# solim-automatic-ownership Specification

## Purpose
TBD - created by archiving change clean-up-solim-refactor. Update Purpose after archive.
## Requirements
### Requirement: Ambient Lifecycle Ownership in Component Build
The Solim framework SHALL automatically track child components, disposables, and Solim controls instantiated or attached during a component's `build()` execution without requiring explicit `own()` or `ownChild()` invocations. Ambient ownership SHALL be the sole ownership path for application code; manual ownership calls in application modules SHALL be rejected by the compiler.

#### Scenario: Child component created in build context
- **WHEN** a component instantiates child components or Solim controls within its `build()` method
- **THEN** the child components and controls are automatically registered with the parent component's disposable registry and disposed when the parent is disposed

#### Scenario: Clean stack unwinding on build failure
- **WHEN** a component's `build()` method throws an exception during construction
- **THEN** the ambient build context stack is cleanly unwound and does not leak context to subsequent component builds

#### Scenario: Manual own() in application code is a compile error
- **WHEN** application code outside `solim.core` calls `own(...)` or `ownChild(...)`
- **THEN** compilation fails, and the author migrates to `effect()`, instance `listen()`, or plain declarative bindings inside `build()`

### Requirement: ComponentContext is the canonical internal registration path
Framework code outside `solim.core` that must own a resource SHALL use `ComponentContext.register(disposable)` or `ComponentContext.registerChild(child)` instead of calling `own()` directly. These methods SHALL be null-safe, pause-aware, and delegate to the ambient owning component.

#### Scenario: Layout registers an effect internally
- **WHEN** a layout creates an `Effect` outside `solim.core` that must live with the owning component
- **THEN** it passes the effect to `ComponentContext.register(...)` and the effect is disposed with the component

### Requirement: Automatic Control and Binding Registration
Solim controls and reactive property bindings declared inside a component build SHALL automatically register their subscriptions and event listeners with the enclosing component.

#### Scenario: SolimTextField declared in build
- **WHEN** a `SolimTextField` is created during a component's `build()` without wrapping in `own(...)`
- **THEN** its internal signal and event bindings are automatically bound to the enclosing component's lifecycle and disposed upon component disposal

#### Scenario: Reactive property binding declared in build
- **WHEN** a property binding is created during a component's `build()`
- **THEN** the subscription is registered with the current component and cancelled upon component disposal

