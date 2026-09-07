## 1. Core and Modifier Foundation

- [ ] 1.1 Add `ElementModifiers.name(@Nullable Element element, @Nullable String name)` in `solim.modifier`
- [ ] 1.2 Add `default Component name(String name)` method to `solim.core.Component`
- [ ] 1.3 Update `solim.core.BaseComponent` to support `.name(String)` with lazy build caching

## 2. Component Implementation

- [ ] 2.1 Add fluent `.name(String)` to layout components (`Row`, `Column`, `Card`, `Scroll`, `Grid`, `Container`, `Divider`, `Spacer`, `SolimStack`, `Wrap`)
- [ ] 2.2 Implement `Component` and fluent `.name(String)` on input widgets (`Button`, `Checkbox`, `SolimSlider`, `SolimTextField`, `Switch`, `SolimSelect`)
- [ ] 2.3 Add fluent `.name(String)` to display and overlay components (`Text`, `SolimImage`, `SolimDialog`)

## 3. Verification

- [ ] 3.1 Add unit tests in `solim` verifying `.name(String)` sets `element().name` and preserves method chaining across components
- [ ] 3.2 Run `./gradlew test` and verify clean build and Java 8 compatibility
