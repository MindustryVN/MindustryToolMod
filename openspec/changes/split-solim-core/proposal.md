## Why

Currently, the `solim` library mixes core internal components, reactive engines, widgets, and layout primitives together with its public entry point in a single module, allowing consumer modules like `mod` to directly access and depend on internal implementation details. Splitting `solim` into a dedicated public facade module (`solim`) and an internal engine module (`solim-core`) enforces clear architectural boundaries, guarantees that consumer mods only interact through the curated `UI.java` API, and prevents unauthorized internal coupling.

## What Changes

- **NEW MODULE**: Create `solim-core` Gradle subproject containing all underlying Solim implementations (signals, layouts, display, input, modifiers, styles, overlays, parent stack, and internal lifecycle reconcilers).
- **REFACTOR**: Streamline `solim` Gradle subproject to contain solely the public entry point `UI.java` in package `solim`.
- **API EXPANSION**: Expand `solim.UI` to encompass all approved user-facing factory and lifecycle methods (including signals, computed, effects, layouts, widgets, dialogs, overlays, units, and structural builders).
- **RESTRICTION**: Restrict `mod` dependencies so it only depends on `:solim` (never `:solim-core`).
- **MCP SERVER ADAPTATION**: Update `:solim-mcp` to reference `:solim-core` / `:solim` as appropriate.
- **BREAKING**: In `mod`, update imports from `solim.ui.Ui` and direct `solim.*` internal packages to use `solim.UI` (or public exported types via `:solim`).

## Capabilities

### New Capabilities
- `solim-core-split`: Architectural separation of Solim into `solim-core` (internal engine & primitives) and `solim` (user-facing public `UI.java` facade), with enforced module-level dependency constraints on `mod`.

### Modified Capabilities
<!-- None -->

## Impact

- **Gradle configuration**: `settings.gradle`, `build.gradle`, `solim/build.gradle`, `mod/build.gradle`, and new `solim-core/build.gradle`.
- **Source trees**: Relocate core classes and tests from `solim/src` to `solim-core/src`; keep only `solim.UI` in `solim/src`.
- **Consumer code**: `mod` views and dialogs migrated to use the centralized `solim.UI` API.
