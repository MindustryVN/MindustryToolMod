# Solim UI Library: Architectural & Technical Reference

A comprehensive, technical specification and architectural summary of the **Solim** declarative and reactive UI library for **Anuken Arc / Mindustry Scene2D**.

---

## 1. Purpose & Design Philosophy

### 1.1 Problem Statement
Anuken Arc (the game engine behind Mindustry) uses a retained-mode Scene2D UI hierarchy derived from libGDX. While performant and battle-tested, writing raw Arc Scene2D code has significant drawbacks for complex applications and game mods:
- **Imperative Mutation Boilerplate:** Constructing layouts requires manual instantiation of `Table`, `Cell`, `Label`, `Image`, with imperative chaining (`table.add(label).pad(4).row()`) that obfuscates layout hierarchy.
- **Scattered Event & State Synchronization:** Keeping UI synchronized with application state usually results in ad-hoc event listeners, polled `update(...)` loops, or manual widget mutation across disparate classes.
- **Resource & Listener Leaks:** Event listeners (`Events.on(...)`), widget bindings, and timers attached to temporary dialogs or HUDs often leak when destroyed unless explicitly unhooked.
- **Rebuild Inefficiencies:** Without a reactive binding system, developers frequently call `table.clear()` and rebuild entire complex UI trees on simple state changes, generating garbage and causing layout jank.

### 1.2 The Solim Approach
Solim is a **lightweight, declarative, reactive UI framework** specifically built on top of **Arc Scene2D**. It bridges declarative UI construction with fine-grained reactivity while preserving Arc's retained widget tree.

### 1.3 Core Principles & Constraints
Solim was engineered under strict architectural constraints:
1. **No Virtual DOM & No Global Diffing:** There is no intermediate virtual node tree or tree reconciliation loop. Arc's `Element` tree is the sole render tree.
2. **Build Once, Bind Automatically:** UI structures are constructed **once** during `build()`. Reactive state changes mutate existing Arc element properties in place via targeted bindings.
3. **No React-Style Re-renders or Hooks:** Components do not re-execute their constructor or `build()` method on state change.
4. **Zero-Overhead Ambient Ownership:** Disposables, child components, and reactive bindings created during a component's `build()` are automatically captured and owned via ambient context stacks.
5. **Direct Arc Native Integration:** Solim wraps Arc widgets transparently without hiding Arc internals. Native Arc `Drawable`, `Color`, `Styles`, and `Element` instances are first-class citizens.
6. **Java 8 Runtime Compatibility:** Compiled under Java 17 syntax with Java 8 bytecode output; relies exclusively on standard Java 8 APIs and Arc utilities (no `List.of()`, `String.isBlank()`, or Java 9+ standard library methods).

---

## 2. High-Level Architecture

Solim is decoupled into distinct, focused packages:

```text
┌─────────────────────────────────────────────────────────────────────────┐
│                           Application / Mod                             │
│       (e.g., FeatureCard, QuickAccessHudView, SettingsDialog)          │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │
                    uses declarative DSL & facades
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                               solim.ui                                  │
│   Ui (Static Facade)    ParentStack (Nesting)    Units (Viewport Dvh)   │
│   Binding (Properties)  Dynamic (Subtrees)       ForEach (Keyed Lists)  │
└──────┬─────────────────────────────┬────────────────────────────┬───────┘
       │ uses                        │ contains                   │ uses
       ▼                             ▼                            ▼
┌──────────────┐             ┌──────────────┐             ┌───────────────┐
│ solim.signal │             │ solim.layout │             │ solim.display │
│              │             │              │             │ solim.input   │
│ Signal<T>    │             │ Column, Row  │             │ solim.overlay │
│ Computed<T>  │◄────────────┤ Card, Grid   │             │ solim.feedback│
│ Effect       │  reactivity │ ReactiveGrid │             │ solim.style   │
│ Context      │             │ Scroll, Tabs │             │               │
└──────┬───────┘             └──────┬───────┘             └───────┬───────┘
       │                            │ implements                  │
       │ automatic                  ▼                             │
       │ ownership          ┌──────────────────┐                  │
       └───────────────────►│    solim.core    │◄─────────────────┘
                            │                  │
                            │ Component        │
                            │ BaseComponent    │
                            │ ComponentContext │
                            │ Disposable       │
                            └────────┬─────────┘
                                     │
                     produces and mutates in place
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                   Arc Scene2D / Mindustry Runtime                       │
│    Table, Cell, Element, ScrollPane, BaseDialog, Events, Core.scene    │
└─────────────────────────────────────────────────────────────────────────┘
```

