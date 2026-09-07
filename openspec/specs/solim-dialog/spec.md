# solim-dialog Specification

## Purpose
Declarative dialog component providing reactive signals, layout helpers, and automatic lifecycle management and disposal of attached content components and resources.

## Requirements

### Requirement: Automatic Content Component Lifecycle Management
The `SolimDialog` component SHALL automatically manage the lifecycle of any `Component` passed to its `content(Component)` method by registering the component's disposal with its internal disposable registry.

#### Scenario: Component content registered and disposed
- **WHEN** a `Component` is attached to a `SolimDialog` via `content(Component)`
- **THEN** the component is added to the dialog layout and registered as a `Disposable` with the dialog

#### Scenario: Dialog disposal cleans up content component
- **WHEN** `dispose()` is invoked on a `SolimDialog` with an attached `Component`
- **THEN** the attached component's `dispose()` method is automatically called

#### Scenario: Subclasses do not need manual disposal override
- **WHEN** a subclass of `SolimDialog` attaches a `Component` via `content(Component)`
- **THEN** disposing the dialog disposes the component without the subclass overriding `onDispose()`
