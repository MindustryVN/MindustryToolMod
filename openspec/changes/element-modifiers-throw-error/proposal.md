## Why

`ElementModifiers` currently accepts `@Nullable Element` and `@Nullable Table` across all modifier methods and silently returns without doing anything when given null or incompatible elements. This silent failure masks bugs such as uninitialized components, lifecycle timing errors, or invalid element hierarchy setups. Enforcing fail-fast validations ensures developer errors surface immediately with clear exceptions.

## What Changes

- **BREAKING**: Modify `ElementModifiers` to reject null `Element` and `Table` arguments with `NullPointerException` instead of returning silently.
- **BREAKING**: In `ElementModifiers.gap(Element element, float gap)`, reject non-`Table` elements with `IllegalArgumentException` instead of silently ignoring the call.
- Update method signatures in `ElementModifiers` from `@Nullable Element`/`@Nullable Table` to non-null parameters, matching project guidelines that values are non-nullable by default.
- Update tests in `solim` to verify that `ElementModifiers` methods throw appropriate exceptions when given null or incompatible arguments.

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `solim-shared-modifiers`: Change `ElementModifiers` behavior to fail fast with explicit errors (`NullPointerException` on null elements, `IllegalArgumentException` on invalid element types) instead of silently no-opping.

## Impact

- `solim.modifier.ElementModifiers`: Remove silent null checks; add explicit `Objects.requireNonNull` and type checks.
- Any caller passing `null` to `ElementModifiers` methods will now encounter a `NullPointerException` rather than a silent no-op.
- Tests in `solim` testing null-safety of `ElementModifiers` will be updated to assert exceptions instead of silent no-ops.
