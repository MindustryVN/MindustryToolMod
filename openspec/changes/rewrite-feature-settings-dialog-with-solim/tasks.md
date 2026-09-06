## 1. Reactive State & Layout Architecture

- [x] 1.1 Define reactive state signals and computed values in `FeatureSettingDialog` (`filter`, `revision`, `filteredFeatures`, `contentWidth`, `columnCount`)
- [x] 1.2 Re-implement dialog action bar using Solim declarative layout (`Ui.row`) with `SolimTextField` search binding and re-enable action button

## 2. Feature Card Integration & Grid Layout

- [x] 2.1 Refactor or adapt `FeatureCard` to bind feature state changes reactively and protect action shortcuts from bubbling toggle events
- [x] 2.2 Implement reactive grid pane effect in `FeatureSettingDialog` that re-renders card elements or empty state indicator whenever filtered features or column count changes

## 3. Dialog Lifecycle & Event Bus Cleanup

- [x] 3.1 Implement `Disposable` in `FeatureSettingDialog` to dispose active Solim `Effect` instances and unsubscribe from `FeatureStateChanged`
- [x] 3.2 Wire dialog `shown` and `hidden` hooks to manage effect subscription lifecycles cleanly

## 4. Verification & AGENTS.md Compliance

- [x] 4.1 Verify that all strings use translation bundle keys and verify strict Java 8 runtime compatibility
- [x] 4.2 Build and verify the mod using Gradle to ensure clean compilation and test execution
