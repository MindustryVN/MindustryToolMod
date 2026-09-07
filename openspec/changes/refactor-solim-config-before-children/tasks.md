## 1. Solim Core DSL Refactoring

- [x] 1.1 Add `.children(Runnable)` method and deferred attachment handling to `solim.layout.Column`.
- [x] 1.2 Add `.children(Runnable)` method and deferred attachment handling to `solim.layout.Row`.
- [x] 1.3 Add `.children(Runnable)` method and deferred attachment handling to `solim.layout.Scroll`.
- [x] 1.4 Add `.children(Runnable)` method and deferred attachment handling to `solim.layout.Card`.
- [x] 1.5 Add `.children(Runnable)` method and deferred attachment handling to `solim.overlay.SolimDialog`.
- [x] 1.6 Update `solim.ui.Ui` facade with parameterless static builder methods (`column()`, `row()`, `scroll()`, `card()`, `grid()`, `container()`, `stack()`, `wrap()`, `dialog()`).

## 2. Solim Unit Tests Migration

- [x] 2.1 Migrate layout tests in `solim/src/test/java/solim/layout/LayoutTest.java` to configuration-before-children style.
- [x] 2.2 Migrate parent stack tests in `solim/src/test/java/solim/ui/ParentStackTest.java` to configuration-before-children style.
- [x] 2.3 Migrate reactivity and component tests in `solim/src/test/java/solim/ui/StructuralReactivityTest.java` and `solim/src/test/java/solim/core/ComponentTest.java`.

## 3. MindustryTool Mod Solim UI Migration

- [x] 3.1 Migrate `FeatureSettingsView.java` and `FeatureHelpView.java` to configuration-before-children DSL style.
- [x] 3.2 Migrate `FeatureCard.java` to configuration-before-children DSL style.
- [x] 3.3 Migrate `BackgroundSettingsDialog.java`, `CrashReportDialog.java`, `UpdateDialog.java`, `AuthOverlay.java`, and `SettingsPanel.java`.

## 4. Verification

- [x] 4.1 Run unit test suite via Gradle to verify build and zero runtime or layout regressions.
- [x] 4.2 Audit codebase to ensure no legacy `component(() -> { ... })` call sites remain.
