---

name: solim-testing
description: Write, review, and improve tests for Solim UI components, reactive primitives, bindings, lifecycle ownership, disposal, and structural UI updates. Use when creating tests for any code under the Solim UI library, fixing failing Solim tests, reviewing test coverage, or verifying reactive and lifecycle behavior. Focus on observable behavior using real Arc UI objects where possible, especially the full lifecycle of create, build, react, update, dispose, and verify no further reactions.
license: MIT
compatibility: Requires Java, the project's existing test framework, and the Solim/Arc UI test environment.
metadata:
author: mindustry-tool
version: "1.0"
--------------

# Solim Testing Skill

## Purpose

This skill defines how to write high-quality tests for the Solim UI library.

Solim is a fine-grained reactive UI framework built on Arc Scene UI. Tests must primarily verify:

* observable UI behavior
* reactive updates
* lifecycle ownership
* cleanup
* disposal
* structural changes

Do not test implementation details unless the test explicitly targets framework internals.

---

# When to Use This Skill

Use this skill when working on:

* Solim UI components
* `BaseComponent`
* `Component`
* `Signal`
* `Computed`
* `Effect`
* reactive bindings
* UI inputs
* lifecycle ownership
* `Disposable`
* dynamic UI
* `Dynamic`
* `ForEach`
* reactive collections
* keyed reconciliation
* UI attachment
* Solim modifier behavior

Also use this skill when:

* adding a new Solim component
* fixing a Solim bug
* refactoring Solim lifecycle code
* fixing a reactive bug
* investigating memory leaks
* reviewing missing test coverage
* modifying disposal behavior

---

# Testing Philosophy

## Test observable behavior

The primary question is:

> What should a Solim user observe?

Prefer:

```java
Signal<String> value = Signal.of("Hello");

Label label = (Label) component.element();

assertEquals("Hello", label.getText().toString());

value.set("World");

assertEquals("World", label.getText().toString());
```

Avoid:

```java
assertEquals(1, component.disposables.size());
assertEquals(2, effect.dependencies.size());
```

unless the test specifically targets those internal implementation details.

---

# The Standard Solim Lifecycle Test

For lifecycle-sensitive components, prefer testing the complete lifecycle:

```text
Create
  ↓
Build
  ↓
Verify initial state
  ↓
React to updates
  ↓
Verify updated state
  ↓
Dispose
  ↓
Update source again
  ↓
Verify no further reaction
```

This is the most important lifecycle contract in Solim.

---

# Required Workflow

Before writing tests:

1. Inspect the implementation being tested.
2. Identify its public behavior.
3. Identify reactive dependencies.
4. Identify owned resources.
5. Identify cleanup/disposal behavior.
6. Search for similar existing tests.
7. Reuse existing test utilities when appropriate.
8. Write focused tests for behavior.
9. Run the affected tests.
10. Run broader tests when the change affects shared infrastructure.

Do not invent behavior that the implementation does not intend to support.

---

# Test Structure

Use Arrange → Act → Assert.

Prefer:

```java
@Test
void updatesLabelWhenSignalChanges() {
    // Arrange
    Signal<String> value = Signal.of("Hello");

    // Act
    Label label = Ui.label(value).element();

    // Assert initial state
    assertEquals("Hello", label.getText().toString());

    // Assert update
    value.set("World");

    assertEquals("World", label.getText().toString());
}
```

For lifecycle tests:

```java
@Test
void stopsReactingAfterDisposal() {
    // Arrange
    Signal<String> value = Signal.of("Hello");
    Component component = createComponent(value);

    // Build
    Element element = component.element();

    // Verify active behavior
    value.set("World");
    assertComponentShows(element, "World");

    // Dispose
    component.dispose();

    // Verify no further reaction
    value.set("After disposal");

    assertComponentStillShows(element, "World");
}
```

Adapt assertions to the actual component API.

---

# 1. Component Tests

Every component should consider these tests.

## Lazy Build

Verify `build()` executes only once.

```java
@Test
void buildsOnlyOnce() {
    AtomicInteger builds = new AtomicInteger();

    BaseComponent component = new BaseComponent() {
        @Override
        protected Element build() {
            builds.incrementAndGet();
            return new Label("Test");
        }
    };

    Element first = component.element();
    Element second = component.element();

    assertSame(first, second);
    assertEquals(1, builds.get());
}
```

