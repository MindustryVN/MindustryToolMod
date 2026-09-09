# Solim Architecture Refactor

## Goal

Refactor and simplify the Solim codebase with these primary goals:

1. Improve code readability.
2. Make ownership and lifecycle behavior predictable.
3. Reduce hidden ambient behavior.
4. Clearly separate:

   * reactive state
   * lifecycle ownership
   * UI attachment
   * parent layout
   * element properties
   * container child configuration
5. Preserve Solim's core philosophy:

   * no Virtual DOM
   * no full rerendering
   * direct Arc `Element` mutation
   * fine-grained `Signal` / `Computed` / `Effect`
   * automatic lifecycle cleanup
   * declarative UI composition
6. Do not rewrite the entire library unnecessarily.
7. Prefer incremental refactoring while preserving existing behavior.

---

# Core Design Rules

## Rule 1: Construction must not secretly mount UI

This:

```java
new FeatureCard(feature);
```

must primarily mean:

```text
Create a Java component object.
```

Construction must not directly:

* attach an element to a `Table`
* mutate a UI parent stack
* register pending UI attachment

Avoid constructor side effects related to UI placement.

Lifecycle ownership may remain automatic where necessary, but UI mounting and attachment must be separated from construction.

---

## Rule 2: Separate lifecycle ownership from UI attachment

These are different concepts:

```text
Ownership
=
Who disposes this resource?

Attachment
=
Where is this Element mounted?
```

Do not mix them.

Target architecture:

```text
Component
│
├── Builds Element
├── Owns lifecycle resources
└── Disposes resources

UI composition
│
├── Determines parent
├── Attaches Elements
└── Configures parent-child layout
```

`BaseComponent` should not need to know implementation details of UI attachment such as `ParentStack`.

---

## Rule 3: Reactive resources created inside a component should be automatically owned

Application code should not normally need:

```java
own(Effect.of(() -> {
    ...
}));
```

Preferred usage:

```java
Effect.of(() -> {
    String channelId = store.activeChannelId().get();

    if (!Objects.equals(channelId, lastChannelId)) {
        lastChannelId = channelId;
        scrollToBottom();
    }
});
```

When called inside an active component lifecycle scope:

```text
Effect created
    ↓
Automatically owned by current component
    ↓
Component disposed
    ↓
Effect disposed
    ↓
Dependencies unsubscribed
```

When called outside a component scope:

```text
Effect created
    ↓
No automatic owner
    ↓
Caller is responsible for disposal
```

Do not require application code to manually understand lifecycle plumbing for normal component usage.

---

# Phase 1 — Lifecycle Correctness

## 1. Reverse disposal order

Resources are typically acquired in order:

```text
A
↓
B
↓
C
```

They should normally be disposed:

```text
C
↓
B
↓
A
```

Current forward disposal:

```java
for (Disposable disposable : disposables) {
    disposable.dispose();
}
```

must be replaced with reverse disposal:

```java
for (int i = disposables.size() - 1; i >= 0; i--) {
    Disposable disposable = disposables.get(i);

    try {
        disposable.dispose();
    } catch (Throwable throwable) {
        Log.err(
            "Error disposing resource in " + getClass().getSimpleName(),
            throwable
        );
    }
}
```

### Checklist

* [ ] Disposal happens in reverse registration order.
* [ ] Every disposable is attempted even if one fails.
* [ ] Errors are logged.
* [ ] Disposable collection is cleared afterward.
* [ ] Double disposal remains safe.

---

## 2. Prevent building disposed components

A disposed component must not be rebuilt.

Invalid behavior:

```java
Component component = new MyComponent();

component.dispose();
component.element(); // Must not silently build
```

Add a guard:

```java
private void checkNotDisposed() {
    if (disposed) {
        throw new IllegalStateException(
            "Cannot use disposed component: "
                + getClass().getSimpleName()
        );
    }
}
```

Use it before building.

### Checklist

* [ ] `element()` throws after disposal.
* [ ] Calling `dispose()` multiple times is safe.
* [ ] A component cannot be resurrected after disposal.

