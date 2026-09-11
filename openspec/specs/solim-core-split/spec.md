# solim-core-split Specification

## Purpose
Specifies the architectural separation of the Solim UI framework into `:solim-core` (internal framework engine) and `:solim` (public API facade).

## Requirements
### Requirement: Solim Core Module Separation
The Solim UI framework SHALL be split into two Gradle subprojects: `:solim-core` and `:solim`.
`:solim-core` SHALL contain all internal reactive engines, component base classes, layout implementations, widget primitives, style definitions, overlay managers, unit conversions, and structural reconcilers.
`:solim` SHALL contain solely the user-facing public API facade class `solim.UI` (`UI.java`).

#### Scenario: Build solim-core independently
- **WHEN** `:solim-core:build` is executed
- **THEN** all core Solim implementation classes and tests compile and pass without relying on `:solim`

#### Scenario: Solim facade module contains only UI.java
- **WHEN** the source directory of `:solim` is inspected
- **THEN** only `solim.UI` (`UI.java`) is present under its source directory

### Requirement: Mod Dependency Constraint
The `:mod` Gradle subproject SHALL depend exclusively on `:solim` for Solim UI functionality and SHALL NOT declare a direct dependency on `:solim-core`. All UI component instantiation and declarative operations in `:mod` SHALL use `solim.UI`.

#### Scenario: Mod build dependencies
- **WHEN** `mod/build.gradle` dependencies are inspected
- **THEN** `project(':solim')` is declared as an implementation dependency and `project(':solim-core')` is not present

#### Scenario: Mod compiles without direct solim-core dependency
- **WHEN** `:mod:compileJava` is executed
- **THEN** compilation succeeds using only `project(':solim')` without compile-time errors

### Requirement: Complete Public UI Facade
`solim.UI` in `:solim` SHALL provide all user-permitted static factory methods for Solim UI operations. This SHALL include:
- Layouts: `column()`, `row()`, `grid()`, `card()`, `scroll()`, `stack()`, `wrap()`, `container()`, `divider()`, `spacer()`
- Widgets: `button()`, `text()`, `textField()`, `slider()`, `checkbox()`, `image()`, `icon()`, `networkImage()`, `badge()`, `badgeCount()`
- Overlays: `dialog()`, `hud()`
- Reactivity: `signal()`, `computed()`, `effect()`, `createSignal()`
- Structural & Components: `dynamic()`, `forEach()`, `component()`
- Units & Events: `unit()`, `dvw()`, `dvh()`, `listen()`
Internal helpers such as `isExpanding`, `element`, and `add` SHALL NOT be exposed on `solim.UI`.

#### Scenario: UI methods instantiation
- **WHEN** UI elements, signals, layouts, or widgets are created in user code
- **THEN** all methods are accessible statically via `solim.UI`

#### Scenario: Reactivity factory methods on UI
- **WHEN** a user creates a signal or computed value via `UI.signal(value)` or `UI.computed(supplier)`
- **THEN** the corresponding `Signal<T>` or `Computed<T>` instance from `solim-core` is instantiated and returned

### Requirement: API Dependency Propagation
The `:solim` subproject SHALL declare an `api` dependency on `:solim-core` using the `java-library` plugin (or equivalent `api` configuration) so that public return types and chained modifier methods (e.g. `Column`, `Button`, `Signal<T>`, `Readable<T>`, `Component`) are accessible to consumer modules without consumers having to declare `:solim-core` directly.

#### Scenario: Chained modifiers in consumer
- **WHEN** a consumer in `:mod` calls `UI.column().grow().gap(UI.unit(2))`
- **THEN** the chained methods compile cleanly on the consumer classpath
