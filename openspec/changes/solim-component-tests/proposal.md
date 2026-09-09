## Why

The Solim UI framework has 27 component classes across layout, input, display, UI, and overlay packages, but lacks comprehensive test coverage. Without tests, regressions in component behavior, reactive bindings, lifecycle ownership, and layout mechanics go undetected. Adding tests now ensures correctness before the framework is used in more features.

## What Changes

- Add unit tests for all 27 Solim component classes
- Each component gets a dedicated test file covering: construction, property binding, reactive updates, disposal, and layout behavior
- Tests use the `solim-test` skill for consistent structure and Mindustry runtime mocking
- Test files follow the pattern `solim/test/<package>/<Component>Test.java`

## Capabilities

### New Capabilities
- `solim-core-tests`: Tests for Component interface and BaseComponent abstract class
- `solim-layout-tests`: Tests for Column, Row, Grid, Card, Container, Divider, Scroll, Spacer, Tabs, Wrap, SolimStack, ReactiveGrid
- `solim-input-tests`: Tests for Button, Checkbox, SolimSelect, SolimSlider, SolimTextField, Switch
- `solim-display-tests`: Tests for Text, SolimImage, NetworkImage, Badge (display)
- `solim-ui-tests`: Tests for SettingsPanel, ForEach, Dynamic
- `solim-overlay-tests`: Tests for SolimDialog, Hud

### Modified Capabilities

(none — this is purely additive test coverage)

## Impact

- New test files in `solim/test/` directory
- No changes to production code
- No API or dependency changes
- Tests require Mindustry runtime initialization (headless or mocked)
