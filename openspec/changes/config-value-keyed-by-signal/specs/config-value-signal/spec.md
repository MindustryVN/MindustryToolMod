## MODIFIED Requirements

### Requirement: Bidirectional Synchronization
`ConfigValue<T>` SHALL synchronize mutations bidirectionally between its imperative setter and its reactive signal without entering infinite recursive update loops. Additionally, `ContextualConfigValue<T, K>` SHALL apply the same bidirectional guarantee: mutations via `set()` persist to the currently-active key and update the signal; discriminant changes save to the old key, load from the new key, and update the signal — all without re-entrant loops.

#### Scenario: Updating ConfigValue updates signal
- **WHEN** `configValue.set(newValue)` is called
- **THEN** the underlying preference is updated and `configValue.signal().get()` immediately reflects `newValue`.

#### Scenario: Updating signal updates preference storage
- **WHEN** `configValue.signal().set(newValue)` is called
- **THEN** the underlying configuration persistence setter is executed with `newValue`.

#### Scenario: Updating ContextualConfigValue updates signal and active key
- **WHEN** `contextualConfig.set(newValue)` is called while discriminant is in state `K`
- **THEN** `Core.settings` is updated at the key derived from `K` and `contextualConfig.signal()` emits `newValue`

#### Scenario: Discriminant change saves old value and loads new
- **WHEN** `contextualConfig` holds `valA` under key `K1` and the discriminant changes to `K2`
- **THEN** `valA` is persisted at key derived from `K1`, the value at key derived from `K2` is loaded, and the signal emits it — without triggering the set-listener for `K2`'s write
