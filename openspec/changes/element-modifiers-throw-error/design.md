## Context

`ElementModifiers` is the central static helper in Solim for mutating Arc `Element` and `Table` instances (size, position, alignment, margin, padding, gap, and name). Historically, every method accepted `@Nullable Element` or `@Nullable Table` and returned silently when null was passed. Furthermore, `ElementModifiers.gap(Element, float)` silently ignored calls when the element was not a `Table`. This silent failure mode contradicts project guidelines (which state that values are non-nullable by default) and conceals configuration or lifecycle bugs.

## Goals / Non-Goals

**Goals:**
- Enforce fail-fast parameter validation in `ElementModifiers`.
- Throw `NullPointerException` via `Objects.requireNonNull` when any target `Element` or `Table` parameter is null.
- Throw `IllegalArgumentException` in `ElementModifiers.gap(Element, float)` when the target is not a `Table`.
- Clean up `@Nullable` annotations on target arguments so parameters are clearly non-null by default.
- Update test cases to assert that calling `ElementModifiers` with null or invalid targets throws the expected exceptions.

**Non-Goals:**
- Adding defensive null checks in caller components where elements are guaranteed non-null.
- Restricting `name` string to non-null: Arc `Element.name` can legitimately be null to clear an element's name.

## Decisions

1. **Use `Objects.requireNonNull` for target validation**
   - *Rationale*: Java 8 standard library method that throws `NullPointerException` with descriptive messages (`"element cannot be null"`, `"table cannot be null"`).
   - *Alternative considered*: Custom `IllegalArgumentException` for all checks. Rejected because NPE is idiomatic in Java when a non-null object reference is null.

2. **Explicit type check for `gap(Element, float)`**
   - *Rationale*: Since `Element` has no concept of default cell padding, applying `gap` to a non-`Table` `Element` is an invalid operation. Throwing `IllegalArgumentException("Element must be a Table to apply gap spacing: " + element.getClass().getName())` immediately surfaces misuse.
   - *Alternative considered*: Leave `gap(Element, float)` silent. Rejected because callers expecting gap to work would wonder why spacing is not applied.

3. **Signature cleanup**
   - *Rationale*: Remove `@Nullable Element` and `@Nullable Table` annotations from all methods in `ElementModifiers`, adhering to project rule: "By default, values are non-nullable." Only keep `@Nullable` on `@Nullable String name`.

## Risks / Trade-offs

- **[Risk] Existing code or tests passing null targets will throw exceptions**
  - *Mitigation*: Existing unit tests in `solim` that verified null-safety (`assertDoesNotThrow(() -> ElementModifiers.gap(null, 20f))`) will be updated to verify `assertThrows(NullPointerException.class, ...)`. No production code in the mod passes null elements to `ElementModifiers`.
