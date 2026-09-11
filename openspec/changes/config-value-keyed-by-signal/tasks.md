## 1. OrientationSignal

- [x] 1.1 Create `mindustrytool/features/OrientationSignal.java` with a private static `Signal<Boolean>` initialized to `false`
- [x] 1.2 Implement `init()` to set the signal from `Core.graphics.isPortrait()` and register a `ResizeEvent` listener that updates the signal (post via `Core.app.post` to ensure dimensions are updated)
- [x] 1.3 Expose `public static Readable<Boolean> isPortrait()` returning the signal
- [x] 1.4 Call `OrientationSignal.init()` from `Main.java` during mod initialization (after `Core.graphics` is available)

## 2. ContextualConfigValue

- [ ] 2.1 Create `mindustrytool/config/ContextualConfigValue.java` with type parameters `<T, K>`
- [ ] 2.2 Constructor accepts: `ConfigGroup group`, `String baseName`, `Readable<K> discriminant`, `Function<K, String> keySuffix`, `T defaultValue`, and a `BiFunction<String, T, Void> persister` (or typed getter/setter factories per type)
- [ ] 2.3 On construction, compute the initial key from current discriminant value, load from `Core.settings`, initialize `Signal<T>` with loaded value or default
- [ ] 2.4 Subscribe to the discriminant: on change, persist current signal value under old key, derive new key, load from `Core.settings`, update signal (guard against re-entrant loops with `updating` flag)
- [ ] 2.5 Implement `get()` returning `signal.peek()`, `set(T value)` persisting to active key and updating signal, `signal()` returning the `Signal<T>`, `reset()` calling `set(defaultValue)`
- [ ] 2.6 Implement `isModified()` comparing `get()` to `defaultValue`

## 3. ConfigGroup factory methods

- [x] 3.1 Add `<K> ContextualConfigValue<Boolean, K> boolValueKeyed(String name, Readable<K> discriminant, Function<K, String> keySuffix, boolean defaultValue)` to `ConfigGroup`
- [x] 3.2 Add `<K> ContextualConfigValue<Integer, K> intValueKeyed(...)` to `ConfigGroup`
- [x] 3.3 Add `<K> ContextualConfigValue<Float, K> floatValueKeyed(...)` to `ConfigGroup`
- [x] 3.4 Add `<K> ContextualConfigValue<String, K> stringValueKeyed(...)` to `ConfigGroup`

## 4. ChatFeature: orientation-aware position configs

- [x] 4.1 In `ChatFeature`, replace `collapsedXConfig`, `collapsedYConfig`, `expandedXConfig`, `expandedYConfig` (`ConfigValue<Float>`) with `ContextualConfigValue<Float, Boolean>` keyed by `OrientationSignal.isPortrait()` with suffix `p -> p ? "portrait" : "landscape"`
- [x] 4.2 Update `xSignal`/`ySignal` subscription logic in `ChatFeature` — the contextual config now automatically switches keys on orientation change; remove any manual `isPortrait` branching that was previously needed for position storage
- [x] 4.3 Verify `resetPosition()` in `ChatFeature` still works correctly: calling `set()` on the contextual configs writes to whichever orientation is currently active
- [x] 4.4 Ensure `collapsedConfig.signal().subscribe(...)` position-swap logic in `ChatFeature` still reads from the correct (now orientation-aware) contextual configs

## 5. Verification

- [x] 5.1 Build mod: `gradlew :mod:compileJava` — no compile errors
- [ ] 5.2 Run Mindustry (`run.bat`), drag the chat overlay and verify position is saved per orientation (if testing on a resizable window, resize to trigger orientation change)
- [ ] 5.3 Verify that collapsing/expanding the chat still restores the correct position for the current orientation
- [ ] 5.4 Stop Mindustry via MCP `stop` tool after verification
