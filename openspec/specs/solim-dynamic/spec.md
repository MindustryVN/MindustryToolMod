## ADDED Requirements

### Requirement: Dynamic value equality guard
The system SHALL skip rebuilding its child component when the source signal emits a value that is equal to the previous value.

#### Scenario: Same value does not trigger rebuild
- **WHEN** the source signal emits a value that is `Objects.equals()` to the current value
- **THEN** the existing child component is preserved without disposal or recreation

#### Scenario: Different value triggers rebuild
- **WHEN** the source signal emits a value that is not equal to the current value
- **THEN** the existing child component is disposed and a new one is created from the factory

#### Scenario: First emission triggers build
- **WHEN** the source signal emits for the first time (no previous value)
- **THEN** the child component is created from the factory