### Required checks

* [ ] Build is lazy.
* [ ] Build executes once.
* [ ] The same element instance is returned.
* [ ] Repeated `element()` calls do not rebuild.

---

# 2. Disposal Tests

Test disposal for every component that owns resources.

## Basic Disposal

```java
@Test
void disposesOwnedResources() {
    AtomicBoolean disposed = new AtomicBoolean();

    Disposable disposable = () -> disposed.set(true);

    TestComponent component = new TestComponent();
    component.own(disposable);

    component.dispose();

    assertTrue(disposed.get());
}
```

---

## Reverse Disposal Order

Resources should normally be disposed in reverse creation order.

```java
@Test
void disposesResourcesInReverseOrder() {
    List<String> disposed = new ArrayList<>();

    TestComponent component = new TestComponent();

    component.own(() -> disposed.add("A"));
    component.own(() -> disposed.add("B"));
    component.own(() -> disposed.add("C"));

    component.dispose();

    assertEquals(
        List.of("C", "B", "A"),
        disposed
    );
}
```

---

## Idempotent Disposal

```java
@Test
void disposeIsIdempotent() {
    AtomicInteger count = new AtomicInteger();

    TestComponent component = new TestComponent();
    component.own(count::incrementAndGet);

    component.dispose();
    component.dispose();

    assertEquals(1, count.get());
}
```

---

## Disposal Failure Isolation

One failing resource must not prevent later resources from being disposed.

```java
@Test
void continuesDisposingWhenResourceThrows() {
    AtomicBoolean disposed = new AtomicBoolean();

    TestComponent component = new TestComponent();

    component.own(() -> {
        throw new RuntimeException("Expected failure");
    });

    component.own(() -> disposed.set(true));

    component.dispose();

    assertTrue(disposed.get());
}
```

### Disposal checklist

* [ ] Owned resources are disposed.
* [ ] Disposal order is correct.
* [ ] Double disposal is safe.
* [ ] Failures do not stop remaining cleanup.
* [ ] Reactive resources stop observing.
* [ ] Event listeners are removed.

---

# 3. Build Failure Tests

If `build()` fails after resources have been created, those resources must be cleaned up.

```java
@Test
void disposesResourcesWhenBuildFails() {
    AtomicBoolean disposed = new AtomicBoolean();

    BaseComponent component = new BaseComponent() {
        @Override
        protected Element build() {
            own(() -> disposed.set(true));

            throw new RuntimeException("Build failed");
        }
    };

    assertThrows(
        RuntimeException.class,
        component::element
    );

    assertTrue(disposed.get());
}
```

Verify:

* [ ] The original failure is propagated.
* [ ] Resources created before failure are disposed.
* [ ] Effects created before failure are disposed.
* [ ] Context is restored.
* [ ] No leaked subscriptions remain.

---

# 4. Signal Tests

Test reactive primitives independently from UI.

## Initial Value

```java
@Test
void hasInitialValue() {
    Signal<String> signal = Signal.of("Hello");

    assertEquals("Hello", signal.get());
}
```

## Updates

```java
@Test
void updatesValue() {
    Signal<String> signal = Signal.of("Hello");

    signal.set("World");

    assertEquals("World", signal.get());
}
```

Also inspect and test the actual equality semantics implemented by `Signal`.

Do not assume duplicate values either notify or do not notify without checking the implementation.

---

# 5. Computed Tests

Test:

* initial calculation
* dependency updates
* multiple dependencies
* dynamic dependencies
* observer cleanup if supported

Example:

```java
@Test
void recalculatesWhenDependencyChanges() {
    Signal<Integer> count = Signal.of(1);

    Computed<Integer> doubled =
        Computed.of(() -> count.get() * 2);

    assertEquals(2, doubled.get());

    count.set(5);

    assertEquals(10, doubled.get());
}
```

---

# 6. Effect Tests

Effects require strong lifecycle coverage.

## Initial Execution

```java
@Test
void runsImmediately() {
    AtomicInteger runs = new AtomicInteger();

    Effect effect = Effect.of(runs::incrementAndGet);

    assertEquals(1, runs.get());

    effect.dispose();
}
```

