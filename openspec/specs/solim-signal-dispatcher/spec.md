# solim-signal-dispatcher Specification

## Purpose
One-flush-per-frame signal dispatching in Solim, providing lightweight batching and deduplication so reactive effects execute at most once per Mindustry frame even when multiple dependencies change during the same frame.

## Requirements

### Requirement: Signal effect batching and deduplication
The reactive system SHALL queue invalidated `Effect`s and execute them at most once per Mindustry frame upon `flush()`, regardless of how many dependency signals were modified in that frame.

#### Scenario: Multiple updates to a single signal within a frame
- **WHEN** a signal is updated multiple times in the same frame before `flush()`
- **THEN** an observing effect executes exactly once during the subsequent `flush()`

#### Scenario: Multiple dependency signals modified within a frame
- **WHEN** multiple signals observed by a single effect are modified in the same frame before `flush()`
- **THEN** the observing effect executes exactly once during the subsequent `flush()`

#### Scenario: Signal updates across separate frames
- **WHEN** a signal is updated, followed by `flush()`, and then updated again followed by another `flush()`
- **THEN** the observing effect executes once in each flush (twice in total)

### Requirement: Cascading effect processing during flush
The dispatcher SHALL continue processing effects that become dirty while another effect is executing during `flush()` until all pending effects are exhausted, using generation-based batch passes up to an iteration safety limit. A single flush pass SHALL drain all currently queued effects without incrementing the cascading generation limit.

#### Scenario: Effect dirtied during execution of another effect
- **WHEN** Effect A modifies Signal B during `flush()`, which invalidates Effect B
- **THEN** Effect B is enqueued and executed within a subsequent cascade pass of the same `flush()` cycle

#### Scenario: Large batch of independent effects does not trip cycle limit
- **WHEN** a single flush cycle contains more than 100 queued effects that do not produce recursive cascade loops
- **THEN** all effects execute completely without triggering an infinite reactive loop warning

#### Scenario: Cycle detection limits infinite execution
- **WHEN** effects cause a circular dependency that repeatedly enqueues effects exceeding the cascade depth threshold
- **THEN** the dispatcher terminates the flush loop, logs an error, and clears the queue

### Requirement: Disposed effect skipping
The dispatcher SHALL not execute any effect that has been disposed, even if it was scheduled prior to disposal.

#### Scenario: Effect disposed while queued
- **WHEN** an effect is invalidated and enqueued, and subsequently disposed before `flush()` runs
- **THEN** the effect is skipped during `flush()` and its logic does not run

### Requirement: Lazy computed values are not eagerly scheduled
The reactive system SHALL keep `Computed` evaluations lazy and SHALL not enqueue `Computed` observers into the frame dispatcher.

#### Scenario: Computed value marked dirty without eager evaluation
- **WHEN** a signal dependency of a `Computed` is modified
- **THEN** the `Computed` is marked dirty and notifies downstream observers, but does not recompute until `.get()` or `.peek()` is called

### Requirement: Idempotent frame lifecycle registration
The reactive system SHALL register its frame hook with Mindustry's `Trigger.update` at most once, even if initialization is invoked multiple times.

#### Scenario: Multiple initialization calls
- **WHEN** `SignalDispatcher.register()` or `UI.init()` is called multiple times
- **THEN** the update listener is registered exactly once with the event bus
