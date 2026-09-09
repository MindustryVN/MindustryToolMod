## Context

Solim is a declarative UI framework for Mindustry mods built on top of Arc. It provides 27 component classes organized into layout, input, display, UI, and overlay packages. Currently there are no automated tests for these components. The `solim-test` skill provides a testing workflow that bootstraps a Mindustry runtime context for component testing.

Key components to test:
- **Core**: Component (interface), BaseComponent (abstract)
- **Layout** (12): Card, Column, Container, Divider, Grid, Row, Scroll, SolimStack, Spacer, Tabs, Wrap, ReactiveGrid
- **Input** (6): Button, Checkbox, SolimSelect, SolimSlider, SolimTextField, Switch
- **Display** (4): Text, SolimImage, NetworkImage, Badge
- **UI** (3): SettingsPanel, ForEach, Dynamic
- **Overlay** (2): SolimDialog, Hud

## Goals / Non-Goals

**Goals:**
- Achieve baseline test coverage for every Solim component
- Verify reactive binding behavior (signal → UI update)
- Verify lifecycle ownership and disposal
- Verify layout modifier methods (width, height, pad, grow, etc.)
- Verify children management and structural reactivity
- Use the `solim-test` skill workflow for consistent test structure

**Non-Goals:**
- Performance benchmarks
- Visual/screenshot tests
- Testing Mindustry engine internals
- Testing the solim-test skill itself
- Refactoring production components (tests only)

## Decisions

### 1. One test class per component
Each component gets its own `*Test.java` file. This keeps tests focused and makes failures easy to trace.

**Alternative considered**: Group related components in one test file. Rejected because it creates large, hard-to-maintain test classes.

### 2. Use solim-test skill workflow
Each component test task instructs the agent to load the `solim-test` skill before writing tests. This ensures consistent mocking, setup, and assertion patterns.

**Alternative considered**: Write a shared test base class. Rejected because the solim-test skill already encapsulates the testing pattern.

### 3. Test categories per component
Each component test covers:
1. **Construction** — component creates without error
2. **Static properties** — setting properties before/after build
3. **Reactive bindings** — Signal changes propagate to UI
4. **Disposal** — cleanup on remove
5. **Layout modifiers** — size, padding, growth, alignment
6. **Children** — adding/removing child components

### 4. No production code changes
Tests are purely additive. No modifications to existing Solim source files.

## Risks / Trade-offs

- **[Risk] Mindustry runtime mocking complexity** → Use the solim-test skill which handles runtime bootstrap. If mocking fails for a component, skip that test and document the limitation.
- **[Risk] Test maintenance burden** → Keep tests simple and focused on public API. Avoid testing implementation details.
- **[Risk] Component dependencies on Arc internals** → Some components (NetworkImage, SolimSelect) depend on Arc APIs. Mock at the boundary, not inside Arc.
