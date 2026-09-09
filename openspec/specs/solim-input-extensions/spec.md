# solim-input-extensions Specification

## Purpose
Provides enter-key submission and validation hooks on SolimTextField.

## Requirements
### Requirement: Enter Key Submission
The system SHALL provide onEnter(Consumer<String> onSubmit) and onEnter(Runnable onSubmit) on solim.input.SolimTextField.

#### Scenario: Pressing enter key with text
- **WHEN** user types text into the field and presses the Enter key
- **THEN** the onEnter callback is invoked with the current text content of the field.

#### Scenario: Enter key on disabled field
- **WHEN** the text field is disabled or disabled signal evaluates to true
- **THEN** pressing Enter does not trigger the onEnter callback.

### Requirement: Custom Input Validation Feedback
The system SHALL allow attaching custom input validator predicates to SolimTextField with reactive validity signals.

#### Scenario: Content validation check
- **WHEN** a validator predicate is registered on the text field
- **THEN** validity updates reactively based on the current text length and format.
