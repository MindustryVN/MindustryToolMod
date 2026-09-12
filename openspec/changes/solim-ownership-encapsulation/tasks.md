## 1. Subproject Scaffold & Dependency Wiring

- [ ] 1.1 Register `:solim-api` and `:solim-runtime` in `settings.gradle`
- [ ] 1.2 Create `solim-api/build.gradle` (exports to `:solim-runtime` and `:solim-core`)
- [ ] 1.3 Create `solim-runtime/build.gradle` (depends on `:solim-api`, used as `implementation` by `:solim-core` and `:solim`)
- [ ] 1.4 Update `solim-core/build.gradle` (`api project(':solim-api')`, `implementation project(':solim-runtime')`)
- [ ] 1.5 Update `solim/build.gradle` (`api project(':solim-core')`, `implementation project(':solim-runtime')`)
- [ ] 1.6 Update `solim-mcp/build.gradle` to include `:solim-runtime` and `:solim-api`

## 2. Base Contracts (`:solim-api`)

- [ ] 2.1 Move `Component.java`, `Disposable.java`, `Readable.java`, and `EventsUtil.java` into `:solim-api`
- [ ] 2.2 Verify `:solim-api:compileJava` passes cleanly

## 3. Internal Engine Extraction (`:solim-runtime`)

- [ ] 3.1 Move `ParentStack`, `ComponentContext`, `ReactiveContext`, `SignalDispatcher`, `StructuralReconciler`, `Binding`, `ElementResolver`, `Ui` into `solim.runtime.*` in `:solim-runtime`
- [ ] 3.2 Decouple `ParentStack` from concrete `Text`/`SolimImage` and layout `SizeConstraints`
- [ ] 3.3 Move internal tests (`ParentStackTest`, `ReactiveContextTest`, `BindingTest`, etc.) into `:solim-runtime`
- [ ] 3.4 Verify `:solim-runtime:test` and `:solim-runtime:compileJava` pass cleanly

## 4. Framework Ownership & Core Adaptation (`:solim-core`)

- [ ] 4.1 Update imports in `BaseComponent.java` to `solim.runtime.*`
- [ ] 4.2 Change `BaseComponent.own()` from `public` to package-private
- [ ] 4.3 Reroute `ReactiveGrid.gap()` from `own(...)` to `ComponentContext.register(...)`
- [ ] 4.4 Update imports in `solim.UI` and other core components to `solim.runtime.*`
- [ ] 4.5 Verify `:solim-core:test` and `:solim:compileJava` pass cleanly

## 5. Mod Migration & Compile-Time Isolation Verification

- [ ] 5.1 Migrate `BrowserSearchHeader` manual `own(subscribe)` to declarative `effect()`
- [ ] 5.2 Migrate `TeamResourceHudView` manual `own()` to `listen()`/`effect()`
- [ ] 5.3 Migrate `ChatOverlayHudView` manual `own()` calls to `listen()`/`effect()`
- [ ] 5.4 Verify compile isolation: test that referencing `solim.runtime.ParentStack` in `:mod` fails `javac` compilation
- [ ] 5.5 Verify `:mod:compileJava` passes with zero manual `own()` calls and zero `solim.runtime` imports

## 6. Packaging, Documentation, and Guardrails

- [ ] 6.1 Update `mod/build.gradle` jar packaging tasks (`jar`, `jarAndroid`, `deploy`) to package all runtime artifacts
- [ ] 6.2 Add guard test ensuring no `:mod` code references `solim.runtime.*` or `own(`
- [ ] 6.3 Update `AGENTS.md` Solim UI section with Schema 1 module boundaries (`:solim-runtime` is forbidden in `:mod`)
- [ ] 6.4 Run complete build (`./gradlew check deploy`) and confirm green