### Module Responsibilities

| Package | Key Classes | Role |
| :--- | :--- | :--- |
| `solim.core` | `Component`, `BaseComponent`, `ComponentContext`, `Disposable`, `EventsUtil` | Core component contracts, lazy single-build lifecycle, ambient resource tracking, event subscription lifecycles. |
| `solim.signal` | `Signal`, `Computed`, `Readable`, `Effect`, `ReactiveContext`, `ReactiveObserver` | Fine-grained dependency tracking, reactive primitives, dynamic reactivity graph, loop detection. |
| `solim.layout` | `Column`, `Row`, `Card`, `Grid`, `ReactiveGrid`, `Scroll`, `Tabs`, `SizeConstraints`, `SizedTable`, `LayoutModifiers` | Structural containers, CSS-like box sizing constraints, scroll boundary triggers, keyed grid reflow. |
| `solim.modifier` | `ElementModifiers` | Static utility modifiers: positioning, padding, margin, opacity, visibility, drag-and-drop handles. |
| `solim.ui` | `Ui`, `ParentStack`, `Binding`, `Units`, `Dynamic`, `ForEach`, `ElementResolver` | Declarative UI facade, ambient table nesting stack, property binders, dynamic viewport units (`dvw`/`dvh`). |
| `solim.input` | `Button`, `SolimTextField`, `Checkbox`, `SolimSlider`, `SolimSelect`, `Switch` | Interactive input components with two-way and one-way reactive state synchronization. |
| `solim.display` | `Text`, `SolimImage`, `NetworkImage`, `Badge`, `Icon` | Presentational components, reactive labels, async cached network images, pill counters. |
| `solim.overlay` | `Hud`, `SolimDialog`, `Popup` | Non-modal floating HUDs with viewport clamping, modal dialogs extending `BaseDialog`. |
| `solim.feedback`| `ProgressBar`, `Alert`, `Avatar`, `Spinner` | Status and progress indicators. |
| `solim.style` | `Style`, `StyleBinding`, `Styles` | Declarative styling records, property appliers, and predefined style themes. |

---

## 3. Reactive State System (`solim.signal`)

Solim's reactive system is a push-pull dependency-tracking graph designed for single-threaded UI runtimes (Mindustry main loop). It does not use `ThreadLocal`, eliminating threading overhead.

```text
┌──────────────┐         reads          ┌──────────────┐
│  Signal<T>   │ ◄───────────────────── │ Computed<R>  │
└──────┬───────┘                        └──────┬───────┘
       │                                       │
       │ notifies                              │ notifies
       ▼                                       ▼
┌──────────────────────────────────────────────────────┐
│            ReactiveObserver (Effect / Computed)      │
│      - records dependencies during execution         │
│      - receives invalidation on source change        │
└──────────────────────────┬───────────────────────────┘
                           │ runs
                           ▼
┌──────────────────────────────────────────────────────┐
│            Direct Arc Element Property Mutation      │
│   (e.g., label.setText(), cell.width(), el.visible)  │
└──────────────────────────────────────────────────────┘
```

