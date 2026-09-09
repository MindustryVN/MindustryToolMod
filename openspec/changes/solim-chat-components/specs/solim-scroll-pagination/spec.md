## ADDED Requirements

### Requirement: Scroll Boundary Triggers
The system SHALL provide `onReachTop(float thresholdPx, Runnable callback)` and `onReachBottom(float thresholdPx, Runnable callback)` methods on `solim.layout.Scroll`.

#### Scenario: Reaching top threshold triggers pagination
- **WHEN** user scrolls up within `thresholdPx` of the top of the scroll container
- **THEN** the registered `onReachTop` callback is executed to trigger history fetching.

#### Scenario: Edge crossing debouncing
- **WHEN** user remains within the top threshold area during continuous scrolling
- **THEN** the callback is not invoked repeatedly until the user scrolls away and crosses the threshold again.

### Requirement: Scroll Position Binding
The system SHALL support programmatically setting and reading scroll positions on `solim.layout.Scroll`.

#### Scenario: Programmatic scroll to bottom
- **WHEN** `scrollToBottom()` is called on a `Scroll` instance
- **THEN** the underlying scroll pane adjusts its scroll position to the bottom of the content.
