# solim-reactivity Specification

## Purpose
TBD - created by archiving change create-solim-core. Update Purpose after archive.
## Requirements
### Requirement: Signal primitive
`Signal<T>` SHALL be a mutable reactive value created via `Signal.of(initial)` with `T get()`, `void set(T)`, equality-guarded notification, `Subscription subscribe(Consumer<T>)` returning disposable handle, and `Computed<U> map(Function<T,U>)` convenience. `get()` SHALL register as dependency when called inside a `Computed` or `Effect` evaluation.

#### Scenario: Equality-guarded notification
- **WHEN** `Signal<Integer> c = Signal.of(0)` and a subscriber is registered, then `c.set(0)` is called with equal value
- **THEN** subscriber is NOT notified and dependent Computeds/Effects do not re-run

#### Scenario: Subscription dispose stops notifications
- **WHEN** `Subscription s = count.subscribe(v -> log(v))` then `s.dispose()` is called
- **THEN** subsequent `count.set(...)` does not invoke the disposed callback

#### Scenario: Map creates derived Computed
- **WHEN** `Computed<String> t = enabled.map(v -> v ? "Enabled" : "Disabled")`
- **THEN** `t.get()` returns mapped value and re-computes when `enabled` changes, with disposal support

#### Scenario: Signal get tracks dependency in Computed and Effect
- **WHEN** `Computed<String> c = Signal.computed(() -> count.get() + "")` or `Effect.of(() -> log(count.get()))` reads `count.get()` during evaluation
- **THEN** the Computed/Effect is automatically subscribed to `count` and re-evaluates when `count` changes

### Requirement: Computed with lazy recomputation and dynamic dependencies
`Computed<T>` SHALL be created via `Signal.computed(Supplier<T>)` or `signal.map(...)`, with automatic dependency tracking, lazy recomputation (recompute on `get()` after invalidation), dynamic dependency cleanup when source branch changes, cycle detection, and `Subscription subscribe(Consumer<T>)` + `void dispose()`.

#### Scenario: Lazy recomputation only on get
- **WHEN** `Computed<String> title = Signal.computed(() -> "Count: " + count.get())` and `count.set(10)` invalidates `title`
- **THEN** supplier does NOT re-execute until `title.get()` or a subscriber/effect reads it

#### Scenario: Dynamic dependencies cleanup
- **WHEN** `Computed<String> v = Signal.computed(() -> darkMode.get() ? username.get() : email.get())` and `darkMode` toggles from true to false
- **THEN** `v` unsubscribes from `username` and subscribes to `email`; subsequent `username.set(...)` does NOT invalidate `v` while `email.set(...)` does

#### Scenario: Cycle detection does not infinite loop
- **WHEN** a `Computed` directly or transitively reads itself during evaluation
- **THEN** implementation detects the cycle and throws or logs without stack overflow / infinite loop, and old dependencies are not leaked

#### Scenario: Invalidation propagates to dependent Computeds
- **WHEN** `Computed b = a.map(...)` and `Computed c = b.map(...)` chain and `a` changes
- **THEN** both `b` and `c` are marked invalid and recompute correctly on next `get()` in topological order

#### Scenario: Computed subscription and dispose
- **WHEN** `Subscription s = computed.subscribe(v -> render(v))` then `computed.dispose()` is called
- **THEN** `computed` unsubscribes from all dependencies, clears listeners, and no longer recomputes or notifies

### Requirement: Effect with auto-tracking and dynamic dependencies
`Effect` SHALL be created via `Effect.of(Runnable|Supplier<Disposable>|Consumer<Cleanup>)` or `Ui.effect(...)`, automatically track every `Signal`/`Computed` read during execution, subscribe to those dependencies, re-run when any dependency changes, remove old dependencies before collecting new ones, handle dynamic branches, prevent leaks, log errors without crashing, and be disposable via `void dispose()`.

#### Scenario: Auto-tracking signals and computeds
- **WHEN** `Effect e = Effect.of(() -> Log.info(darkMode.get() + username.get()))`
- **THEN** `e` subscribes to `darkMode` and `username`; changing either re-runs the effect

#### Scenario: Dynamic dependency branch switch
- **WHEN** `Effect e = Effect.of(() -> { if (enabled.get()) Log.info(username.get()); else Log.info(email.get()); })` and `enabled` toggles
- **THEN** after toggle, `e` stops depending on the old branch value and starts depending on the new one; old dependency changes no longer trigger `e`

#### Scenario: Old dependencies removed before new collection
- **WHEN** Effect re-runs due to dependency change
- **THEN** it first unsubscribes from previous dependencies before executing supplier to collect new set, preventing leak of stale deps

#### Scenario: Dispose stops reacting
- **WHEN** `Effect e = Effect.of(...)` then `e.dispose()` is called
- **THEN** `e` unsubscribes from all dependencies, runs cleanup, and no longer re-runs on signal changes

