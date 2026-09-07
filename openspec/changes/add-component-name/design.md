## Context

Arc UI uses `Element.name` (a public `String` field on `arc.scene.Element`) to tag and query elements in UI hierarchies (e.g., via `scene.find(name)`, debugging logs, or UI tree introspection). Solim components wrap Arc elements and expose declarative modifier methods such as `.width()`, `.height()`, `.size()`, `.visible()`, and `.gap()`. However, Solim components lack a uniform `.name(String)` modifier.

## Goals / Non-Goals

**Goals:**
- Add `ElementModifiers.name(@Nullable Element element, @Nullable String name)` as the centralized helper.
- Add `default Component name(String name)` to `solim.core.Component`.
- Expose fluent `.name(String name)` returning `this` on all concrete Solim components across layout, input, display, and overlay packages.
- Ensure all Solim input widgets (`Checkbox`, `SolimSlider`, `Switch`, `SolimSelect`) implement `Component`.

**Non-Goals:**
- Implement automated unique name generation or id namespacing.
- Modify Mindustry Arc's underlying `Element` class.

## Decisions

### 1. Centralize Naming in `ElementModifiers`
- **Decision**: Add `public static void name(@Nullable Element element, @Nullable String name)` in `ElementModifiers`.
- **Rationale**: Keeps all direct `Element` mutations centralized in `ElementModifiers`, consistent with `ElementModifiers.width`, `ElementModifiers.size`, `ElementModifiers.position`, and `ElementModifiers.gap`.

### 2. Default Method on `Component` Interface
- **Decision**: Add:
  ```java
  default Component name(String name) {
      ElementModifiers.name(element(), name);
      return this;
  }
  ```
- **Rationale**: Any class implementing `Component` immediately gains naming functionality out of the box.

### 3. Concrete Return Types on Component Implementations
- **Decision**: In addition to the interface default, provide typed overloads returning `this` on concrete classes:
  ```java
  public Row name(String name) {
      ElementModifiers.name(table, name);
      return this;
  }
  ```
- **Rationale**: Preserves fluent chaining without requiring callers to cast or lose concrete layout methods (e.g. `row().name("nav").gap(8).children(...)`).

### 4. Implement `Component` on All Solim Widgets
- **Decision**: Ensure `Checkbox`, `SolimSlider`, `Switch`, and `SolimSelect` implement `Component` and return their primary `Element` via `element()`.
- **Rationale**: Unifies all Solim widgets under the `Component` contract and ensures `.name(String)` is universally available.

## Risks / Trade-offs

- [Risk] Null element before build: `BaseComponent` builds lazily on first `element()` call. Calling `.name(name)` before `element()` could fail to set the name if applied to null.
  - *Mitigation*: In `BaseComponent`, `element()` forces build if not already built, or cache `name` string to apply after `build()`.