---

## 3. Dispose partial builds when `build()` fails

If `build()` creates:

* Effects
* event listeners
* child components
* bindings

and then throws, all previously created resources must be cleaned up.

Target:

```java
try {
    cached = build();
    applyName(cached);
    return cached;
} catch (Throwable throwable) {
    dispose();
    throw throwable;
} finally {
    ComponentContext.pop();
}
```

### Checklist

* [ ] Failed builds dispose owned resources.
* [ ] Component context is always popped.
* [ ] No partially registered Effect survives a failed build.
* [ ] No partially registered event listener survives a failed build.

---

# Phase 2 — Standardize Ownership

## 4. Make `Component` extend `Disposable`

Components are lifecycle resources.

Target:

```java
public interface Component extends Disposable {

    Element element();

    @Override
    void dispose();
}
```

This allows:

```java
own(component);
```

instead of special ownership logic.

---

## 5. Unify ownership APIs

Avoid having multiple overlapping concepts:

```java
registerDisposable(...)
own(...)
ownChild(...)
```

Prefer one primary internal ownership method:

```java
public final <T extends Disposable> T own(T disposable) {
    if (disposable != null) {
        disposables.add(disposable);
    }

    return disposable;
}
```

Then all resources work consistently:

```java
own(effect);
own(listener);
own(binding);
own(component);
```

If compatibility aliases are needed temporarily, mark them deprecated and migrate usages.

### Checklist

* [ ] One primary ownership API exists.
* [ ] Components can be owned directly.
* [ ] Effects can be owned directly.
* [ ] Bindings can be owned directly.
* [ ] Event subscriptions can be owned directly.
* [ ] Legacy ownership APIs are removed or deprecated.

---

# Phase 3 — Automatic Effect Ownership

## 6. Automatically register Effects with the active lifecycle context

`Effect.of(...)` currently creates and runs an Effect.

Improve the lifecycle sequence:

```text
Create Effect
    ↓
Register with active ComponentContext
    ↓
Run Effect
    ↓
Track dependencies
```

Do not run user code before ownership is established.

Implement a single internal factory:

```java
private static Effect create(
        Runnable runnable,
        Supplier<Runnable> supplier,
        Consumer<Cleanup> cleanupConsumer
) {
    Effect effect = new Effect(
        runnable,
        supplier,
        cleanupConsumer
    );

    ComponentContext.registerDisposable(effect);

    effect.runEffect();

    return effect;
}
```

Then delegate all public factories:

```java
public static Effect of(Runnable runnable) {
    return create(runnable, null, null);
}

public static Effect of(Supplier<Runnable> supplier) {
    return create(null, supplier, null);
}

public static Effect of(Consumer<Cleanup> consumer) {
    return create(null, null, consumer);
}
```

If no active component context exists:

```java
ComponentContext.registerDisposable(effect);
```

must safely do nothing.

### Desired application API

Before:

```java
own(Effect.of(() -> {
    update();
}));
```

After:

```java
Effect.of(() -> {
    update();
});
```

### Checklist

* [ ] Effects automatically register inside active component context.
* [ ] Effects outside a component context remain manually managed.
* [ ] Registration happens before the initial Effect run.
* [ ] All `Effect.of(...)` overloads use the same creation path.
* [ ] Existing `own(Effect.of(...))` usages are migrated where appropriate.
* [ ] No Effect is double-owned.

---

# Phase 4 — Fix Signal Callback Cleanup

## 7. Redesign `createSignal(callbackRegistrar, supplier)`

Do not use:

```java
Consumer<Runnable> callbackRegistrar
```

because it provides no cleanup mechanism.

A lifecycle-managed callback registration must return a `Disposable`.

Preferred API:

```java
public <T> Signal<T> createSignal(
        Function<Runnable, Disposable> registrar,
        Supplier<T> supplier
) {
    Signal<T> signal = Signal.of(supplier.get());

    if (registrar != null) {
        Disposable subscription = registrar.apply(
            () -> signal.set(supplier.get())
        );

        own(subscription);
    }

    return signal;
}
```

