## Why

`ConfigValue` currently stores values under a fixed key, so per-orientation preferences (e.g., the chat overlay position in portrait vs. landscape) must be managed manually with separate config objects and manual key switching. The `ChatFeature` already has 4 position config values (collapsed/expanded × x/y) and will need 8 if orientation is added — the combinatorial explosion is untenable. A `ConfigValue` that reactively switches its active storage key based on a `Readable<K>` discriminant signal solves this at the framework level and keeps call sites clean.

## What Changes

- A new `ContextualConfigValue<T, K>` class is introduced: a `ConfigValue`-like wrapper that maps a `Readable<K>` discriminant signal to per-key storage. When the discriminant changes, the active storage key changes, the current value is reloaded from the new key, and the reactive signal reflects the new value.
- `ConfigGroup` gains factory methods (`boolValueKeyed`, `floatValueKeyed`, etc.) that accept a key-name function `Function<K, String>` and a discriminant `Readable<K>`, returning `ContextualConfigValue<T, K>`.
- A global `OrientationSignal` (in `mindustrytool.features` or a new `mindustrytool.platform` package) provides a `Signal<Boolean>` that emits `true` when `Core.graphics.isPortrait()` changes, suitable as a discriminant for `ContextualConfigValue`. It is updated on the Arc `ResizeEvent`.
- `ChatFeature` is updated to use `ContextualConfigValue<Float, Boolean>` for position configs (`collapsedX`, `collapsedY`, `expandedX`, `expandedY`) keyed by orientation, replacing the current 4 separate configs with 4 contextual ones.

## Capabilities

### New Capabilities

- `contextual-config-value`: A reactive config value that dynamically switches its backing storage key based on a discriminant signal (`Readable<K>`), reloading the persisted value and emitting on every discriminant change.
- `orientation-signal`: A framework-level `Signal<Boolean>` reflecting `Core.graphics.isPortrait()` that fires on `ResizeEvent`. Suitable as a ready-made discriminant for orientation-aware config values.

### Modified Capabilities

- `config-value-signal`: The bidirectional sync requirement now applies to `ContextualConfigValue` in addition to `ConfigValue` — when the discriminant changes, the signal reflects the newly-loaded value for the new key, and mutations write to the currently-active key.

## Impact

- **`mod/src/mindustrytool/config/`**: New `ContextualConfigValue.java`. `ConfigGroup.java` gains new factory methods.
- **`mod/src/mindustrytool/features/`** (or `platform/`): New `OrientationSignal.java` providing `Signal<Boolean> isPortrait`.
- **`mod/src/mindustrytool/features/chat/ChatFeature.java`**: Replace `collapsedXConfig`, `collapsedYConfig`, `expandedXConfig`, `expandedYConfig` with orientation-keyed contextual variants; remove manual `isPortrait`-branching logic that would otherwise be needed.
- **No breaking changes** — existing `ConfigValue` and `ConfigGroup` APIs remain unchanged.
