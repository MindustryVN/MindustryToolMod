# solim-dialog Specification

## Purpose
Declarative dialog component providing reactive signals, layout helpers, and automatic lifecycle management and disposal of attached content components and resources.
## Requirements
### Requirement: Automatic Content Component Lifecycle Management
The `SolimDialog` component SHALL manage content attachment declaratively via `children(Runnable)` using `ParentStack` and SHALL defer executing the content builder until the dialog is shown or unwrapped via `dialog()`. It SHALL automatically register any resources and components created within the content builder with its internal disposable registry. The legacy `content(Component)` method SHALL NOT be supported.

#### Scenario: Content builder is not executed on instantiation
- **WHEN** a `SolimDialog` is instantiated and configured with `children(Runnable contentBuilder)`
- **THEN** `contentBuilder` is not executed during instantiation
- **AND** no child elements or reactive bindings are created before the dialog is shown

#### Scenario: Content builder executed lazily when shown
- **WHEN** `show()` is invoked on a `SolimDialog` configured with `children(Runnable contentBuilder)`
- **THEN** `contentBuilder` is executed
- **AND** its child elements are attached to the dialog's content container via `ParentStack`

#### Scenario: Content builder executed when unwrapped
- **WHEN** `dialog()` is invoked on a `SolimDialog` configured with `children(Runnable contentBuilder)`
- **THEN** `contentBuilder` is executed before the wrapped dialog is returned
- **AND** showing the returned dialog displays the built content

#### Scenario: Content only built once across repeated shows
- **WHEN** `show()` is invoked multiple times on the same `SolimDialog`
- **THEN** `contentBuilder` is only executed once and existing elements are preserved

#### Scenario: Dialog disposal cleans up content components
- **WHEN** `dispose()` is invoked on a `SolimDialog` whose content was built
- **THEN** all attached components and resources are automatically disposed

### Requirement: Full Screen by Default
The `SolimDialog` component SHALL enable `fillParent(true)` by default to fill the entire viewport width and height, matching standard Mindustry dialog behavior, while allowing callers to override it via `fillParent(boolean)`.

#### Scenario: Default full screen sizing
- **WHEN** a `SolimDialog` is instantiated
- **THEN** `isFillParent()` returns `true` and the dialog expands to fill the entire scene

#### Scenario: Explicit non-fullscreen sizing
- **WHEN** a `SolimDialog` is configured with `fillParent(false)`
- **THEN** `isFillParent()` returns `false` and the dialog sizes according to its packed bounds

