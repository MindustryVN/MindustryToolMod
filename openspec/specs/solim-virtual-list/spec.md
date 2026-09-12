# solim-virtual-list Specification

## Purpose
Reusable Solim virtual list layout primitive that measures item heights and mounts only elements intersecting the visible viewport plus an overscan buffer inside a scroll container.

## Requirements

### Requirement: Reusable virtual list layout component
The Solim layout system SHALL provide a declarative `VirtualList` component (accessible via `solim.UI.virtualList(...)`) that accepts an item collection, key selector, item height provider, and item component builder, and mounts only elements that intersect the visible viewport plus an overscan buffer.

#### Scenario: Mounting only visible items in viewport
- **WHEN** a list of 100 items is supplied to `VirtualList` inside a scroll viewport that can physically fit 10 items
- **THEN** only the visible items plus configured overscan buffer items are instantiated and added to the Scene2D hierarchy, while off-screen items are not mounted

#### Scenario: Overscan buffer preservation
- **WHEN** items are rendered in `VirtualList`
- **THEN** an overscan buffer (default 3 items above and below the visible viewport) is mounted to prevent visual blanking during scrolling

### Requirement: Prefix-sum height indexing and scroll bounds
The `VirtualList` SHALL maintain a prefix-sum array of cumulative item heights, report the total height as its preferred height to the parent `ScrollPane`, and locate visible items in $O(\log N)$ time using binary search.

#### Scenario: Total scrollable height reflects all items
- **WHEN** items with variable or fixed heights are loaded into `VirtualList`
- **THEN** `getPrefHeight()` returns the exact sum of all item heights plus spacing, allowing the `ScrollPane` scrollbar to reflect the full content extent

#### Scenario: Binary search visible window lookup
- **WHEN** the scroll position changes to an arbitrary offset $Y$
- **THEN** the component computes the first and last visible item indices in $O(\log N)$ time using binary search on the cumulative height array

### Requirement: Viewport scroll synchronization
The `VirtualList` SHALL monitor `ScrollPane` visual scroll updates and reconcile active mounted child components without rebuilding or recreating unchanged items.

#### Scenario: Scrolling updates mounted elements smoothly
- **WHEN** the user scrolls the viewport down by several item heights
- **THEN** items scrolling out of view past the overscan buffer are unmounted, newly visible items are mounted, and previously mounted items that remain in the visible window are retained

### Requirement: Container width invalidation
The `VirtualList` SHALL detect changes to container width and trigger re-measurement of variable-height items and rebuild of the prefix-sum array.

#### Scenario: Width resize recalculates layout
- **WHEN** the container width changes due to window or panel resizing
- **THEN** variable-height items are re-measured against the new width, the prefix-sum array is updated, and the visible window is re-computed