### 3.1 `Readable<T>`
Common functional interface representing any read-only source (static value, `Signal<T>`, or `Computed<T>`).
- `T get()`: Returns the current value and registers dependency if inside an active `ReactiveObserver`.
- `T peek()`: Returns the current value **without** dependency tracking. Intended for one-time reads (e.g., button click handlers).
- `<R> Computed<R> map(Function<T, R> mapper)`: Returns a lazily evaluated derived `Computed`.
- Static helpers: `Readable.of(T value)`, `Readable.from(Supplier<T> supplier)`.

### 3.2 `Signal<T>`
Mutable reactive value.
- `Signal.of(T initial)`: Instantiates a signal.
- `Signal.fromCallback(Consumer<Runnable> registrar, Supplier<T> supplier)`: Bridges external event listeners (e.g., `element::resized`) to reactive signals.
- `void set(T newValue)`: Updates value. Uses `Objects.equals(value, newValue)` equality check to prevent unnecessary down-stream notifications.
- `void update(Function<T, T> updater)`: Functional update shorthand.
- `Subscription subscribe(Consumer<T> listener)`: Subscribes an explicit callback; returns a disposable unsubscription handle.
- **Anti-Pattern Guard:** If `signal.get()` is invoked during `BaseComponent.build()` while outside any `Effect` or `Computed`, Solim logs a warning alerting the developer that reactivity is severed and suggests passing the `Readable` directly or using `.peek()`.

### 3.3 `Computed<T>`
Lazily-evaluated, memoized derived reactive value implementing `ReactiveObserver` and `Readable<T>`.
- **Dynamic Dependency Tracking:** Uses `ReactiveContext.push(this)` during evaluation. If conditional branches alter the signals read, old subscriptions are unsubscribed and new ones registered.
- **Cycle Detection:** Throws `IllegalStateException("Cycle detected in Computed")` if evaluation recurses into itself.
- **Dirty Propagation:** When an upstream dependency changes, `invalidate()` marks the `Computed` as dirty and eagerly notifies downstream listeners only if actively observed.

### 3.4 `Effect`
Auto-tracking side-effect runner implementing `ReactiveObserver` and `Disposable`.
- Runs immediately upon creation, recording all dependencies accessed during execution.
- Re-executes whenever any tracked dependency changes.
- **Cleanup Handlers:** Supports cleanup execution before re-running or on disposal via `Effect.of(cleanup -> { cleanup.add(runnable); })` or `Effect.of(() -> { return () -> cleanup(); })`.
- Registered automatically with the ambient building component via `ComponentContext.register(effect)`.

### 3.5 `ReactiveContext`
Static stack (`Deque<ReactiveObserver>`) managing active observers.
- `track(Object observable)`: Called from `Signal.get()` or `Computed.get()` to register the observable dependency on the current top-of-stack observer.
- `untracked(Supplier<T>)` / `untracked(Runnable)`: Temporarily suspends tracking, allowing untracked reads inside observers or constructors.

---

## 4. Component Architecture & Lifecycle (`solim.core`)

### 4.1 The `Component` Interface
The base abstraction for all Solim UI constructs:
```java
public interface Component {
    Element element();
    default void dispose() {}
    default Component name(String name) {
        ElementModifiers.name(element(), name);
        return this;
    }
}
```

### 4.2 `BaseComponent`
Abstract base class providing lazy single-build semantics, ambient lifecycle management, and automatic child registration.

```text
new BaseComponent()
       │
       ├─► Registers with ambient ComponentContext.current() (Parent Component)
       └─► Registers with ParentStack.current() as pending child
       
parent.element() or ParentStack.pop() triggers:
       │
       ├─► ComponentContext.push(this)
       ├─► ParentStack.isolate(this::build)  <-- builds Arc hierarchy once
       ├─► Assigns default or explicit debug element name
       ├─► ComponentContext.pop()
       └─► Caches root Element (subsequent calls return cached)
       
parent.dispose() or component.dispose()
       │
       ├─► Calls onDispose() subclass hook
       ├─► Disposes all registered Disposable instances in reverse order
       ├─► Disposes all child Components
       └─► Clears disposables list (idempotent; guarded by disposed flag)
```