If Arc APIs do not naturally provide an unsubscribe handle, create an adapter abstraction.

Never claim automatic cleanup when the underlying API cannot actually unregister.

### Checklist

* [ ] Callback registrations return a cleanup handle.
* [ ] Cleanup handle is owned by the component.
* [ ] Component disposal unregisters callbacks.
* [ ] No callback API falsely claims automatic cleanup.

---

# Phase 5 — Simplify BaseComponent

## 8. Remove UI attachment responsibilities from BaseComponent

`BaseComponent` should not directly depend on:

```java
ParentStack
Table
```

Remove imports such as:

```java
import arc.scene.ui.layout.Table;
import solim.ui.ParentStack;
```

from `BaseComponent` unless there is an unavoidable architectural reason.

Target responsibility:

```text
BaseComponent
├── Lazy build
├── Cached Element
├── Lifecycle ownership
├── Disposal
├── Naming/debug metadata
└── Component lifecycle context
```

Not:

```text
BaseComponent
├── Find UI parent
├── Register pending UI attachment
└── Control parent stack
```

---

## 9. Avoid constructor-based UI side effects

Current construction should not cause UI placement behavior.

Avoid:

```java
public BaseComponent() {
    ParentStack.registerPendingComponent(...);
}
```

Component mounting should happen through the UI composition layer.

### Target conceptual flow

```text
new Component()
    ↓
Component exists

component.element()
    ↓
Builds Arc Element

UI composition
    ↓
Attaches Element to parent
```

Ownership and attachment must remain distinguishable.

---

# Phase 6 — Clarify UI Composition

## 10. Define clear modifier categories

Every fluent method should have a clear target.

### A. Self / Element modifiers

These modify the actual Arc `Element`.

Examples:

```java
.visible(...)
.opacity(...)
.color(...)
.name(...)
.position(...)
```

### B. Parent layout modifiers

These modify how this element behaves inside its parent.

Examples:

```java
.grow()
.growX()
.growY()
.margin(...)
.align(...)
```

### C. Container / child configuration

These modify how a container manages its children.

Examples:

```java
.padding(...)
.gap(...)
.defaults(...)
```

Do not make these categories ambiguous.

---

## 11. Establish a strict fluent ordering convention

Recommended visual order:

```java
column()

    // Configure this container
    .padding(16f)
    .gap(12f)

    // Create children
    .children(() -> {

        text("Title");

        divider();

        row()
            .children(() -> {
                text("Enable")
                    .growX();

                checkbox(enabled);
            })
            .growX();
    })

    // Configure this container inside its parent
    .grow();
```

Meaning:

```text
Before children()
=
configure this component/container

Inside children()
=
configure child components

After children()
=
configure this component's parent layout
```

Enforce this convention consistently across Solim.

If the API design allows stronger compile-time separation, prefer that.

---

## 12. Remove overlap between modifier systems

Audit:

```text
ElementModifiers
LayoutModifiers
```

For every modifier, answer:

```text
What object does it modify?
```

Examples:

```text
opacity → Element
visible → Element
position → Element

grow → Parent Cell
growX → Parent Cell
margin → Parent Cell

padding → Container
gap → Container
```

Do not expose the same behavior through multiple modifier systems unless there is a genuinely different target.

### Checklist

* [ ] Every modifier has one clear owner.
* [ ] No duplicate modifier implementations without reason.
* [ ] Modifier names describe their actual target.
* [ ] Documentation explains target behavior.

---

# Phase 7 — Reduce Ambient State

## 13. Audit ComponentContext and ParentStack

Identify exactly what each ambient system owns.

Target separation:

```text
ComponentContext
=
Lifecycle ownership

ParentStack
=
UI composition / attachment only
```

Do not allow both systems to independently manage component lifecycle.

---

## 14. Remove unsafe pause/resume patterns

Avoid:

```java
ComponentContext.pause();

try {
    ...
} finally {
    ComponentContext.resume();
}
```

as a general lifecycle mechanism.

Prefer scoped APIs:

