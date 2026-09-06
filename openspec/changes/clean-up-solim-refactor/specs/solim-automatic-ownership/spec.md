## ADDED Requirements

### Requirement: Ambient Lifecycle Ownership in Component Build
The Solim framework SHALL automatically track child components, disposables, and Solim controls instantiated or attached during a component's `build()` execution without requiring explicit `own()` or `ownChild()` invocations.

#### Scenario: Child component created in build context
- **WHEN** a component instantiates child components or Solim controls within its `build()` method
- **THEN** the child components and controls are automatically registered with the parent component's disposable registry and disposed when the parent is disposed

#### Scenario: Clean stack unwinding on build failure
- **WHEN** a component's `build()` method throws an exception during construction
- **THEN** the ambient build context stack is cleanly unwound and does not leak context to subsequent component builds

### Requirement: Automatic Control and Binding Registration
Solim controls and reactive property bindings declared inside a component build SHALL automatically register their subscriptions and event listeners with the enclosing component.

#### Scenario: SolimTextField declared in build
- **WHEN** a `SolimTextField` is created during a component's `build()` without wrapping in `own(...)`
- **THEN** its internal signal and event bindings are automatically bound to the enclosing component's lifecycle and disposed upon component disposal

#### Scenario: Reactive property binding declared in build
- **WHEN** a property binding is created during a component's `build()`
- **THEN** the subscription is registered with the current component and cancelled upon component disposal