---

## Reactive Updates

```java
@Test
void rerunsWhenDependencyChanges() {
    Signal<Integer> signal = Signal.of(1);
    AtomicInteger runs = new AtomicInteger();

    Effect effect = Effect.of(() -> {
        signal.get();
        runs.incrementAndGet();
    });

    assertEquals(1, runs.get());

    signal.set(2);

    assertEquals(2, runs.get());

    effect.dispose();
}
```

---

## Disposal

```java
@Test
void stopsReactingAfterDisposal() {
    Signal<Integer> signal = Signal.of(1);
    AtomicInteger runs = new AtomicInteger();

    Effect effect = Effect.of(() -> {
        signal.get();
        runs.incrementAndGet();
    });

    effect.dispose();

    signal.set(2);

    assertEquals(1, runs.get());
}
```

---

## Dynamic Dependencies

Test dependency switching.

Example behavior:

```text
condition = true
→ Effect depends on first

condition = false
→ Effect stops depending on first
→ Effect depends on second
```

Verify both:

* new dependency causes updates
* old dependency no longer causes updates

---

## Cleanup

Test cleanup:

```text
Run effect
↓
Dependency changes
↓
Previous cleanup runs
↓
Effect runs again
```

Also test:

```text
Dispose effect
↓
Current cleanup runs
```

---

# 7. Automatic Effect Ownership

When an Effect is created inside a component lifecycle scope:

```java
Effect.of(() -> {
    signal.get();
});
```

the Effect should be automatically disposed with its component if that is part of the Solim lifecycle contract.

Test:

```java
@Test
void automaticallyDisposesEffectWithComponent() {
    Signal<Integer> signal = Signal.of(1);
    AtomicInteger runs = new AtomicInteger();

    Component component = new BaseComponent() {
        @Override
        protected Element build() {
            Effect.of(() -> {
                signal.get();
                runs.incrementAndGet();
            });

            return new Table();
        }
    };

    component.element();

    assertEquals(1, runs.get());

    signal.set(2);

    assertEquals(2, runs.get());

    component.dispose();

    signal.set(3);

    assertEquals(2, runs.get());
}
```

Also verify Effects outside a component context are not accidentally disposed.

---

# 8. UI Binding Tests

For reactive UI bindings, always test:

```text
Initial Signal
↓
Initial UI state

Signal changes
↓
UI updates

Component disposes
↓
Signal changes again

UI no longer reacts
```

Example:

```java
@Test
void updatesLabelWhenSignalChanges() {
    Signal<String> value = Signal.of("Hello");

    Label label = Ui.label(value).element();

    assertEquals("Hello", label.getText().toString());

    value.set("World");

    assertEquals("World", label.getText().toString());
}
```

---

# 9. Two-Way Binding Tests

For input components, always test both directions.

## Signal → UI

```text
Signal changes
↓
Widget updates
```

## UI → Signal

```text
User interaction
↓
Widget changes
↓
Signal updates
```

## Feedback Prevention

```text
Signal updates widget
↓
Widget update must not recursively update signal forever
```

Test:

* [ ] Signal → UI.
* [ ] UI → Signal.
* [ ] No feedback loop.
* [ ] Equal values avoid unnecessary updates.
* [ ] Disposal removes the binding.

---

# 10. Dynamic UI Tests

For dynamic UI, test structure changes rather than internal reconciliation implementation.

Test:

* initial content
* replacement
* addition
* removal
* disposal of removed children
* no duplicate elements

Lifecycle pattern:

```text
Build
↓
Capture current child identity
↓
Update source
↓
Verify expected structure
↓
Verify removed child cleanup
```

---

# 11. ForEach and Keyed Reconciliation

For keyed collections, identity preservation is critical.

Example:

```text
[A, B, C]

→ reorder →

[C, B, A]
```

Expected:

```text
A component reused
B component reused
C component reused

Only placement/order changes
```

Test:

* [ ] Add item.
* [ ] Remove item.
* [ ] Reorder items.
* [ ] Preserve identity for stable keys.
* [ ] Dispose removed components.
* [ ] Empty collection.
* [ ] Duplicate key behavior.

Do not merely assert final text.

