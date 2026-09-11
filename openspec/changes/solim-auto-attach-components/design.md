## Context

Solim manages two orthogonal stacks during UI construction:

1. **`ComponentContext` stack** — tracks the currently-building `BaseComponent` for automatic lifecycle ownership. When a child `BaseComponent` is constructed, its constructor calls `ComponentContext.registerChild(this)`, which calls `parent.own(child)` so the child is disposed with its parent.

2. **`ParentStack` stack** — tracks the current layout container (`Table`). When layout primitives like `text()`, `image()`, `button()` are created, they call `ParentStack.attachToParent(element())` to wire themselves into the layout.

The gap: `BaseComponent` subclasses created inside `children()` blocks participate in `ComponentContext` (lifecycle) but do NOT automatically participate in `ParentStack` (layout). Authors must call `component(new MyView())` to attach the element. Every other Solim primitive auto-attaches; `BaseComponent` subclasses are the only exception.

## Goals / Non-Goals

**Goals:**
- `BaseComponent` instances constructed inside an active `ParentStack` scope automatically schedule element attachment to the current parent — no `component()` call required.
- `UI.element(Element el)` public facade for attaching a raw Arc `Element` to the current parent (replacing the `component(() -> someArcElement)` workaround).
- Existing `component(T comp)` stays valid and backwards-compatible.
- The `component()` call inside `children()` becomes optional, not required.

**Non-Goals:**
- Removing the `component()` API.
- Auto-attaching `BaseComponent` instances constructed outside a `children()` scope (e.g., stored as a field and later used).
- Changing `ParentStack.isolate()` semantics used during `build()` execution.

## Decisions

### Decision 1: Where to hook — `BaseComponent` constructor vs `ComponentContext.registerChild`

**Options:**
- A) Hook inside `BaseComponent` constructor after `ComponentContext.registerChild(this)` — check whether `ParentStack` is active and, if so, call `ParentStack.registerPendingComponent(this, current)`.
- B) Hook inside `ComponentContext.registerChild` — but this mixes layout concerns into a pure lifecycle class.
- C) New `ParentStack.registerPendingComponent` overload for `Component` — called from the `BaseComponent` constructor directly.

**Decision: Option A.** The `BaseComponent` constructor is the natural join point because it already calls `ComponentContext.registerChild(this)`. Adding a `ParentStack` check there keeps concerns co-located with the construction event. `ComponentContext` stays focused on lifecycle only.

**Rationale:** The constructor fires exactly once, at the moment the developer writes `new MyView()` inside `children()`. The active `ParentStack` entry at that moment IS the intended parent. `ParentStack.registerPendingComponent` already exists for deferred attachment (used by the existing `component()` implementation), so no new attachment API is needed.

### Decision 2: Immediate attach vs pending (deferred) attach

**Options:**
- A) Immediate: call `element()` right in the constructor to attach now.
- B) Deferred: register as a pending component; attach when the parent is popped (existing pending-component mechanism).

**Decision: Option B — deferred (pending) attach.** Calling `element()` inside the constructor violates `BaseComponent`'s lazy-build contract. The component may not be fully constructed yet (superclass constructor fires before subclass fields are initialized). The existing `registerPendingComponent` / `attachPendingComponents` mechanism is already proven and used by `component()`.

### Decision 3: Guard against double-attach

`component()` currently calls `ParentStack.attachToParent(ParentStack.isolate(comp::element))` which calls `doAttach`, which already guards: `if (child.parent != parent && !parent.getChildren().contains(child, true))`. So if a developer calls `component(new MyView())` on a component that was already auto-registered, the second attach is silently skipped. No additional guard needed.

### Decision 4: `UI.arc(Element el)` facade — naming as a guardrail

For raw Arc `Element` instances (e.g. `SchematicImage`, `SplitBar`) that do not extend `BaseComponent`, add a facade method to `UI.java`:

```java
public static Element arc(Element el) {
    ParentStack.attachToParent(el);
    return el;
}
```

**Name rationale:** `arc()` rather than `element()`. The name is chosen deliberately to signal "I am crossing into the raw Arc layer." A developer (or AI agent) seeing `arc(new Label("hi"))` would immediately recognise the mismatch — `text("hi")` exists and is obviously preferred. The word `element` is too generic and would invite casual misuse for any Arc widget; `arc` is a named escape hatch that aligns with the project rule: *"Direct Arc usage is allowed only when Solim has no reasonable equivalent."*

## Risks / Trade-offs

- **Unintended auto-attach when constructing outside `children()`** → If a developer constructs a `BaseComponent` in a field initializer or during a non-children scope where `ParentStack` happens to be active (unlikely but possible), the component would be silently attached. Mitigation: `ParentStack.current()` returns `null` outside `children()` scopes; the hook only fires when `ParentStack.current() != null`.

- **`build()` runs in an `isolate()` scope** → `BaseComponent.element()` calls `ComponentContext.push(this)` and runs `build()` inside `ParentStack.isolate(comp::element)`. Children created during `build()` are registered against the parent, but `ParentStack` is cleared during `isolate`. The auto-attach hook fires in the **constructor**, before `build()`, while the outer `children()` parent is still on the stack. So there is no conflict.

- **Backwards compatibility** → All existing `component()` calls remain valid. Double-attach is already guarded. Zero breaking changes.
