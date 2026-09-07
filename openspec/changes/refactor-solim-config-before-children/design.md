## Context

In the current Solim UI library architecture, container components (such as `Column`, `Row`, `Scroll`, `Card`, `SolimDialog`, `Grid`, `SolimStack`, `Wrap`, `Container`) accept child builder lambdas (`Runnable`) directly during instantiation (e.g. `column(() -> { ... })`), returning the container instance or element for post-configuration chaining (e.g. `column(() -> { ... }).grow()`).

When UI trees grow large, configuration calls like `.grow()`, `.padding(...)`, and `.gap(...)` end up at the very bottom of long child blocks. This makes layout properties hard to associate with their owning components.

To solve this, we are refactoring Solim UI so that component configuration is declared **before** its children (e.g. `column().grow().padding(10f).children(() -> { ... })`).

## Goals / Non-Goals

**Goals:**
- Provide parameterless builder factory overloads in `Ui` (e.g. `column()`, `row()`, `scroll()`, `card()`, `grid()`, `container()`, `stack()`, `wrap()`, `dialog()`).
- Implement fluent `.children(Runnable)` methods on layout container classes (`Column`, `Row`, `Scroll`, `Card`, `SolimStack`, `SolimDialog`, etc.).
- Maintain exact declarative tree semantics, reactive binding, component cleanup, and `ParentStack` child-attacher mechanisms.
- Migrate all internal Solim UI usages in the codebase to configuration-before-children syntax.

**Non-Goals:**
- Redesigning Solim to an imperative manual tree API (e.g., `var col = new Column(); parent.add(col)`).
- Altering core reactive signal, effect, or event mechanics.

## Decisions

### Decision 1: Fluent `.children(Runnable)` Method Pattern
Each container component class (`Column`, `Row`, `Scroll`, `Card`, `SolimStack`, `SolimDialog`, etc.) will provide a `.children(Runnable r)` method.
When `.children(r)` is called:
1. It pushes the container's inner target `Table` and custom `Attacher` onto `ParentStack`.
2. It executes `r.run()` inside a `try/finally` block.
3. It pops `ParentStack`.
4. It calls `ParentStack.attachToParent(element())` to attach the container element to the outer active parent container.

*Rationale*:
Deferring `ParentStack.attachToParent(...)` until `.children(...)` (or `.element()`) is called ensures that all preceding modifier calls (such as `.grow()`, `.padding()`, `.align()`) are applied to the container before the outer parent container attacher inspects the child (for instance, via `isExpanding(child)`).

### Decision 2: Factory Overloads in `Ui` Facade
`solim.ui.Ui` will introduce static factory methods:
- `public static Column column()`
- `public static Row row()`
- `public static Scroll scroll()`
- `public static Card card()` / `public static Card card(Drawable bg)` / `public static Card card(ButtonStyle style)`
- `public static Table container()`
- `public static Table stack()`
- `public static Table wrap()`
- `public static Table grid(int columns)`
- `public static SolimDialog dialog(String title)`

Legacy lambda-taking overloads (e.g. `column(Runnable)`) will be updated or removed after clean migration across the codebase.

### Decision 3: Clean Repository Migration
All existing UI view classes (`FeatureSettingsView`, `FeatureHelpView`, `FeatureCard`, `BackgroundSettingsDialog`, `CrashReportDialog`, `UpdateDialog`, `AuthOverlay`, etc.) and all test files will be updated to use the new fluent `component().modifier().children(() -> { ... })` syntax.

## Risks / Trade-offs

- **[Risk] Double-attachment if both `.children()` and `.element()` or implicit attach are called.**
  → *Mitigation*: Track an `attached` state flag inside containers or guard `ParentStack.attachToParent` to ensure container element is added to parent `Table` exactly once.

- **[Risk] Missed migration of old-style lambda builders in tests or auxiliary classes.**
  → *Mitigation*: Run comprehensive grep search across repository for all container calls and update test suites. Verify zero compilation errors with `gradlew test` / `gradlew build`.
