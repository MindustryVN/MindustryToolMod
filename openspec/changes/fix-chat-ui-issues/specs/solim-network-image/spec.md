## ADDED Requirements

### Requirement: Enforced Size Constraints Overriding Intrinsic Texture Dimensions
The `NetworkImage` component and underlying `SizedImage` SHALL respect explicit size constraints and preferred dimensions, preventing downloaded image dimensions from overriding configured sizes.

#### Scenario: Network image loaded with explicit size
- **WHEN** `networkImage(...).size(w, h)` is specified and a network image texture is loaded
- **THEN** the component's preferred width and height remain fixed at `w` and `h` rather than expanding to the texture's native dimensions.
