## Why

When `ChatOverlayHudView` is opened on mobile devices (`Vars.mobile = true`), framerate drops by approximately half (e.g., from 60 FPS down to 30 FPS or lower). This severely degrades the mobile gameplay experience.

Currently, there is no standardized performance testing tooling or profiling instrumentation for Solim UI to quantify element counts, layout validation cost, reactive dispatch latency, or draw-call overhead. Furthermore, `ChatOverlayHudView` on mobile mounts all three tabs simultaneously into an Arc `Stack` and instantiates unvirtualized message cards for every chat message loaded, causing massive element counts, layout thrashing, and batch flushing.

Solim UI needs both a reliable performance testing/benchmarking framework and targeted optimizations in `ChatOverlayHudView` and `Tabs` to preserve full 60 FPS performance on mobile devices.

## What Changes

- **Solim UI Performance Testing Suite (`solim-ui-perf-testing`)**:
  - Headless benchmarks measuring component instantiation time, layout `validate()` duration, and reactive signal update propagation under heavy component loads (100–1,000 items).
  - Runtime UI profiler and diagnostic metrics tracker capturing Scene2D element count, active binding count, layout invalidation frequency, and frame duration.
  - MCP tool extension to query live Solim UI performance metrics and detect layout thrashing during runtime.
- **Lazy Tab Mounting in `solim-tabs`**:
  - Update `Tabs` to support lazy tab instantiation and deferred component building so hidden tabs do not reside in the active scene tree or consume layout cycles.
- **`ChatOverlayHudView` Mobile Performance Optimizations**:
  - Windowing / viewport capping for `ChatMessageListView`: restrict active DOM/Scene2D element nodes to the visible viewport plus a small buffer, rather than instantiating hundreds of nested `Card`, `SolimStack`, `Badge`, and `NetworkImage` widgets simultaneously.
  - Fix layout invalidation feedback loops in `HudRootTable` and `keepInScreen()`.
  - Batching / texture atlas optimizations for user avatars to eliminate redundant texture binding flushes on mobile GPUs.

## Capabilities

### New Capabilities
- `solim-ui-perf-testing`: Performance benchmarking suite and runtime UI metrics profiling for Solim declarative components, measuring layout time, reactivity overhead, and element tree scale.

### Modified Capabilities
- `chat-overlay`: Add requirements for mobile performance, lazy tab mounting, and viewport-capped message feed rendering to maintain stable 60 FPS when open.
- `solim-tabs`: Add requirement for lazy tab evaluation and inactive tab isolation to prevent inactive tab subtrees from degrading scene performance.

## Impact

- `solim-core`: New performance benchmark test fixtures in `src/test/java/solim/perf/`; additions to `Tabs` for lazy tab instantiation.
- `solim-mcp`: New `get_ui_perf_metrics` MCP tool to inspect live scene element counts, draw times, and invalidation counters.
- `mod`: `ChatOverlayHudView`, `ChatMessageListView`, and `Tabs` usage refactored for viewport windowing, lazy tab mounting, and stable layout sizing without frame drops.