```java
ComponentContext.withoutAutoOwnership(() -> {
    ...
});
```

or explicit internal ownership:

```java
owner.createDetachedChild(...);
```

The system should make ownership explicit internally rather than relying on global mutable flags.

### Checklist

* [ ] No pause/resume call can leave context permanently paused.
* [ ] Scoped APIs use `try/finally`.
* [ ] Structural components explicitly control child ownership.
* [ ] Ambient context is minimized.

---

# Phase 8 — Structural Reactivity

## 15. Share reconciliation infrastructure

Audit:

```text
Dynamic
ForEach
ReactiveGrid
```

Extract shared concepts:

```text
StructuralReconciler
├── Create child
├── Preserve existing child
├── Mount child
├── Remove child
└── Dispose child
```

The public APIs can remain different:

```java
dynamic(...)
forEach(...)
grid(...)
```

but lifecycle/reconciliation logic should not be duplicated unnecessarily.

### Checklist

* [ ] Removed children are always disposed.
* [ ] Preserved keyed children keep identity.
* [ ] Reused children are not rebuilt unnecessarily.
* [ ] Child ownership has one consistent implementation.
* [ ] Structural containers do not double-register children.

---

# Phase 9 — Standardize Two-Way Binding

## 16. Extract shared two-way binding logic

Do not implement feedback-loop guards independently in every input.

Avoid repeated code:

```java
boolean updating = false;
```

in:

```text
Checkbox
Slider
TextField
Select
Switch
```

Create a reusable internal binding abstraction.

Conceptually:

```java
TwoWayBinding.bind(
    signal,
    widgetGetter,
    widgetSetter,
    widgetListener
);
```

The abstraction should:

```text
Signal changes
    ↓
Update widget

Widget changes
    ↓
Update signal

Internal update
    ↓
Prevent feedback loop
```

### Checklist

* [ ] All two-way inputs use shared logic.
* [ ] Equality checks prevent unnecessary updates.
* [ ] Programmatic widget updates do not trigger loops.
* [ ] Binding is disposable.
* [ ] Binding is lifecycle-owned.

---

# Phase 10 — Improve Readability of BaseComponent

## 17. Extract debug naming logic

Keep `element()` focused on lifecycle.

Extract:

```java
private void applyName(Element element)
```

Target:

```java
@Override
public final Element element() {
    checkNotDisposed();

    if (cached != null) {
        return cached;
    }

    ComponentContext.push(this);

    try {
        cached = build();

        if (cached == null) {
            throw new IllegalStateException(
                "build() returned null for "
                    + getClass().getSimpleName()
            );
        }

        applyName(cached);

        return cached;
    } catch (Throwable throwable) {
        dispose();
        throw throwable;
    } finally {
        ComponentContext.pop();
    }
}
```

---

# Desired BaseComponent Responsibility

After refactoring, `BaseComponent` should conceptually look like:

```text
BaseComponent
│
├── name(...)
├── element()
│   ├── validate lifecycle
│   ├── lazy build
│   ├── enter ComponentContext
│   ├── build
│   ├── apply metadata
│   └── handle failed build
│
├── own(...)
├── listen(...)
├── createSignal(...)
│
└── dispose()
    ├── mark disposed
    ├── onDispose()
    └── dispose owned resources in reverse order
```

It should not manage:

```text
UI parent lookup
Pending UI attachment
Table mounting
ParentStack mechanics
```

---

# Public API Rules

Application code should ideally look like this:

```java
public final class ChannelView extends BaseComponent {

    @Override
    protected Element build() {
        Effect.of(() -> {
            String channelId = store.activeChannelId().get();

            if (!Objects.equals(channelId, lastChannelId)) {
                lastChannelId = channelId;
                scrollToBottom();
            }
        });

        return column()
            .padding(16f)
            .gap(8f)
            .children(() -> {

                text(store.channelName());

                divider();

                forEach(
                    store.messages(),
                    Message::id,
                    MessageItem::new
                );
            })
            .grow()
            .element();
    }
}
```

The application developer should not normally need to understand:

