# auth-session-signal Specification

## Purpose
Provides a single reactive source of truth for auth session state in `MindustryAuthProvider` — session identity, loading, and error signals with a main-thread update contract — replacing session broadcast events and token-snapshot re-derivation in consumers.

## Requirements
### Requirement: Reactive session source of truth
`MindustryAuthProvider` SHALL expose session identity as a reactive `Signal` (null when no session is loaded) alongside loading and error signals, and SHALL mutate all three exclusively on the main thread. The session broadcast events (`SessionLoadEvent`, `LoginEvent`, `LogoutEvent`) and the unlistened raw session fire SHALL be removed.

#### Scenario: Session fetch publishes through signals on the main thread
- **WHEN** `fetchSession()` completes successfully
- **THEN** the session signal holds the fetched session, loading is false, and all updates were applied on the main thread

#### Scenario: No broadcast events are fired
- **WHEN** session loads, login completes, or logout occurs
- **THEN** no `SessionLoadEvent`, `LoginEvent`, or `LogoutEvent` is fired and no raw session object is fired as an event

### Requirement: Loading and error session states
The provider SHALL expose reactive loading and error states for the session lifecycle: loading is true while a fetch is in flight, and error carries the fetch failure while preserving the previous session.

#### Scenario: Fetch failure preserves session and reports error
- **WHEN** `fetchSession()` fails
- **THEN** the error signal holds the failure, loading is false, and the session signal still holds the previous session

### Requirement: Chat login state derived from session
`ChatStore.loggedIn` SHALL be derived from the session signal (logged in if and only if a session is present) instead of token snapshots and event handlers.

#### Scenario: Cold start with valid tokens
- **WHEN** the app starts with stored tokens but the session fetch has not completed
- **THEN** chat login-gated UI treats the user as logged out until the session arrives

#### Scenario: Session arrival updates chat gating
- **WHEN** the session signal receives a session
- **THEN** chat login-gated UI treats the user as logged in without any event handling

### Requirement: Auth overlay state derived from session signals
`AuthOverlay` SHALL derive its displayed state (loading / error / login prompt / user card) from the session signals without subscribing to session events, rendering identically to before for each state.

#### Scenario: Overlay reflects signal states
- **WHEN** the session signals indicate loading, error, absence, or presence of a session
- **THEN** the overlay shows the loading indicator, error card with retry, login button, or user card respectively

### Requirement: Logout clears session without refetch
`logout()` SHALL remove stored tokens and clear the session signal (and error) directly instead of triggering a session fetch.

#### Scenario: Logout shows login prompt
- **WHEN** the user logs out
- **THEN** the session signal is null, no error state is produced, and the overlay shows the login button

### Requirement: Non-reactive session peek for the chat parser
The provider SHALL offer a non-subscribing session read for non-reactive contexts, and the chat message parser SHALL use it for mention detection and have its cache invalidated when the session changes.

#### Scenario: Late login fixes future mention parses
- **WHEN** messages were parsed while no session was present and a session subsequently arrives
- **THEN** the parser cache is invalidated so subsequent parses evaluate mentions against the current user