#### Key Characteristics:
1. **Lazy Execution:** `build()` runs exactly once when `element()` is first accessed.
2. **Ambient Resource Tracking:** When `new BaseComponent()` is instantiated inside another component's `build()`, `ComponentContext.registerChild(this)` ensures the child component's lifecycle is tied to the parent.
3. **Event Integration:** Provides `listen(Class<T> eventType, Cons<T> listener)` and `createSignal(Class<E> eventType, ...)` which automatically unregister Arc event listeners upon component disposal.
4. **Isolated Construction:** Uses `ParentStack.isolate(...)` around `build()` so inner components do not inadvertently attach to external tables.

### 4.3 `ComponentContext`
Maintains an ambient call-stack of `BaseComponent` instances during `build()`.
- `push(BaseComponent)` / `pop()`: Enters and exits component building scope.
- `register(Disposable)`: Enrolls an arbitrary disposable (reactive binding, effect, listener) into the currently active component.
- `registerChild(Component)`: Automatically attaches child component disposal to the parent.
- `pause()` / `resume()`: Pauses automatic ambient registration (used by structural containers like `ReactiveGrid` and `ForEach` to manage child lifecycles explicitly).

---

## 5. Declarative UI Construction (`solim.ui`)

Solim eliminates imperative widget assembly via an ambient nesting system.

### 5.1 `ParentStack`
An internal ambient stack tracking the currently active parent `Table` and its associated `Attacher`.
```java
@FunctionalInterface
public interface Attacher {
    Cell<?> attach(Table parent, Element child);
}
```

#### Workflow:
1. When a container (`Column`, `Row`, `Card`, `Scroll`) calls `.children(Runnable r)`, it pushes its internal table and container-specific attacher onto `ParentStack`.
2. As child components (`text()`, `button()`, `image()`) are instantiated inside `r`, they are registered or immediately attached via `ParentStack.attachToParent(child)`.
3. When the container block finishes, `ParentStack.pop()` attaches any pending components, runs container layout post-processing (e.g., applying size constraints and padding), and pops the table.
4. `ParentStack.isolate(Supplier<T>)`: Clears the parent stack temporarily so that subtrees can be constructed in total isolation without accidental attachment.

### 5.2 The `Ui` Entry Facade
Static utility class exposing all declarative layout and widget constructors:
- **Containers:** `column()`, `row()`, `card()`, `grid()`, `scroll()`, `tabs()`, `container()`, `stack()`
- **Widgets:** `button()`, `text()`, `image()`, `icon()`, `networkImage()`, `textField()`, `slider()`, `checkbox()`, `badge()`
- **Overlays:** `dialog()`, `hud()`
- **Structural Reactivity:** `dynamic()`, `forEach()`, `grid(columnCount, items, ...)`
- **Primitives:** `spacer()`, `divider()`, `add(child)`
- **Units & Scaling:** `unit(float)` ($n \times 4\text{px}$ standard grid), `dvw()`, `dvh()`

---

## 6. Layout System & CSS-Style Box Sizing (`solim.layout`)

Solim standardizes layout behavior through a unified sizing abstraction rather than exposing raw Arc Scene2D `Cell` quirks.

### 6.1 `LayoutModifiers<SELF>`
A shared mixin interface implemented by `Column`, `Row`, `Card`, `Grid`, `ReactiveGrid`, `Scroll`, `Tabs`, `Hud`, and `Dynamic`. It provides a fluent, chainable configuration API:

