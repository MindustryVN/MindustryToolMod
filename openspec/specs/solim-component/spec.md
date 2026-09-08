# solim-component Specification

## Purpose
TBD - created by archiving change create-solim-core. Update Purpose after archive.
## Requirements
### Requirement: Component interface
The framework SHALL provide `solim.core.Component` with `arc.scene.Element element()`, `default void dispose()`, and `default Component name(String name)` to rename the component's underlying Arc `Element`.

#### Scenario: Component returns Element
- **WHEN** `Component c = new MyComponent()` and `Element e = c.element()` is called
- **THEN** `e` is the Arc `Element` built by the component (non-null after build)

#### Scenario: Default dispose is no-op
- **WHEN** a `Component` does not override `dispose()`
- **THEN** calling `dispose()` does not throw and is safe

#### Scenario: Default name sets element name
- **WHEN** `Component c = new MyComponent()` and `c.name("custom-name")` is called
- **THEN** `c.element().name` equals `"custom-name"` and `c` is returned

### Requirement: BaseComponent single-build semantics
`BaseComponent` SHALL be an abstract class implementing `Component` with lazy `build()` semantics: `protected abstract Element build()` runs exactly once on first `element()` call, result is cached, and subsequent `element()` calls return cached instance. Components SHALL NOT automatically re-render.

#### Scenario: Build runs once
- **WHEN** `BaseComponent comp = new Counter()` then `comp.element()` is called twice
- **THEN** `build()` is invoked exactly once and both calls return same `Element` instance

#### Scenario: No automatic re-render on signal change
- **WHEN** a `Signal` inside a `BaseComponent` changes
- **THEN** `build()` is NOT called again; only reactive bindings/effects mutate existing `Element` properties

### Requirement: Constructor props and normal Java composition
Custom components SHALL be normal Java classes with constructor parameters as props; no React-style props map or hooks SHALL be required. Components may hold `Signal`, `Computed`, `Effect` as fields.

#### Scenario: Constructor props
- **WHEN** `UserCard card = new UserCard(Signal<User> user)` with `user` field stored
- **THEN** `card.build()` can read `user.get()` or `user.map(User::name)` without framework injecting props

#### Scenario: Signal fields inside component
- **WHEN** `Counter` holds `Signal<Integer> count = Signal.of(0)` and `Computed<String> text = count.map(v -> "Count: " + v)` and `build()` returns `button(text, () -> count.set(count.get()+1))`
- **THEN** clicking button updates `text` via reactive binding without rebuilding `Counter`

### Requirement: Component as child via ElementResolver
Layouts and parents SHALL accept children that are `Element` or `Component`; `Component` children SHALL be automatically resolved to `component.element()` without requiring callers to write `new Header().element()`.

#### Scenario: Direct Component child
- **WHEN** `column(() -> { add(new Header()); add(new ChatPanel()); })` where `Header`/`ChatPanel` implement `Component`
- **THEN** parent column contains their resolved `Element`s in order

#### Scenario: Mixed Element and Component children
- **WHEN** `column(() -> { text("Hello"); add(new UserCard(user)); image(tex); })`
- **THEN** all three children are attached correctly via `ElementResolver.resolve(child)` logic

### Requirement: Component lifecycle and explicit disposal
Components SHALL follow lifecycle `constructor → build() → element mounted → dispose()` . Components owning `Effect` or `Subscription` SHALL dispose them in overridden `dispose()`. Framework SHALL NOT introduce complex ownership/scope hooks; lifecycle is explicit.

#### Scenario: Effect disposed with component
- **WHEN** `ChatPanel` creates `Effect effect = Effect.of(() -> Log.info(messages.get().size))` in `build()` and overrides `dispose() { effect.dispose(); }`
- **THEN** after `panel.dispose()` the effect no longer reacts to `messages` changes

#### Scenario: BaseComponent dispose default
- **WHEN** `BaseComponent` without overrides is disposed
- **THEN** no exception and no resource leak beyond its cached `Element` reference

### Requirement: No React hooks or re-render loop
The framework SHALL NOT expose React-style hooks (`useState`, `useEffect`) nor a component re-render/reconciliation loop. Keep components as plain classes.

#### Scenario: No hooks API
- **WHEN** `solim.core` package is inspected
- **THEN** it contains no `useState`, `useEffect`, `useMemo`, or JSX-like APIs

### Requirement: Default element naming for Solim components
Every concrete Solim component SHALL automatically assign a default name to its underlying Arc `Element` upon construction, adhering to the format `solim-<component>-<internal>`. If a developer explicitly calls `.name(String name)`, the custom name SHALL completely overwrite the default name.

#### Scenario: Default name assigned on construction
- **WHEN** a Solim component such as `Button`, `Column`, `Row`, `Card`, or `Text` is instantiated without calling `.name()`
- **THEN** its `element().name` is populated with the default name conforming to `solim-<component>-<internal>` (e.g. `solim-button-sizedButton`, `solim-column-table`, `solim-card-cardButton`)

#### Scenario: Explicit name completely overwrites default name
- **WHEN** a Solim component is instantiated and `.name("custom-name")` is invoked
- **THEN** its `element().name` equals `"custom-name"` without preserving the default prefix

### Requirement: BaseComponent default naming
`BaseComponent` SHALL assign a default name formatted as `solim-<component>-<internal>` (derived from the lowercase class simple name and the built element's simple name/role) to its built element if no custom `name(...)` has been assigned before or after `build()`.

#### Scenario: BaseComponent uses default name when unset
- **WHEN** a subclass of `BaseComponent` is built and no custom name is set
- **THEN** its `element().name` is formatted as `solim-<subclass>-<internal>`

#### Scenario: BaseComponent preserves custom name override
- **WHEN** a subclass of `BaseComponent` has `.name("my-card")` called
- **THEN** its `element().name` equals `"my-card"`