When keyed identity matters, capture and assert element/component identity:

```java
assertSame(previousElement, currentElement);
```

for items that should be preserved.

---

# 12. UI Structure Tests

Test actual Arc UI structure where useful.

Example:

```java
@Test
void attachesChildrenInCorrectOrder() {
    Table table = Ui.column()
        .children(() -> {
            Ui.text("One");
            Ui.text("Two");
        })
        .element();

    assertEquals(2, table.getChildren().size);
}
```

Test:

* child count
* child order
* nesting
* attachment only once

Do not test `ParentStack` internals unless specifically testing `ParentStack`.

---

# 13. Layout Tests

Do not test Arc's own layout engine unnecessarily.

Test Solim's mapping from its API to Arc configuration.

Examples:

* padding
* gap
* grow
* growX
* growY
* alignment
* width
* height

The test should answer:

> Did the Solim modifier correctly configure the intended Arc object?

Avoid brittle assertions against unrelated Arc implementation details.

---

# 14. Prefer Real Arc Objects

Use real objects where possible:

```java
new Table();
new Label();
new Image();
```

Avoid excessive mocking.

Solim is an integration layer around Arc UI, so real UI objects usually provide more valuable tests.

Mock only dependencies that cannot reasonably run in the test environment.

---

# 15. Test Isolation

Every test must clean up resources it creates.

Especially:

* Effects
* Components
* listeners
* subscriptions
* global state

Bad:

```java
Effect.of(() -> {
    signal.get();
});
```

without cleanup.

Good:

```java
Effect effect = Effect.of(() -> {
    signal.get();
});

try {
    // assertions
} finally {
    effect.dispose();
}
```

Use project test lifecycle utilities if available.

Tests must not depend on execution order.

---

# Test Naming

Use behavior-focused names:

```text
buildsOnlyOnce
updatesLabelWhenSignalChanges
stopsReactingAfterDisposal
disposesResourcesInReverseOrder
preservesChildrenByKeyWhenReordered
disposesRemovedChildren
runsCleanupBeforeEffectReruns
```

Avoid:

```text
testComponent
testEffect
test1
works
```

---

# Minimum Test Requirements

## Static Component

* [ ] Builds correctly.
* [ ] Builds once.
* [ ] Returns cached element.
* [ ] Children attach correctly.
* [ ] Disposal is safe.

## Reactive Component

Additionally:

* [ ] Initial value is rendered.
* [ ] Source updates update UI.
* [ ] Multiple updates work.
* [ ] Disposal stops updates.

## Input Component

Additionally:

* [ ] Signal → UI works.
* [ ] UI → Signal works.
* [ ] No feedback loop.
* [ ] Disposal removes binding.

## Structural Component

Additionally:

* [ ] Initial children are correct.
* [ ] Add works.
* [ ] Remove works.
* [ ] Reorder works.
* [ ] Removed children dispose.
* [ ] Stable keyed children preserve identity.

---

# Before Finishing

Run this checklist:

## Correctness

* [ ] Tests reflect actual intended behavior.
* [ ] Tests do not invent unsupported behavior.
* [ ] Edge cases relevant to the implementation are covered.

## Lifecycle

* [ ] Created resources are disposed.
* [ ] Disposed components stop reacting.
* [ ] Disposed Effects stop observing.
* [ ] Removed dynamic children are disposed.

## Quality

* [ ] Tests use descriptive names.
* [ ] Tests are isolated.
* [ ] Tests avoid unnecessary mocks.
* [ ] Tests avoid implementation details where possible.
* [ ] Tests are deterministic.
* [ ] No timing/sleep-based assertions unless absolutely unavoidable.

## Final Validation

* [ ] Run affected tests.
* [ ] Run related test suites.
* [ ] Check for leaked Effects/listeners.
* [ ] Check for flaky behavior.

---

# Guiding Principle

The most important question when testing Solim is:

> Does the component behave correctly throughout its entire lifetime and completely stop reacting after disposal?

The standard lifecycle to verify is:

```text
Create
↓
Build
↓
React
↓
Update
↓
Dispose
↓
Verify no further reaction
```

Prefer comprehensive lifecycle tests for shared reactive infrastructure and focused behavior tests for individual UI components.
