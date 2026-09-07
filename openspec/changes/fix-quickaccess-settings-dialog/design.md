## Context

`QuickAccessSettingsDialog` was implemented with imperative `.subscribe()` listeners to synchronize slider and checkbox values with `QuickAccessFeature` config options. In addition, several single-use local variables (`opacitySignal`, `scaleSignal`, `colsIntSignal`, `colsFloatSignal`, `opacityLabelText`, `scaleLabelText`, `colsLabelText`) were declared at the top of the constructor instead of being cleanly declared or inlined in the component hierarchy.

Furthermore, following Solim guidelines in `AGENTS.md`, layout components must use fluent method chaining (`row().gap(...).children(...)`) where children and modifiers are chained rather than passed as constructor or factory parameters.

## Goals / Non-Goals

**Goals:**
- Adhere strictly to Solim declarative chaining (`row().gap(...).children(...)`).
- Support integer signals in `SolimSlider` and `Ui.slider(Signal<Integer>, int, int, int)`.
- Support callback consumer in `Checkbox` and `Ui.checkbox(String, boolean, Consumer<Boolean>)`.
- Refactor `QuickAccessSettingsDialog` to inline label text bindings, bind integer cols directly to the slider, bind feature checkboxes directly with callbacks, and eliminate all manual `.subscribe()` calls and single-use local variables.

**Non-Goals:**
- Introduce parameter-based layout factories like `row(gap, children)`; all layout configurations remain method chained.
- Change the underlying storage format of `QuickAccessFeature` settings.

## Decisions

### 1. Fluent Method Chaining for Layouts
- **Decision**: Layout components like `Row` use method chaining for modifiers and children:
  ```java
  row()
      .gap(unit(2))
      .children(() -> {
          ...
      });
  ```
- **Rationale**: Follows Solim's core architectural guideline in `AGENTS.md` ("Modifier Order: 1. Create component 2. Size/growth 3. Spacing 4. Alignment 5. Styling 6. Behavior 7. `children()` last"). Chaining keeps layout properties composable and uniform across all Solim containers (`Row`, `Column`, `Card`, etc.).

### 2. Direct Integer Support in `SolimSlider`
- **Decision**: Add an overloaded constructor and static factory in `SolimSlider` (and facade in `Ui`):
  - `public SolimSlider(Signal<Integer> signal, int min, int max, int step)`
  - `public static SolimSlider slider(Signal<Integer> signal, int min, int max, int step)`
  This bidirectional slider updates the integer signal when the slider changes and synchronizes the slider value when the signal updates.
- **Rationale**: Avoids creating a duplicate float signal and calling `.subscribe()` just to bridge between float slider controls and integer configs.

### 3. Callback Overload for `Checkbox`
- **Decision**: Add an overloaded constructor in `Checkbox` (and facade in `Ui`):
  - `public Checkbox(String label, boolean initial, Consumer<Boolean> onChanged)`
  - `public static Checkbox checkbox(String label, boolean initial, Consumer<Boolean> onChanged)`
- **Rationale**: Feature visibility in `QuickAccessFeature` is toggled per feature id (`setFeatureVisible(id, visible)`), which updates a set config. A callback-based checkbox avoids creating temporary detached `Signal<Boolean>` instances with manual `.subscribe()` calls.

### 4. Inlining Single-Use Variables in `QuickAccessSettingsDialog`
- **Decision**: Remove local variable declarations at the constructor head (`opacitySignal`, `scaleSignal`, `colsIntSignal`, `colsFloatSignal`, `opacityLabelText`, `scaleLabelText`, `colsLabelText`) and supply signals and mapped computeds directly in the declarative Solim hierarchy.
- **Rationale**: Conforms to Solim's declarative guidelines ("UI should describe what the UI is... preserve readability through nested declarative children blocks rather than introducing unnecessary temporary variables").

## Risks / Trade-offs

- [Risk] Slider step resolution for integers: casting float slider value to integer could cause rounding drift if step is fractional.
  - *Mitigation*: Slider for integer uses integer step sizes and `Math.round(slider.getValue())`.
