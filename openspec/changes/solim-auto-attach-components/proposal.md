## Why

When composing Solim UIs, placing a custom `BaseComponent` inside a `children()` block currently requires an explicit `component(new MyComponent())` call. This breaks the "write what the UI is" principle — developers must remember a ceremony call that the framework should handle automatically. The framework already knows a `children()` block is running (via `ParentStack`) and already knows that `BaseComponent` instantiation triggers `ComponentContext.registerChild`; it just doesn't auto-attach the element to the ambient parent. Removing `component()` from the everyday authoring path aligns with the declarative, auto-attach model every other Solim primitive already enjoys.

## What Changes

- `BaseComponent` constructor (or `ComponentContext.registerChild`) SHALL automatically enqueue the component's element for attachment to the current `ParentStack` parent when a parent is active at construction time, mirroring what layout primitives (`column`, `row`, `text`, etc.) already do via `ParentStack.attachToParent(element())`.
- `UI.component(T comp)` SHALL be retained for backwards compatibility and for the explicit-attach use case (attaching a component that was constructed outside a `children()` block).
- A new `UI.arc(Element el)` method SHALL be added to the `UI` facade for attaching raw Arc `Element` instances (e.g. `SchematicImage`, `SplitBar`) that do not extend `BaseComponent`. The name `arc()` is intentional: it signals an explicit escape hatch into the Arc layer, making it obvious this is for widgets with no Solim equivalent — discouraging misuse for things like `arc(new Label("hi"))` when `text("hi")` already exists.
- All existing `component(new SomeBaseComponent(...))` call sites inside `children()` blocks SHALL be removable — the explicit call becomes optional, not required.

## Capabilities

### New Capabilities

- `solim-component-auto-attach`: `BaseComponent` instances created inside an active `ParentStack` scope auto-attach their element to the current parent, making `component()` unnecessary for the common in-`children()` usage.
- `solim-arc-interop-facade`: `UI.arc(Element el)` — a named escape hatch for attaching raw Arc `Element` instances with no Solim equivalent, replacing the `component(() -> someElement)` workaround. The deliberate name discourages casual misuse.

### Modified Capabilities

- `solim-component`: The `BaseComponent` instantiation contract changes — construction inside a `children()` scope now has the implicit side-effect of scheduling element attachment. Spec scenarios for `component()` need updating to reflect that direct `component()` calls are now optional, not required, inside `children()` blocks.
- `solim-declarative-ui`: The auto-attach guarantee must now cover `BaseComponent` subclass instances in addition to layout primitives and `text()`/`image()` etc.

## Impact

- **`solim-core`**: `BaseComponent` constructor and/or `ComponentContext` — where child registration is triggered. The auto-attach hook must be added here.
- **`solim` (facade)**: `UI.java` — add `arc(Element el)` escape-hatch method.
- **`mod`**: All `component(new SomeBaseComponent(...))` calls inside `children()` blocks can be removed. Affected files: `ChatOverlayHudView`, `ChatMessageListView`, `ChatUserListView`, `ChatAvatar`, `ChatChannelListView`.
- **No breaking changes** — existing `component()` calls remain valid.
