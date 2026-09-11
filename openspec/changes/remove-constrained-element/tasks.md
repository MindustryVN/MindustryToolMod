## 1. Remove ConstrainedElement and Core Attach Mechanics

- [x] 1.1 Remove `instanceof ConstrainedElement` checks and constraint application logic from `solim.ui.ParentStack`
- [x] 1.2 Remove `instanceof ConstrainedElement`, `SizedButton`, and `SizedImage` checks from `solim.modifier.ElementModifiers`
- [x] 1.3 Delete `solim.layout.ConstrainedElement.java`
- [x] 1.4 Refactor `solim.layout.LayoutModifiers` and `solim.layout.SizeConstraints` to remove `ConstrainedElement` dependencies and configure native cells/elements

## 2. Refactor Solim Input and Display Components

- [x] 2.1 Refactor `Button.java` to wrap `arc.scene.ui.Button` directly and remove `SizedButton`
- [x] 2.2 Refactor `Text.java` to wrap `arc.scene.ui.Label` directly and remove `SizedLabel`
- [x] 2.3 Refactor `SolimImage.java` to wrap `arc.scene.ui.Image` directly and remove `SizedImage`
- [x] 2.4 Refactor `SolimTextField.java` to wrap `arc.scene.ui.TextField` directly and remove `SizedTextField`
- [x] 2.5 Refactor `Checkbox.java` to wrap `arc.scene.ui.CheckBox` directly and remove `SizedCheckBox`

## 3. Refactor Layout Containers and Structural Components

- [x] 3.1 Replace `SizedTable` in `Row.java`, `Column.java`, `Grid.java`, and `Scroll.java` with standard `arc.scene.ui.layout.Table`
- [x] 3.2 Refactor `Card.java` to wrap standard `arc.scene.ui.Button` and remove `CardButton`
- [x] 3.3 Replace `SizedTable` and remove `ConstrainedElement` checks/implementation from `Dynamic.java`, `ForEach.java`, and `ReactiveGrid.java`
- [x] 3.4 Delete `solim.layout.SizedTable.java`

## 4. Update Mod Code and Verification

- [x] 4.1 Remove `ConstrainedElement` from `mindustrytool.features.teamresource.SplitBar` in `mod`
- [x] 4.2 Update test references and comments in `SnapshotIntrospectionTest.java`
- [x] 4.3 Update unit tests in `solim` (`LayoutTest`, `ButtonTest`, etc.) to align with native Arc wrapping
- [x] 4.4 Run `./gradlew :solim:test` and `./gradlew test` to ensure all tests pass