| Category | Methods | Description |
| :--- | :--- | :--- |
| **Preferred Size** | `width(float/Readable)`, `height(...)`, `size(...)` | Maps to CSS `width`/`height` (preferred dimension). |
| **Bound Constraints**| `minWidth(...)`, `minHeight(...)`, `maxWidth(...)`, `maxHeight(...)` | Maps to CSS `min-width`, `max-width`, etc. |
| **Flex Growth** | `growX()`, `growY()`, `grow()` | Flex-grow flags independent of explicit width/height. |
| **Alignment** | `center()`, `top()`, `bottom()`, `left()`, `right()` | Aligns element within parent container cell. |
| **Margins** | `margin(float/Readable)`, `marginTop(...)`, etc. | Outer margins applied to parent cell padding. |
| **Opacity** | `opacity(float/Readable)`, `alpha(...)` | Visual alpha transparency binding. |

### 6.2 `SizeConstraints` & `SizedTable`
Arc tables compute preferred sizes based on child element intrinsic sizes. Solim overrides this with CSS box-model semantics:
- `SizeConstraints`: Value object storing static or reactive dimensions (`prefWidth`, `minWidth`, `maxWidth`, `growX`, etc.).
- `SizedTable`: Custom `Table` subclass implementing `ConstrainedElement`. It overrides `getPrefWidth()`, `getPrefHeight()`, `getMinWidth()`, and `getMaxWidth()` to prioritize `SizeConstraints` values over Arc defaults while clamping between min and max bounds.
- `applyToCell(Cell<?> cell)`: Called when an element is added to a parent table. Automatically binds reactive size constraints via `Effect` so that when a signal changes, `cell.width(...)` or `cell.height(...)` is updated and `cell.getTable().invalidateHierarchy()` is triggered.

### 6.3 Standard Layout Containers

#### `Column` & `Row`
- `Column`: Vertical table with `Column.ATTACHER` (`cell.row()`, auto `cell.growY()` if child is expanding).
- `Row`: Horizontal table with `Row.ATTACHER` (auto `cell.growX()` if child is expanding).
- Support `.gap(float)`: Distributes uniform padding across all children via `table.defaults().pad(gap / 2f)` and adjusts existing cells.

#### `Card`
A clickable, stylable container combining a custom `CardButton` (implementing `ConstrainedElement`) and an inner container `Table`.
- Event bubbling protection: If an inner child stops an input event (`event.stop()`), the card's `onClick` does not fire.
- Supports reactive background colors, borders, and button styles.

#### `Grid`
Grid layout with fixed or reactive column counts:
- `grid(int cols)` / `grid(Readable<Integer> cols)`.
- When column count changes, it reflows children across rows dynamically without destroying child instances.

#### `Scroll`
Wraps Arc's `ScrollPane` and content `Table` with pagination and boundary detection:
- `onReachTop(float thresholdPx, Runnable callback)`: Fires callback when scrolled within threshold of top.
- `onReachBottom(float thresholdPx, Runnable callback)`: Fires callback when scrolled near bottom (critical for chat views and infinite scrolling).
- `scrollToTop()`, `scrollToBottom()`.

#### `Tabs`
Declarative tab control:
- Takes `Signal<Integer> activeTab`.
- Automatically builds tab button headers and switches content visibility via a `SolimStack` without destroying off-screen tab hierarchies.

#### `Spacer` & `Divider`
- `Spacer`: Expands to fill available space (`table.userObject = "expanding"`, `cell.grow()`).
- `Divider`: 1.5px line in `Direction.X` or `Direction.Y`, pre-tinted with translucent white.

---

## 7. Structural Reactivity (`solim.ui`)

Property reactivity updates existing widgets in-place. However, dynamic lists or conditional views require structural changes. Solim uses **targeted structural reconciliation** instead of full-tree rebuilds.

### 7.1 `Dynamic<T>`
Switches child subtrees when a reactive source `Readable<T>` emits a new value:
```java
dynamic(selectedTabSignal, tab -> {
    switch (tab) {
        case HOME: return new HomeView();
        case CHAT: return new ChatView();
        default:   return null;
    }
});
```
- Disposes the previous component and its internal bindings before mounting the new one.
- Cleans up child elements in its container table without touching sibling elements.

