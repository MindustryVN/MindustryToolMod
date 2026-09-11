## 1. Gradle Subproject Setup

- [x] 1.1 Add `:solim-core` to `settings.gradle`
- [x] 1.2 Create `solim-core/build.gradle` configuring dependencies and Mindustry test runtime
- [x] 1.3 Update `solim/build.gradle` with `java-library` and `api project(':solim-core')`
- [x] 1.4 Ensure `mod/build.gradle` depends strictly on `project(':solim')` and does not reference `project(':solim-core')`
- [x] 1.5 Update `solim-mcp/build.gradle` to depend on `project(':solim-core')`

## 2. Source Tree Migration

- [x] 2.1 Move all implementation packages and test files from `solim` to `solim-core` (`solim.core`, `solim.display`, `solim.input`, `solim.layout`, `solim.modifier`, `solim.overlay`, `solim.signal`, `solim.style`, `solim.ui`)
- [x] 2.2 Create `solim/src/solim/UI.java` in the `:solim` subproject
- [x] 2.3 Verify `solim/src` contains only `solim/UI.java`

## 3. Solim Public UI Facade Implementation

- [x] 3.1 Implement layout factory methods on `solim.UI` (`column`, `row`, `grid`, `card`, `scroll`, `stack`, `wrap`, `container`, `divider`, `spacer`)
- [x] 3.2 Implement widget factory methods on `solim.UI` (`button`, `text`, `textField`, `slider`, `checkbox`, `image`, `icon`, `networkImage`, `badge`, `badgeCount`)
- [x] 3.3 Implement overlay methods on `solim.UI` (`dialog`, `hud`)
- [x] 3.4 Implement reactivity factory methods on `solim.UI` (`signal`, `computed`, `effect`, `createSignal`)
- [x] 3.5 Implement structural, lifecycle, and utility methods on `solim.UI` (`dynamic`, `forEach`, `component`, `unit`, `dvw`, `dvh`, `listen`)

## 4. Mod & Consumer Migration

- [x] 4.1 Update all UI views, dialogs, and features in `mod` to import `solim.UI`
- [x] 4.2 Replace references to `solim.ui.Ui` with `solim.UI`
- [x] 4.3 Replace direct signal and effect instantiations in `mod` with `UI.signal()`, `UI.computed()`, `UI.effect()`

## 5. Verification

- [x] 5.1 Run `:solim-core:test` to verify all Solim core unit tests pass
- [x] 5.2 Run `:solim:build` to verify public facade compiles and packages cleanly
- [x] 5.3 Run `:mod:compileJava` to verify `mod` compiles using only `:solim`
- [x] 5.4 Run `./gradlew test jar` to verify end-to-end multi-project build succeeds
