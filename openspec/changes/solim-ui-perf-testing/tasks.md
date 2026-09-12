## 1. Solim Headless Benchmarks (solim-core)

- [ ] 1.1 Create `LayoutBenchmarkTest` in `solim-core` measuring component hierarchy instantiation and `validate()` traversal time across 100, 500, and 1,000 items.
- [ ] 1.2 Create `ReactivityBenchmarkTest` in `solim-core` measuring latency and memory stability for 10,000 Signal and Computed update dispatches.
- [ ] 1.3 Create `TabsPerformanceTest` verifying that inactive tabs do not evaluate layout passes or cause parent layout invalidation.

## 2. Solim UI Profiler & Diagnostics

- [ ] 2.1 Implement `UiProfiler` in `solim-core` to recursively inspect Scene2D Element counts, visible nodes, registered components, active bindings, and layout invalidations.
- [ ] 2.2 Add `get_ui_perf_metrics` MCP tool in `solim-mcp` returning real-time JSON performance statistics (FPS, element counts, binding counts, invalidation frequency).

## 3. Tabs Optimization (solim-core)

- [ ] 3.1 Update `Tabs.java` to support lazy tab mounting, executing `contentBuilder` only when a tab is first selected.
- [ ] 3.2 Configure `Tabs.java` to set `setLayoutEnabled(false)` and `setVisible(false)` on all inactive tab containers to isolate layout propagation.

## 4. Chat Overlay Mobile Performance (mod)

- [ ] 4.1 Implement viewport windowing in `ChatMessageListView` for mobile (`Vars.mobile == true`), restricting the active mounted cards to a bounded window of recent messages (25–30 items) while preserving scroll and pagination.
- [ ] 4.2 Add threshold dampening to `HudRootTable.validate()` and `Hud.keepInScreen()` to prevent sub-pixel layout invalidation loops.
- [ ] 4.3 Verify mobile chat overlay maintains stable 55+ FPS when opened and navigated using headless tests and UI profiler.