### 7.2 `ForEach<T, K>` & `ReactiveGrid<T, K>`
Keyed reactive collections for lists and grids.
```java
grid(columnCountSignal, featuresSignal, Feature::id, FeatureCard::new)
    .gap(unit(2))
    .empty(() -> text("No features found"));
```

#### Keyed Reconciliation Algorithm:
1. Subscribes to `items.get()` and `columnCount.get()`.
2. Extracts a key K for each item using `keyExtractor`.
3. Checks `activeComponents` cache:
   - **Existing Key:** The component is preserved in memory; its position in the grid/list is updated.
   - **New Key:** Calls `itemFactory` to instantiate the new component.
   - **Removed Key:** Identifies items present in the previous render but absent in the new list; invokes `comp.dispose()` to tear down bindings and frees resources.
4. **Reflow:** For `ReactiveGrid`, cells are re-added to the table respecting the dynamic column count and gap spacing. Empty states (`empty(Runnable)` or `emptyView(Supplier<Component>)`) are mounted when the collection is empty.
5. **Context Isolation:** Ambient registration is paused during child instantiation (`ComponentContext.pause()`) so the collection container owns the items directly rather than the outer parent.

---

## 8. Modifiers & Drag-and-Drop System (`solim.modifier`)

### 8.1 `ElementModifiers`
A static utility class that bridges Solim properties to Arc elements:
- **Geometry & Hierarchy:** `width`, `height`, `size`, `position`, `x`, `y`, `name`
- **Visibility & Transparency:** `visible(element, Readable<Boolean>)`, `opacity(element, Readable<Float>)`
- **Spacing:** `padding(...)`, `margin(...)`, `gap(...)` (applies directly to `Table` or resolves to parent `Cell.pad(...)` for non-table elements)

### 8.2 Drag-and-Drop Mechanics
Solim provides a built-in drag engine for HUDs and overlays:
```java
ElementModifiers.draggable(dragHandleElement, targetHud, xSignal, ySignal);
```
- Listens to touch events (`InputListener`) on the drag handle.
- Calculates stage deltas (`event.stageX`, `event.stageY`) or local deltas.
- Translates target element position via `element.moveBy(dx, dy)`.
- Clamps position inside the visible viewport using `Hud.keepInScreen()`.
- Synchronizes updated coordinates back into `xSignal` and `ySignal`.

---

## 9. Input & Interactive Components (`solim.input`)

Input widgets provide two-way binding with signals, protected against infinite feedback loops.

```text
┌─────────────────┐    User Input (type/drag)     ┌─────────────────┐
│   Arc Widget    │ ────────────────────────────► │    Signal<T>    │
│ (TextField/etc) │ ◄──────────────────────────── │                 │
└─────────────────┘      Effect Sync (set value)   └─────────────────┘
                                   ▲
                                   │ Equality Guard
                       (only updates if value changed)
```

### 9.1 `Button`
A pure container button supporting children composition:
- Subclass `SizedButton` implements `ConstrainedElement`.
- `button(() -> onAction()).children(() -> { image(icon); text("Label"); })`.
- Gestures: `onClick`, `onLongClick(durationMs, callback)`.
- Event Propagation: `stopClickPropagation(true)` (enabled by default to prevent clicks from bleeding through overlay containers).
- Reactive States: `enabled(Readable<Boolean>)`, `checked(Readable<Boolean>)`, `visible(Readable<Boolean>)`, `tooltip(Readable<String>)`.

### 9.2 `SolimTextField`
Two-way bound text input for `Signal<String>`:
- **Feedback Loop Protection:** Uses internal `updating` flag and string equality checks to prevent change-event ping-pong.
- **Validation:** Built-in `validator(Predicate<String>)` exposing a reactive `Signal<Boolean> valid()`.
- Keyboard Actions: `onSubmit(Consumer<String>)`, `onEnter(Runnable)`.

### 9.3 `Checkbox` & `Switch`
- `Checkbox`: Two-way synchronization with `Signal<Boolean>` or callback-driven (`initial, onChanged`).
- `Switch`: Compact visual toggle button bound to `Signal<Boolean>`.

