## 1. Solim Layout Containers

- [x] 1.1 Remove default `growX` from `Column.ATTACHER` so child elements do not grow unless explicitly set via `SizeConstraints` or `Ui.isExpanding`
- [x] 1.2 Remove default `growX` for `TextField` in `Row.ATTACHER`
- [x] 1.3 Remove default `growX` from `Card.ATTACHER`
- [x] 1.4 Remove default `growX` from `Scroll.ATTACHER`
- [x] 1.5 Update `ForEach` and `Dynamic` to avoid forcing `.growX()` or `.grow()` on child components

## 2. Solim Unit Tests

- [x] 2.1 Add unit tests in `LayoutTest` asserting that children in `Column`, `Row`, `Card`, and `Scroll` do not grow by default
- [x] 2.2 Verify explicit `.grow()`, `.growX()`, and `.growY()` modifiers correctly expand cells
- [x] 2.3 Verify `Spacer` expansion behavior remains intact in `Row` and `Column`

## 3. Mod UI Updates

- [x] 3.1 Update `FeatureSettingsView` toolbar to add explicit `.growX()` to the search text field
- [x] 3.2 Update `FeatureCard` layout to add explicit `.growX()` to rows and elements intended to span the card width
- [x] 3.3 Check and update any other mod views needing explicit `.growX()`

## 4. Verification

- [x] 4.1 Run `./gradlew test` to verify all Solim layout tests pass
- [x] 4.2 Run `./gradlew jar` to ensure both Solim and mod compile without errors
