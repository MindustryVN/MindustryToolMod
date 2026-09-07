## Why

Arc UI relies on `Element.name` for identification, debugging, UI tree inspection, and querying (such as `find(name)`). Solim components currently lack a standard fluent `.name(String)` modifier, forcing developers to access the underlying Arc element imperatively or preventing named inspection altogether. Adding fluent `.name(String)` to all Solim components allows clean declarative naming of elements.

## What Changes

- Add `ElementModifiers.name(@Nullable Element element, @Nullable String name)` static utility.
- Add `default Component name(String name)` method to the `Component` interface.
- Implement/expose fluent `.name(String name)` returning `this` on all Solim components:
  - Layouts: `Row`, `Column`, `Card`, `Scroll`, `Grid`, `Container`, `Divider`, `Spacer`, `SolimStack`, `Wrap`
  - Input widgets: `Button`, `Checkbox`, `SolimSlider`, `SolimTextField`, `Switch`, `SolimSelect`
  - Display widgets: `Text`, `SolimImage`
  - Overlay: `SolimDialog`
- Ensure all input widgets (`Checkbox`, `SolimSlider`, `Switch`, `SolimSelect`) implement `Component`.

## Capabilities

### New Capabilities

### Modified Capabilities
- `solim-shared-modifiers`: Add element naming utility to `ElementModifiers` and fluent `.name(String)` modifier support across Solim components.
- `solim-component`: Add `default Component name(String name)` to `Component` interface and ensure all Solim widgets implement `Component`.

## Impact

- `solim`: `solim.core.Component`, `solim.modifier.ElementModifiers`, layout components (`Row`, `Column`, `Card`, `Scroll`, `Grid`, `Container`, `Divider`, `Spacer`, `SolimStack`, `Wrap`), input components (`Button`, `Checkbox`, `SolimSlider`, `SolimTextField`, `Switch`, `SolimSelect`), display components (`Text`, `SolimImage`), and overlay components (`SolimDialog`).
- Backwards compatible: No breaking changes to existing methods or signatures.
