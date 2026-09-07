## 1. Solim Widget Enhancements

- [x] 1.1 Add integer slider constructor and static factory to `SolimSlider` and `Ui.slider(Signal<Integer>, int, int, int)`
- [x] 1.2 Add callback constructor to `Checkbox` and `Ui.checkbox(String, boolean, Consumer<Boolean>)`
- [x] 1.3 Add unit tests in `solim` for integer slider and callback checkbox

## 2. QuickAccessSettingsDialog Refactoring

- [x] 2.1 Replace manual float cols signal and `.subscribe()` with direct integer slider binding `slider(feature.getColsConfig().signal(), 1, 9, 1)`
- [x] 2.2 Replace manual visibility signal and `.subscribe()` with callback checkbox `checkbox(f.getName(), feature.isFeatureVisible(meta.getId()), visible -> feature.setFeatureVisible(meta.getId(), visible))`
- [x] 2.3 Remove all single-use local variable declarations at constructor head and inline computed text bindings
- [x] 2.4 Maintain clean chained `row().gap(unit(2)).children(() -> ...)` declarations for all settings rows

## 3. Verification

- [x] 3.1 Run `./gradlew test` and verify code builds cleanly and complies with Java 8 runtime compatibility