#### Scenario: Effect errors are logged safely
- **WHEN** effect supplier throws an exception
- **THEN** exception is caught and logged via `arc.util.Log` (or equivalent) and does not break reactive graph or prevent other effects from running

### Requirement: Effect cleanup
Effects SHALL support cleanup that runs before re-execution and on dispose. Two acceptable APIs: returning `Disposable`/`Runnable` from supplier, or accepting `Cleanup` parameter with `cleanup.add(Runnable)`. Cleanup errors SHALL be logged and not prevent other cleanups or disposal.

#### Scenario: Cleanup runs before re-run
- **WHEN** `Effect.of(cleanup -> { Subscription s = api.events().subscribe(...); cleanup.add(s::dispose); })` and a dependency changes causing re-run
- **THEN** previous `s.dispose()` is invoked before new subscription is created

#### Scenario: Cleanup runs on dispose
- **WHEN** effect with registered cleanups is disposed
- **THEN** all cleanups are executed, errors are caught/logged, and disposal completes

#### Scenario: Cleanup failure does not block others
- **WHEN** multiple cleanups are registered and one throws
- **THEN** remaining cleanups still execute and the exception is logged

### Requirement: ReactiveContext and ReactiveObserver shared mechanism
A shared dependency-tracking mechanism SHALL allow the currently executing `Computed` or `Effect` (both implementing internal `ReactiveObserver`) to collect dependencies when `signal.get()`/`computed.get()` is called. Implementation SHALL use a stack-based context (e.g., `Deque<ReactiveObserver>`) suitable for single-threaded Arc/Mindustry UI thread and SHALL NOT use `ThreadLocal` unless necessary. Context SHALL support `push(observer)`, `pop()`, `current()`, and `track(dependency)`.

#### Scenario: Stack push/pop during evaluation
- **WHEN** `Effect` starts execution it pushes itself onto `ReactiveContext` stack, then `signal.get()` registers dependency via `ReactiveContext.current().addDependency(signal)`, then effect pops
- **THEN** only the active observer collects dependencies and nested evaluations correctly push/pop without corrupting outer observer

#### Scenario: Computed and Effect both use same context
- **WHEN** `Computed` evaluation reads a `Signal` that is also read by an `Effect`
- **THEN** both correctly register dependencies via the same `ReactiveContext`/`ReactiveObserver` abstraction without duplicate code paths

#### Scenario: No ThreadLocal by default
- **WHEN** `src/solim/signal/ReactiveContext.java` is inspected
- **THEN** it uses a plain static stack/Deque (or ArrayDeque) and does not import `java.lang.ThreadLocal` unless justified by test for threading

### Requirement: Subscription and Disposable contracts
`Subscription` (or `Disposable`) SHALL expose `void dispose()` idempotent. `Signal.subscribe`, `Computed.subscribe`, and `Computed/Effect.dispose` SHALL all return or implement this contract, clean up listener lists, and be safe to call multiple times.

#### Scenario: Idempotent dispose
- **WHEN** `subscription.dispose()` is called twice
- **THEN** second call is no-op and does not throw

#### Scenario: No leak after dispose
- **WHEN** 100 signals/computeds/effects are created and disposed
- **THEN** no strong references remain in dependency graphs (verifiable via listener count == 0 and no retained observer entries)

### Requirement: Signal.map convenience
`Signal<T>.map(Function<T,R>)` SHALL return a `Computed<R>` equivalent to `Signal.computed(() -> fn.apply(signal.get()))` with same lazy/dynamic/disposal semantics.

#### Scenario: Map reactive updates
- **WHEN** `Signal<Boolean> dark = Signal.of(false)` and `Computed<String> label = dark.map(v -> v ? "On" : "Off")`
- **THEN** `label.get()` is "Off", after `dark.set(true)` `label.get()` is "On", and `label` notifies subscribers

### Requirement: Untracked Execution in ReactiveContext
`ReactiveContext` SHALL provide an `untracked(Supplier<T>)` and `untracked(Runnable)` mechanism that temporarily suspends active dependency tracking so that any signal or computed reads occurring inside the block are not recorded as dependencies of the active `ReactiveObserver`.

#### Scenario: Reading signals inside untracked block
- **WHEN** an `Effect` is executing and invokes `ReactiveContext.untracked(() -> signal.get())`
- **THEN** `signal` is not added as a dependency to the running `Effect`, and changes to `signal` do not trigger the effect to re-run.

#### Scenario: Dynamic component factory isolation
- **WHEN** `Dynamic` executes its child component factory
- **THEN** the factory is invoked within an untracked scope, preventing child signal evaluations during component construction from leaking into the `Dynamic` switcher effect.

