## Why

`FeatureSettingDialog` manages the discovery, toggling, and configuration of mod features. Its current implementation is built using imperative Mindustry Arc UI constructs with manual rebuild passes (`rebuild()`, `rebuildPane()`), ad-hoc resize listeners, and explicit event bus subscription handling.

With the introduction of the Solim reactive UI toolkit (`:solim`), rewriting `FeatureSettingDialog` establishes a modern, declarative UI architecture. This enables reactive filter binding via signals, automatic UI reconciliation on feature state changes, and lifecycle-aware cleanup of event subscriptions and effects, reducing UI boilerplate and eliminating manual state synchronization bugs.

## What Changes

- **Reactive State Management**: Migrate filter text, matching features list, and layout dimensions (column count / responsive sizing) to Solim reactive primitives (`Signal<String>`, `Computed<Seq<Feature>>`, `Effect`).
- **Declarative Dialog Layout**: Re-implement dialog structure and toolbar (search field, re-enable button, footer actions) using Solim layout and widget primitives (`Ui.column`, `Ui.row`, `SolimTextField`, `Button`).
- **Reactive Feature Cards**: Adapt or integrate feature card rendering with Solim reactive state, reacting to `FeatureStateChanged` events and local toggle events without full imperative pane destruction where appropriate.
- **Resource & Lifecycle Disposal**: Connect the dialog lifecycle with Solim `Disposable` / `Effect` cleanup so all event listeners and reactive subscriptions are cleanly torn down when dialog is dismissed.
- **Strict AGENTS.md Compliance**: Ensure all user-visible strings continue to use `Core.bundle`, all comments and i18n are preserved, and Java 8 runtime compatibility is strictly respected.

## Capabilities

### New Capabilities
- `feature-settings-dialog`: Reactive feature settings dialog built using Solim reactive signals and declarative layout components.

### Modified Capabilities
<!-- No requirement changes to existing capability specs in openspec/specs/. -->

## Impact

- **Modified Files**:
  - `mod/src/mindustrytool/features/settings/FeatureSettingDialog.java` (complete Solim rewrite)
  - `mod/src/mindustrytool/features/settings/FeatureCard.java` (adapted for Solim reactive bindings if needed)
- **Module Dependencies**: Depends on `:solim` module, which is already configured in `mod/build.gradle`.
- **Runtime Environment**: Java 8 runtime compatible (no Java 9+ APIs).
- **Localization**: Maintains full coverage in `assets/bundles/bundle.properties`.
