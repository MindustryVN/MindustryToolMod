## ADDED Requirements

### Requirement: ContextualConfigValue switches storage key on discriminant change
`ContextualConfigValue<T, K>` SHALL be a reactive config value that accepts a `Readable<K>` discriminant and a `Function<K, String>` key-suffix mapper. When the discriminant emits a new value, the currently-active storage key SHALL change, the old value SHALL be persisted under the old key, the new value SHALL be loaded from `Core.settings` under the new key (falling back to `defaultValue`), and the reactive signal SHALL be updated to reflect the new value.

#### Scenario: Discriminant change reloads value from new key
- **WHEN** a `ContextualConfigValue<Float, Boolean>` with discriminant `isPortrait` holds value `100f` under key `foo.x.landscape` and `isPortrait` changes to `true`
- **THEN** the value `100f` is persisted under `foo.x.landscape`, `Core.settings` is read at `foo.x.portrait`, and the signal emits that loaded value (or default if not yet stored)

#### Scenario: Mutation writes to active key
- **WHEN** `contextualConfig.set(42f)` is called while discriminant is `true` (portrait)
- **THEN** `Core.settings` stores `42f` at `foo.x.portrait` and the reactive signal emits `42f`

#### Scenario: Signal is reactive
- **WHEN** UI binds to `contextualConfig.signal()` and the discriminant changes
- **THEN** the signal emits the newly-loaded value so reactive components update automatically

#### Scenario: Default value used for unvisited keys
- **WHEN** a discriminant value is seen for the first time and no value exists in `Core.settings` for the derived key
- **THEN** the signal holds the `defaultValue` provided at construction

### Requirement: ConfigGroup factory methods for contextual values
`ConfigGroup` SHALL expose factory methods `boolValueKeyed`, `intValueKeyed`, `floatValueKeyed`, and `stringValueKeyed` that accept a base name, a discriminant `Readable<K>`, a key-suffix function `Function<K, String>`, and a default value, returning a `ContextualConfigValue<T, K>`. The storage key for each slot SHALL be derived as `resolveKey(baseName + "." + suffix.apply(discriminantValue))`.

#### Scenario: Key derivation uses ConfigGroup namespace
- **WHEN** `configGroup.floatValueKeyed("x", isPortrait, p -> p ? "portrait" : "landscape", 0f)` is called on a group with namespace `mindustrytool.features.chat.collapsed`
- **THEN** portrait values are stored at `mindustrytool.features.chat.collapsed.x.portrait` and landscape values at `mindustrytool.features.chat.collapsed.x.landscape`