```text
ParentStack
ComponentContext.pause()
Manual Effect disposal
Internal reconciliation
Subscription ownership
```

Those should remain framework implementation details.

---

# Important Constraints

## Do not rewrite the reactive system

Preserve the existing strengths:

```text
Signal
Computed
Effect
Auto dependency tracking
Fine-grained updates
No Virtual DOM
Direct Arc Element mutation
```

Do not introduce:

* React-style full rerenders
* Virtual DOM
* unnecessary object recreation
* global UI refreshes

---

## Do not break existing UI behavior unnecessarily

Before changing public APIs:

1. Search all usages.
2. Identify migration scope.
3. Preserve compatibility where cheap.
4. Prefer automated mechanical migration.
5. Remove deprecated APIs only after usages are migrated.

---

# Required Implementation Process

For every phase:

1. Inspect current implementation.
2. Identify all usages.
3. Implement the smallest clean change.
4. Run formatting.
5. Run compilation.
6. Run tests.
7. Fix affected call sites.
8. Search for obsolete patterns.
9. Verify lifecycle behavior.

Do not blindly modify files based only on this document. Adapt implementation details to the actual existing architecture while preserving these design goals.

---

# Final Verification Checklist

## Lifecycle

* [ ] Components cannot build after disposal.
* [ ] Failed builds clean up resources.
* [ ] Resources dispose in reverse order.
* [ ] Double disposal is safe.
* [ ] Effects unsubscribe when their owner is disposed.
* [ ] Event listeners unregister when their owner is disposed.
* [ ] Callback-based signals have real cleanup.

## Ownership

* [ ] Effects created in component scope are automatically owned.
* [ ] Application code rarely needs `own(...)`.
* [ ] Components are disposable resources.
* [ ] Ownership APIs are unified.
* [ ] No resource is accidentally double-owned.

## Architecture

* [ ] `BaseComponent` does not manage UI attachment.
* [ ] UI attachment is separate from lifecycle ownership.
* [ ] Constructor side effects are minimized.
* [ ] Ambient state is minimized.
* [ ] `ComponentContext` has one clear responsibility.
* [ ] `ParentStack` has one clear responsibility.

## UI API

* [ ] Modifier targets are clearly defined.
* [ ] Self modifiers are distinct from parent-layout modifiers.
* [ ] Container child configuration is distinct from both.
* [ ] Modifier ordering is consistent.
* [ ] `LayoutModifiers` and `ElementModifiers` do not overlap unnecessarily.

## Reactivity

* [ ] Effects use one creation path.
* [ ] Effects register ownership before initial execution.
* [ ] Structural reactive components share reconciliation logic where appropriate.
* [ ] Two-way bindings use shared infrastructure.

## Developer Experience

Application UI code should generally read declaratively:

```java
column()
    .padding(16f)
    .gap(12f)
    .children(() -> {

        text("Settings");

        row()
            .children(() -> {
                text("Enabled")
                    .growX();

                checkbox(enabled);
            });
    })
    .grow();
```

The developer should be able to understand:

* what component is being configured
* where children belong
* what modifiers affect the element
* what modifiers affect parent layout
* what resources are automatically cleaned up

without needing to inspect hidden framework state.

---

# Definition of Done

The refactor is complete when:

1. Lifecycle correctness issues are fixed.
2. Effect ownership is automatic inside component scopes.
3. `BaseComponent` no longer mixes lifecycle and UI attachment.
4. Modifier responsibilities are clearly separated.
5. Ambient ownership/UI state is reduced.
6. Structural reactive components have consistent ownership behavior.
7. Application UI code becomes simpler and requires fewer manual lifecycle calls.
8. All existing tests pass.
9. New tests cover:

   * Effect disposal
   * failed builds
   * reverse disposal order
   * disposed component behavior
   * callback cleanup
   * automatic Effect ownership
10. The final code is simpler than before, not merely more abstract.

## Guiding Principle

> Make ownership, lifecycle, UI attachment, and modifier targets obvious from the code instead of relying on hidden framework behavior.
