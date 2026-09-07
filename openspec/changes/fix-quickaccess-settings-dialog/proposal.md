## Why

`QuickAccessSettingsDialog` currently contains imperative `.subscribe()` calls for slider and checkbox bindings and unnecessary single-use local variables declared at the constructor head. Additionally, layouts should consistently use Solim's declarative method chaining style (`row().gap(...).children(...)`) rather than parameter-based configurations. Supporting integer signals and callback bindings in Solim input widgets enables a clean, fully declarative dialog implementation without manual subscriptions or single-use variables.

## What Changes

- Ensure `Row` continues to support declarative chaining for modifiers (`row().gap(...).children(...)`) in accordance with Solim guidelines.
- Add integer slider support in `SolimSlider` and `Ui.slider(Signal<Integer>, int, int, int)`.
- Add callback support to `Checkbox` and `Ui.checkbox(String, boolean, Consumer<Boolean>)`.
- Refactor `QuickAccessSettingsDialog` to remove all manual `.subscribe()` calls, eliminate single-use local variable declarations by inlining declarative bindings, and maintain clean chained `row().gap(...).children(...)` layouts.

## Capabilities

### New Capabilities

### Modified Capabilities
- `solim-widgets`: Support `Signal<Integer>` in `SolimSlider` and callback-based `checkbox` overload in `Checkbox` / `Ui`.
- `quick-access`: Settings dialog uses declarative chained `row().gap(...).children(...)` layout, direct integer slider binding, and callback checkbox without manual subscriptions or single-use variables.

## Impact

- `solim`: `solim.input.SolimSlider`, `solim.input.Checkbox`, `solim.ui.Ui`.
- `mod`: `mindustrytool.features.quickaccess.QuickAccessSettingsDialog`.
- Backward compatible: All existing Solim widget signatures and chaining methods remain intact.
