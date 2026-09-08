## 1. Implement Strict Validations in ElementModifiers

- [x] 1.1 Remove `@Nullable` annotations from `Element` and `Table` parameters and add `Objects.requireNonNull` across all modifier methods in `solim.modifier.ElementModifiers`
- [x] 1.2 Update `ElementModifiers.gap(Element element, float gap)` to require `element` to be an instance of `Table` and throw `IllegalArgumentException` otherwise

## 2. Update Tests and Verify

- [x] 2.1 Update `solim/src/test/java/solim/modifier/ElementModifiersTest.java` to assert `NullPointerException` on null targets and `IllegalArgumentException` on non-Table element for `gap`
- [x] 2.2 Run `./gradlew test` and verify clean build, passing tests, and Java 8 runtime compatibility
