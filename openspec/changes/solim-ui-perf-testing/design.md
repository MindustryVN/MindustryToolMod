## Context

On mobile devices, opening `ChatOverlayHudView` cuts framerate in half (e.g., dropping from 60 FPS to 25–30 FPS). The root causes identified across the Solim and mod codebase are:
1. **Unvirtualized List Scale**: `ChatMessageListView` renders all active channel messages into a single `Table` within a `ScrollPane`. Every message creates a `Card`, `SolimStack`, `ChatAvatar` (`Badge` + `NetworkImage`), multiple `Row`/`Column` containers, labels, and dynamic observers. For 50–100 messages, the scene graph balloons to 2,000–3,500 active `Element` nodes that are traversed during `Scene.act()` and `Scene.draw()`.
2. **Texture Swapping & Draw Call Explosion**: Each loaded network avatar has its own OpenGL texture region. In Arc's `SpriteBatch`, binding a new texture forces a batch flush and GPU draw call. Rendering dozens of individual avatar textures in the chat feed creates severe GPU fill and driver overhead on mobile chips.
3. **Eager Tab Rendering in `Tabs`**: `Tabs.java` immediately executes the `contentBuilder` for Channels, Messages, and Members tabs upon initialization, placing them all in an Arc `Stack`. Even when hidden, these containers consume memory and maintain active signal subscriptions.
4. **Layout Invalidation Loops**: `HudRootTable.validate()` checks `getPrefWidth()` and calls `setSize()` + `hud.keepInScreen()`. If coordinate snapping or signal updates cause `invalidateHierarchy()`, layout validation recurses or repeats every frame.
5. **Lack of Performance Profiling Tools**: Solim currently lacks an automated mechanism to measure layout traversal time, element counts, or draw call impact.

## Goals / Non-Goals

**Goals:**
- Provide a headless benchmark framework in `solim-core` measuring element tree construction, layout validation time, and signal dispatch throughput under heavy workloads (100–1,000 items).
- Provide a lightweight runtime `UiProfiler` in Solim to track live element counts, active bindings, layout invalidation counts per frame, and frame execution times.
- Expose a `get_ui_perf_metrics` MCP tool in `solim-mcp` to inspect UI performance live from running Mindustry instances.
- Optimize `Tabs` with lazy mounting (building tab content only when first activated) and layout isolation (`setLayoutEnabled(false)` on inactive tabs).
- Implement viewport windowing/capping in `ChatMessageListView` on mobile (rendering a bounded window of recent messages, e.g. 25–30 items) to keep the scene element count under 500.
- Eliminate layout thrashing in `HudRootTable` by dampening `keepInScreen()` updates when coordinates are within tolerance.

**Non-Goals:**
- Implementing a full-blown Virtual DOM or complex global diffing engine (violates AGENTS.md rules).
- Rewriting Arc's internal `SpriteBatch` or `Scene` render loops.
- Altering the desktop layout or visual appearance of the chat overlay.

## Decisions

### Decision 1: Headless Performance Benchmarks (`solim-core`)
- **Rationale**: AGENTS.md specifies that Solim framework behavior should be tested independently of Mindustry runtime. We create benchmark test fixtures in `solim-core/src/test/java/solim/perf/` using standard Java `System.nanoTime()` timing over warm-up and measurement iterations.
- **Benchmarks**:
  - `LayoutBenchmarkTest`: measures time to construct and `validate()` trees of 100, 500, and 1,000 nested Solim components.
  - `ReactivityBenchmarkTest`: measures propagation latency for 10,000 Signal -> Computed -> Effect updates.
  - `TabsPerformanceTest`: verifies that inactive tabs with layout disabled take negligible time during parent `validate()`.

### Decision 2: Runtime `UiProfiler` and Solim MCP Tool
- **Rationale**: Real-game performance bottlenecks (like mobile FPS drops) stem from draw calls and scene element counts in the live Mindustry runtime.
- **Design**:
  - Add a lightweight `UiProfiler` class in `solim-core` (or `solim`) that can be activated on demand.
  - Recursively counts total `Element`s, visible elements, `Table` cells, and monitors per-frame `invalidate()` calls.
  - Expose via `solim-mcp` as `get_ui_perf_metrics` returning JSON:
    ```json
    {
      "fps": 60,
      "totalElements": 342,
      "visibleElements": 118,
      "registeredComponents": 45,
      "activeBindings": 82,
      "invalidationsPerFrame": 0
    }
    ```

### Decision 3: Lazy Tab Mounting & Layout Disabling in `Tabs`
- **Rationale**: When `ChatOverlayHudView` opens on mobile, it defaults to the Messages tab (index 1). Channels and Members tabs do not need to be instantiated until the user taps them.
- **Design**:
  - `Tabs` stores `Runnable contentBuilder` and evaluates it on first activation.
  - Inactive tab containers receive `table.setLayoutEnabled(false)` and `table.setVisible(false)`, completely bypassing them during Arc layout traversal.

### Decision 4: Message List Viewport Windowing on Mobile
- **Rationale**: In chat applications, users only see ~6–10 messages at a time on mobile screens. Keeping 100+ fully-rendered cards in the retained Arc scene tree causes massive overhead.
- **Design**:
  - In `ChatMessageListView`, on mobile (`Vars.mobile == true`), cap the active display list to the most recent $N$ messages (e.g., 25–30 items).
  - When the user scrolls near the top, prepend the next slice while maintaining scroll offset.
  - This keeps active Scene2D elements strictly bounded ($\le 500$ elements) regardless of how large the channel message history is.

### Decision 5: Layout Thrashing Protection in `HudRootTable`
- **Rationale**: `HudRootTable.validate()` currently calls `setSize()` and `keepInScreen()` during `validate()`.
- **Design**:
  - Only update size if dimensions differ by more than 1 pixel (`Math.abs(current - target) > 1.0f`).
  - Only fire position signal updates in `keepInScreen()` if position changed beyond a 0.5px threshold.

## Risks / Trade-offs

- **[Risk] Windowed message list might disrupt scroll-to-bottom or pagination**:
  - *Mitigation*: Ensure the windowing logic maintains an anchor element and only slices the list on mobile, leaving desktop behavior identical.
- **[Risk] Profiler overhead during normal gameplay**:
  - *Mitigation*: `UiProfiler` is inactive by default and only runs sampling passes when explicitly queried or enabled.
