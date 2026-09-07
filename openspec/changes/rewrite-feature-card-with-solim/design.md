## Context

`FeatureCard` in `mod/src/mindustrytool/features/settings/FeatureCard.java` currently builds its UI imperatively by instantiating Arc Scene2D objects (`Button`, `Table`, `Label`, `Image`) and calling static methods from `solim.ui.Binding` (`Binding.bindWidth`, `Binding.bindColor`, `Binding.bindText`). This violates Solim's declarative paradigm and the core principle that reactive property bindings must be owned by the components themselves via chained methods.

Furthermore, Solim currently lacks a dedicated `Card` container component and an `IconButton` component, forcing feature developers to write manual Arc Scene2D code whenever cards or icon buttons are needed.

## Goals / Non-Goals

**Goals:**
- Provide a reusable `Card` container component in `solim.layout` with `Ui.card(...)` facades that integrates with `ParentStack`, provides click handling, and supports fluent chained property bindings (`.width()`, `.height()`, `.prefHeight()`, `.color()`, `.style()`, `.padding()`, `.onClick()`).
- Provide a reusable `IconButton` widget in `solim.input` with `Ui.iconButton(...)` facades that wraps Arc `ImageButton`, accepts `ImageButtonStyle` (such as `Styles.clearNonei`), supports sizing, tooltips, and click event isolation (`stopClickPropagation`).
- Enrich `Text` with fluent modifiers: `.color(Readable<Color>)`, `.wrap(boolean)`, `.ellipsis(boolean)`, `.style(LabelStyle)`, and alignment (`.left()`, `.center()`).
- Rewrite `FeatureCard.java` using purely Solim declarative components (`card`, `column`, `row`, `image`/`icon`, `text`, `iconButton`, `spacer`), eliminating raw Scene2D table manipulation and external `Binding.bindX(...)` calls.
- Ensure strict Java 8 runtime compatibility and complete internationalization compliance.

**Non-Goals:**
- Modifying `FeatureSettingsView` or other callers; `FeatureCard`'s public interface (`enabled()`, `cardWidth()`, `element()`, constructors) remains intact.
- Replacing or removing `solim.ui.Binding` utility class; existing static methods remain available for legacy code, but new code uses chained component methods.

## Decisions

### Decision 1: `Card` Component Architecture
- **Choice**: Implement `Card` in `solim.layout` wrapping an Arc `Button` with an inner content container `Table`.
- **Rationale**: Wrapping `Button` gives native touch response, hover states, and draw styling (`Styles.black8`). The inner `Table` hosts declarative children pushed onto `ParentStack`.
- **Fluent API**:
  - `.width(Readable<Float>)`, `.width(float)`
  - `.height(Readable<Float>)`, `.height(float)`
  - `.prefHeight(float)`
  - `.color(Readable<Color>)`, `.color(Color)`
  - `.style(ButtonStyle)`
  - `.padding(float)`
  - `.onClick(Runnable)`
  - `.name(String)`
- **Alternatives Considered**: Writing card logic as a one-off private helper inside `FeatureCard.java`. Rejected because cards are standard layout elements used across settings, dialogs, and feature browsers.

### Decision 2: `IconButton` Component Architecture
- **Choice**: Implement `IconButton` in `solim.input` wrapping Arc `ImageButton`.
- **Rationale**: Mindustry provides `ImageButton.ImageButtonStyle` (e.g. `Styles.clearNonei`) which is distinct from `TextButton.TextButtonStyle`. An `ImageButton` wrapper provides native support for Mindustry icon styles, sizing, and tooltips.
- **Fluent API**:
  - `Ui.iconButton(Drawable icon, Runnable onClick)`
  - `Ui.iconButton(Drawable icon, ImageButtonStyle style, Runnable onClick)`
  - `.size(float)`
  - `.tooltip(String)` / `.tooltip(Readable<String>)`
  - `.stopClickPropagation(boolean)` (defaults to `true`)
- **Alternatives Considered**: Adding icon support to `Button` (`TextButton`). Rejected because `ImageButton` and `TextButton` have incompatible style classes in Arc (`ImageButtonStyle` vs `TextButtonStyle`), and `Styles.clearNonei` is an `ImageButtonStyle`.

### Decision 3: Component-Owned Chained Reactive Bindings
- **Choice**: Reactive bindings are created directly inside component methods (e.g., `text.color(Readable<Color>)`, `card.width(Readable<Float>)`) and automatically registered via `ComponentContext.register(effect)` or stored in the component's internal disposable list.
- **Rationale**: Keeps application code clean and declarative (`text(status).color(colorSignal)` instead of `Binding.bindColor(label, colorSignal)`). Components own their subscriptions and clean up upon disposal.

### Decision 4: Event Bubbling Isolation
- **Choice**: `IconButton` automatically invokes `event.stop()` upon click events when `stopClickPropagation` is enabled (default `true`).
- **Rationale**: When an `IconButton` (such as settings or help) is placed inside a clickable `Card`, clicking the button must not bubble up to trigger the card's feature toggle action.

## Risks / Trade-offs

- **[Risk] Nested child click bubbling**: Clicking an icon button inside a clickable card might trigger the card's `onClick`.
  → **Mitigation**: `IconButton` stops click event propagation (`event.stop()`), ensuring only the inner button's action fires.
- **[Risk] Layout cell expansion in Card container**: Children added to `Card` might not stretch or align as intended.
  → **Mitigation**: `Card` uses a `Column`-like layout attacher (`cell.growX(); cell.row();`) with configurable padding, matching `FeatureCard`'s container specifications.
- **[Risk] Java 8 runtime incompatibility**: Using newer Java collections or APIs.
  → **Mitigation**: Adhere to project rules: pure Java 8 runtime constructs, no `List.of`, no `Optional.isEmpty`, no `stream.toList()`.
