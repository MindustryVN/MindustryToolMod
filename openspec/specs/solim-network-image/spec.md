# solim-network-image Specification

## Purpose
Provides asynchronous texture fetching and rendering with in-memory caching and reactive URL binding.
## Requirements
### Requirement: Declarative Network Image Component
The system SHALL provide a NetworkImage component (solim.display.NetworkImage, Ui.networkImage) that asynchronously fetches an image from an HTTP/HTTPS URL and displays it in the Solim UI tree.

#### Scenario: Async image loaded successfully
- **WHEN** a valid image URL is supplied to 
etworkImage(url)
- **THEN** the component initially renders the placeholder drawable and transitions to the downloaded texture once the network response is received.

#### Scenario: Image download failure fallback
- **WHEN** an image URL fails to load or returns an HTTP error
- **THEN** the component falls back to the configured fallback drawable without throwing unhandled exceptions.

### Requirement: In-Memory Texture Caching
The system SHALL maintain an in-memory texture cache for downloaded images to avoid redundant network requests and visual flickering.

#### Scenario: Cached image display
- **WHEN** multiple components or repeat requests load an image URL that is already cached
- **THEN** the component retrieves the texture from cache immediately on the main thread without re-triggering an HTTP fetch.

### Requirement: Reactive URL Binding
The system SHALL support dynamic image updates when provided a Readable<String> URL source.

#### Scenario: URL signal change
- **WHEN** the underlying URL signal changes to a new image address
- **THEN** the component updates its display to fetch and render the new image.

### Requirement: Enforced Size Constraints Overriding Intrinsic Texture Dimensions
The `NetworkImage` component and underlying `SizedImage` SHALL respect explicit size constraints and preferred dimensions, preventing downloaded image dimensions from overriding configured sizes.

#### Scenario: Network image loaded with explicit size
- **WHEN** `networkImage(...).size(w, h)` is specified and a network image texture is loaded
- **THEN** the component's preferred width and height remain fixed at `w` and `h` rather than expanding to the texture's native dimensions.