### 9.4 `SolimSlider`
Two-way bound slider supporting `Signal<Float>` or `Signal<Integer>` with range, step size, and change thresholds.

---

## 10. Display & Feedback Components (`solim.display`, `solim.feedback`)

### 10.1 `Text`
High-level label wrapper backed by `SizedLabel`:
- Direct binding to `Readable<String>`, `Readable<Color>`, `Readable<Float>` (font scale).
- Features: `wrap(true)`, `ellipsis(true)`, text alignment (`left()`, `center()`, `right()`), and margin/padding.

### 10.2 `SolimImage`
High-level image wrapper backed by `SizedImage`:
- Supports Arc `Drawable`, `Scaling` modes, color tints, reactive dimensions, padding, and dragging.

### 10.3 `NetworkImage`
Asynchronous image loader for remote HTTP/HTTPS image URLs:
- **Concurrent In-Memory Caching:** Uses `ConcurrentHashMap<String, TextureRegionDrawable>` to prevent duplicate network requests.
- **Main Thread Handoff:** Network bytes are fetched asynchronously, decoded into `Pixmap`/`Texture`, and posted back to Arc's render thread via `Core.app.post()`.
- Fallbacks: Supports custom placeholder drawables, error drawables, and pluggable `ImageLoader` implementations.

### 10.4 `Badge`
Pill-shaped badge for notification counters and status tags:
- Backed by `SizedTable` with customizable background and padding.
- `ofCount(Readable<Integer> count)`: Features `hideOnZero(true)` which automatically hides the badge element when the counter reaches 0.

### 10.5 `ProgressBar`
Simple bar indicator bound to `Signal<Float>` (0.0 to 1.0) with smooth interpolation.

---

## 11. Overlay & Dialog System (`solim.overlay`)

### 11.1 `Hud`
Floating, non-modal overlay designed for in-game HUDs (e.g., Quick Access toolbars, resource monitors):
- **Click-Through Transparency:** Root `HudRootTable` defaults to `Touchable.childrenOnly`, allowing player game clicks to pass through empty regions. Content is placed inside an enabled child container.
- **Viewport Clamping (`keepInScreen()`):** Automatically handles window resizing (`ResizeEvent`) and repositioning to guarantee the HUD never slips off-screen.
- **Coordinate Synchronization:** Binds bidirectionally with `xSignal` and `ySignal` during drag-and-drop.

### 11.2 `SolimDialog`
Extends Mindustry's native `BaseDialog` while adding Solim declarative construction:
- `.content(Runnable contentBuilder)`: Builds dialog content using standard Solim layout blocks.
- Action Buttons: Fluent helpers `.actionButton(text, action)` for the standard bottom button bar.
- Lifecycle: Implements Solim `Disposable` and Arc `Disposable`. Closing the dialog triggers disposal of all registered effects, child components, and event listeners.

---

## 12. Styling & Theming (`solim.style`)

Solim avoids complex CSS runtime engines in favor of simple, immutable style records:
- `Style`: Immutable configuration containing foreground color, background color, padding, and flags (`ghost`, `primary`). Built using `Style.builder()`.
- `Styles`: Predefined default presets (`PRIMARY`, `GHOST`, `BLACK6`).
- `StyleBinding`: Applies style properties to widgets either statically or reactively via `Signal<Style>` / `Computed<Style>`.

---

## 13. Viewport Units & Responsive Design (`solim.ui.Units`)

Solim provides reactive viewport units modeled after modern CSS viewport units:
- `Units.dvw`: Reactive signal equal to 1% of the current viewport width in Arc scene units.
- `Units.dvh`: Reactive signal equal to 1% of the current viewport height in Arc scene units.
- `Units.update()`: Synchronizes automatically on Mindustry's `ResizeEvent`.
- `Ui.unit(float n)`: Standard spacing multiplier ($n \times 4\text{px}$) used throughout Mindustry UI design.

