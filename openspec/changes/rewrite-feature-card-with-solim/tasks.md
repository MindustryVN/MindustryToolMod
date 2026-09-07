## 1. Solim Widget and Component Enhancements

- [x] 1.1 Enrich `Text` component with fluent chained property modifiers (`.color(Readable<Color>)`, `.wrap(boolean)`, `.ellipsis(boolean)`, `.style(LabelStyle)`, `.left()`, `.center()`) with internal reactive binding ownership.
- [x] 1.2 Implement `IconButton` component in `solim.input` with icon drawable, `ImageButtonStyle`, sizing, tooltips, and click event bubbling isolation (`stopClickPropagation`).
- [x] 1.3 Add `iconButton(...)` declarative facades to `solim.ui.Ui`.

## 2. Solim Card Component

- [x] 2.1 Implement `Card` in `solim.layout` with Arc `Button` base, inner container `Table`, and `ParentStack` child attachment integration.
- [x] 2.2 Add fluent chained property bindings to `Card` (`.width(Readable<Float>)`, `.width(float)`, `.height(Readable<Float>)`, `.height(float)`, `.prefHeight(float)`, `.color(Readable<Color>)`, `.color(Color)`, `.style(ButtonStyle)`, `.padding(float)`, `.onClick(Runnable)`, `.name(String)`).
- [x] 2.3 Add `card(...)` declarative facades to `solim.ui.Ui`.

## 3. Rewrite FeatureCard with Solim Components

- [x] 3.1 Refactor `FeatureCard.java` to use declarative Solim components (`card`, `row`, `image`, `text`, `iconButton`, `spacer`), eliminating raw Scene2D element construction.
- [x] 3.2 Replace all external `Binding.bindX(...)` calls in `FeatureCard` with chained reactive component modifiers (`.width(...)`, `.color(...)`, `text(statusText)`).
- [x] 3.3 Ensure dialog action triggers (main dialog, settings dialog, help dialog) properly stop event propagation without triggering card toggle.

## 4. Testing and Verification

- [x] 4.1 Add unit tests for `Card` component and chained property bindings in `solim`.
- [x] 4.2 Add unit tests for `IconButton` component and event propagation in `solim`.
- [x] 4.3 Verify `FeatureCard` rendering, toggle behavior, and interactions with existing test suite and `gradle build`.
