## Context

`ConfigValue<T>` wraps a single fixed `String key` that maps to `Core.settings`. The `ChatFeature` currently manages 4 position `ConfigValue` instances (collapsed-x, collapsed-y, expanded-x, expanded-y). Adding orientation awareness naively would double this to 8 objects (portrait/landscape variants of each), require manual `isPortrait` branching everywhere values are read or written, and add `ResizeEvent` listeners in every feature that needs this.

The goal is a reusable abstraction where the storage key is derived from the current value of a reactive discriminant signal, so the value automatically tracks the right persisted slot.

## Goals / Non-Goals

**Goals:**
- `ContextualConfigValue<T, K>` — a drop-in complement to `ConfigValue` with a reactive discriminant `Readable<K>` that changes the active storage key when it fires.
- `OrientationSignal` — a singleton `Signal<Boolean>` reflecting `Core.graphics.isPortrait()`, updated on `ResizeEvent`.
- `ConfigGroup` factory methods for contextual values (`boolValueKeyed`, `floatValueKeyed`, etc.).
- Update `ChatFeature` to use orientation-keyed position configs.

**Non-Goals:**
- Changing or removing the existing `ConfigValue` API.
- Supporting multiple simultaneous discriminant signals (single discriminant only).
- Persisting the discriminant key itself.
- Hot-swapping the discriminant at runtime after construction.

## Decisions

### Decision 1: `ContextualConfigValue<T, K>` vs extending `ConfigValue<T>`

**Options:**
- A) Extend `ConfigValue<T>` with an overridable key computation.
- B) New `ContextualConfigValue<T, K>` class (not a subtype of `ConfigValue<T>`).

**Decision: Option B — new class.** `ConfigValue` stores `key` as a `final String`. Extending it with a mutable key would violate its contract and complicate the existing bidirectional sync guard. A separate class with the same public surface (`get()`, `set()`, `signal()`, `reset()`) is cleaner and avoids inheritance pitfalls.

### Decision 2: Key derivation — `Function<K, String>` vs `Function<K, String>` keyed through `ConfigGroup`

`ConfigGroup` already handles namespace resolution via `resolveKey(String name)`. The factory method `floatValueKeyed(String baseName, Readable<K> discriminant, Function<K, String> suffix, float defaultValue)` will compute the storage key as `resolveKey(baseName + "." + suffix.apply(discriminantValue))`. This keeps the namespace prefix from `ConfigGroup` and lets the discriminant provide only the differentiating suffix (e.g., `"portrait"` / `"landscape"`).

Usage:
```java
collapsedXConfig = collapsedGroup.floatValueKeyed(
    "x",
    OrientationSignal.isPortrait(),
    portrait -> portrait ? "portrait" : "landscape",
    defaultX
);
```
This stores values at keys like `mindustrytool.features.chat.collapsed.x.portrait`.

### Decision 3: When to reload — subscribe vs compute-on-get

**Options:**
- A) On discriminant change: reload from `Core.settings` under the new key, push new value to signal.
- B) On `get()`: always call `Core.settings.get(currentKey())` live, no signal.

**Decision: Option A — subscribe and reload.** The value must be reactive for UI bindings (`text(config.signal())`). Computing on-get would break all reactive consumers. On discriminant change: (1) save current signal value under the old key, (2) load from `Core.settings` under the new key, (3) push to signal. Mutation (`set(v)`) always writes to the currently-active key.

### Decision 4: `OrientationSignal` placement

Place `OrientationSignal` in `mindustrytool.features` as a simple static utility class:

```java
public final class OrientationSignal {
    private static final Signal<Boolean> signal = Signal.of(false);

    public static void init() {
        signal.set(Core.graphics.isPortrait());
        Events.on(ResizeEvent.class, e -> signal.set(Core.graphics.isPortrait()));
    }

    public static Readable<Boolean> isPortrait() {
        return signal;
    }
}
```

`init()` is called once from `Main` (or the first feature that uses it). The `ResizeEvent` is fired by Mindustry when the window/screen dimensions change.

### Decision 5: Default value per discriminant slot

`ContextualConfigValue` uses a single `defaultValue` (same as `ConfigValue`). Each slot that has never been saved starts from that default. If per-slot defaults are needed later, that is a future extension.

## Risks / Trade-offs

- **[Risk] `ResizeEvent` may fire before `Core.graphics` reports the new dimensions** → Mitigation: post to `Core.app.post(...)` so the signal update runs after the resize is fully committed. Check empirically.
- **[Risk] First load before `init()`** → `signal` is initialized to `false`. If `init()` is called before any feature reads orientation, this is safe. Document call order requirement.
- **[Risk] Old keys left in `Core.settings` when orientation-keyed values replace flat keys** → `ChatFeature` previously stored `collapsed.x` flat; now stores `collapsed.x.portrait` and `collapsed.x.landscape`. On first run, old keys are unused and new ones load the default. Acceptable migration — old keys can be cleaned up manually if needed.