---

## 14. Arc / Mindustry Runtime Integration

| Mindustry / Arc Subsystem | Solim Integration Layer |
| :--- | :--- |
| **Scene Tree (`Core.scene`)** | Components produce standard `arc.scene.Element` instances that mount directly to the stage. |
| **Event Bus (`Events`)** | `BaseComponent.listen(...)` and `EventsUtil` register lifecycle-safe event listeners that auto-dispose. |
| **Graphics & Scaling (`Scl`, `Core.graphics`)** | `Units` queries `Scl.scl()` and `Core.graphics` to provide resolution-independent viewport metrics. |
| **Game Threading** | Effects and signals execute on Arc's main update thread. Async tasks (`NetworkImage`) dispatch UI mutations through `Core.app.post()`. |
| **Localization (`Core.bundle`)** | All user-visible strings are loaded via `Core.bundle.get(key)` or `Core.bundle.format(key, ...)`. |
| **Textures & Skins (`Core.atlas`, `Styles`, `Tex`)** | Native drawables and styles (`Tex.whiteui`, `Styles.clearNonei`, `Icon.*`) pass directly to Solim components. |

---

## 15. Developer Experience & Usage Patterns

Inspecting the main project codebase reveals how Solim is used in practice.

### 15.1 Pattern: Feature Setting Dialog
```java
public class FeatureSettingDialog extends SolimDialog {
    public FeatureSettingDialog(Feature feature) {
        super(feature.getName());
        
        children(() -> {
            column().grow().padding(unit(4)).gap(unit(2)).children(() -> {
                text(feature.getDescription()).color(Color.lightGray);
                
                divider();
                
                row().growX().children(() -> {
                    text("Enable Feature").growX();
                    checkbox("", feature.enabledSignal());
                });
            });
        });
        
        actionButton(Core.bundle.get("close"), this::hide);
    }
}
```

### 15.2 Pattern: Reactive HUD Overlay
```java
public class QuickAccessHudView extends BaseComponent {
    private final QuickAccessFeature feature;

    @Override
    protected Element build() {
        Readable<Float> scale = feature.scaleConfig.signal();
        Readable<Float> buttonSize = scale.map(s -> unit(10) * s);
        Readable<List<Feature>> activeFeatures = feature.activeFeaturesSignal();

        return hud(() -> {
            button()
                .size(buttonSize)
                .children(() -> image(Icon.move))
                .draggable(feature.xSignal, feature.ySignal);

            divider(Direction.Y);

            grid(feature.columnsSignal(), activeFeatures, Feature::id, f ->
                button(() -> f.getMainDialog().show())
                    .size(buttonSize)
                    .tooltip(f.getName())
                    .children(() -> image(f.getIcon()))
            ).gap(unit(1));
        })
        .opacity(feature.opacityConfig.signal())
        .position(feature.xSignal, feature.ySignal)
        .element();
    }
}
```

### 15.3 Critical Best Practices & Guardrails
1. **Never Call `signal.get()` in `build()`:** Calling `.get()` extracts a static snapshot and breaks reactivity. Always pass the `Readable`/`Signal` directly or use `.map(...)`. For intentional one-off reads, use `.peek()`.
2. **Children Block Last:** Keep layout and style modifiers (`size`, `padding`, `gap`, `color`) visually grouped before the `.children(() -> ...)` declaration.
3. **No Unnecessary State Duplication:** Maintain a single reactive source of truth (e.g. `Signal<T>`), avoiding paired primitive booleans and signals.
4. **Automatic Resource Ownership:** Let `ComponentContext` manage disposables and child lifecycles; avoid manual `own()` calls unless constructing decoupled background workers.
5. **No Full Rebuilds for Property Changes:** Use structural components (`Dynamic`, `ForEach`, `ReactiveGrid`) only when elements are added or removed; use property bindings for styling, sizing, and text.