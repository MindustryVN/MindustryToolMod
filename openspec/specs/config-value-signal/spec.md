# config-value-signal Specification

## Purpose
Enables `ConfigValue<T>` configuration instances to provide stable, cached Solim reactive signals with bidirectional synchronization against underlying preference storage.

## Requirements

### Requirement: Reactive Signal Exposure
`ConfigValue<T>` SHALL provide a stable, cached reactive `Signal<T>` instance via its `signal()` method.

#### Scenario: Accessing signal returns stable instance
- **WHEN** caller invokes `configValue.signal()` multiple times
- **THEN** the exact same `Signal<T>` reference is returned without creating new instances.

#### Scenario: Signal initialized with current configuration value
- **WHEN** `ConfigValue<T>` is created
- **THEN** its `signal()` holds the value returned by the preference getter or default value.

### Requirement: Bidirectional Synchronization
`ConfigValue<T>` SHALL synchronize mutations bidirectionally between its imperative setter and its reactive signal without entering infinite recursive update loops.

#### Scenario: Updating ConfigValue updates signal
- **WHEN** `configValue.set(newValue)` is called
- **THEN** the underlying preference is updated and `configValue.signal().get()` immediately reflects `newValue`.

#### Scenario: Updating signal updates preference storage
- **WHEN** `configValue.signal().set(newValue)` is called
- **THEN** the underlying configuration persistence setter is executed with `newValue`.
