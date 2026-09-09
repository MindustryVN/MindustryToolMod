## ADDED Requirements

### Requirement: Component interface contract
The test suite SHALL verify that Component interface defines the expected lifecycle methods.

#### Scenario: Component has build method
- **WHEN** a class implements Component
- **THEN** it SHALL have a `build()` method returning an Element

#### Scenario: Component has dispose method
- **WHEN** a class implements Component
- **THEN** it SHALL have a `dispose()` method for cleanup

### Requirement: BaseComponent construction
The test suite SHALL verify that BaseComponent subclasses can be constructed and built.

#### Scenario: BaseComponent creates element on build
- **WHEN** a BaseComponent subclass is built
- **THEN** it SHALL return a non-null Arc Element

#### Scenario: BaseComponent owns children
- **WHEN** child components are added during build
- **THEN** they SHALL be disposed when the parent is disposed

### Requirement: BaseComponent reactive bindings
The test suite SHALL verify that BaseComponent manages signal subscriptions.

#### Scenario: Bindings disposed on component dispose
- **WHEN** a BaseComponent with reactive bindings is disposed
- **THEN** all signal subscriptions SHALL be unsubscribed
