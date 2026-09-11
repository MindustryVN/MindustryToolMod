## Context

The Solim UI library was previously structured as a single Gradle subproject (`:solim`) containing both low-level internal abstractions (signal graph, reconciler, parent stack, concrete widgets) and public facade utilities (`solim.ui.Ui`). Consumers in `:mod` directly imported internal classes from `solim.*`.

To enforce clean architectural boundaries and provide a single, restricted entry point for mod developers, we are separating Solim into:
1. `:solim-core`: houses all internal engines, layout widgets, display components, reactive primitives, styles, overlays, and unit tests.
2. `:solim`: contains solely the public facade class `solim.UI` (`UI.java`).
3. `:mod`: consumes only `:solim`.

## Goals / Non-Goals

**Goals:**
- Create `:solim-core` subproject and move all current Solim source files (except `UI.java`) and tests into it.
- Restructure `:solim` subproject so its source tree contains exclusively `solim.UI` (`UI.java`).
- Extend `solim.UI` to encompass all user-permitted methods, including reactivity factories (`signal`, `computed`, `effect`), layouts, widgets, dialogs, overlays, units, and structural helpers.
- Configure `:solim` to export `:solim-core` via `api project(':solim-core')` using the `java-library` plugin so return types and modifiers are usable by callers.
- Ensure `:mod` only declares `implementation project(':solim')` (and `project(':solim-mcp')`), without any direct dependency on `:solim-core`.
- Adapt `:solim-mcp` to depend on `:solim-core` (and `:solim` if needed).
- Verify all unit tests and compilation across modules pass cleanly.

**Non-Goals:**
- Changing Solim's internal reactive model, rendering pipeline, or Mindustry integration.
- Rewriting business logic inside `mod`.
- Removing existing test suites in Solim.

## Decisions

### 1. Subproject Split: `:solim` and `:solim-core`
- **Choice**: Rename/split so that `solim-core/` holds the implementation and `solim/` holds the public API facade.
- **Alternatives considered**:
  - Keeping a single subproject with package-private restrictions: Rejected because package-private visibility does not work across packages in Java 8 without complex module paths or JPMS.
  - Creating a separate `:solim-api` module and keeping `:solim` as the core: Rejected because the user explicitly specified `:solim` as containing `UI.java` and `:solim-core` as containing all remaining files, with `mod` depending on `:solim`.

### 2. Dependency Propagation via `api`
- **Choice**: Apply `java-library` plugin to `:solim` and declare `api project(':solim-core')`.
- **Rationale**: Methods on `solim.UI` return concrete and generic types from `:solim-core` (such as `Column`, `Button`, `Signal<T>`, `Readable<T>`, `Component`). Without `api`, consumers in `:mod` would not be able to call chained modifiers (e.g. `.grow()`, `.gap()`) without adding `:solim-core` to their own dependencies.
- **Alternatives considered**:
  - `implementation project(':solim-core')` in `:solim`: Would require wrapping every single widget and return type in shadow interfaces in `:solim`, which introduces massive boilerplate and unnecessary complexity contrary to AGENTS.md rules.

### 3. Facade Class `solim.UI` (`UI.java`)
- **Choice**: Locate `UI.java` at `solim/src/solim/UI.java` in package `solim`.
- **Rationale**: Calling `import static solim.UI.*;` gives mod developers instant access to the entire curated Solim DSL.
- **Public API Coverage**:
  - Layout: `column`, `row`, `grid`, `card`, `scroll`, `stack`, `wrap`, `container`, `divider`, `spacer`
  - Display: `text`, `image`, `icon`, `networkImage`, `badge`, `badgeCount`
  - Input: `button`, `textField`, `slider`, `checkbox`
  - Overlays: `dialog`, `hud`
  - Reactivity: `signal(T)`, `computed(Supplier<T>)`, `effect(Runnable)`, `createSignal(...)`
  - Structural: `dynamic`, `forEach`, `component`, `add`
  - Units & Events: `unit`, `dvw`, `dvh`, `listen`

### 4. Migration Strategy for `:mod`
- **Choice**: Update `mod` import statements from `solim.ui.Ui` to `solim.UI`, and migrate direct signal/effect instantiations to `UI.signal(...)` / `UI.computed(...)` / `UI.effect(...)` or use the exposed core types.

## Risks / Trade-offs

- **[Risk] Broken imports across `mod` and `solim-mcp` during relocation**:
  - *Mitigation*: Move files in git, update package references, and run `./gradlew compileJava test` across all subprojects.
- **[Risk] Missing factory methods on `solim.UI` required by existing `mod` code**:
  - *Mitigation*: Audit all `solim.*` calls in `mod` before finalizing `UI.java` to ensure 100% method coverage.
