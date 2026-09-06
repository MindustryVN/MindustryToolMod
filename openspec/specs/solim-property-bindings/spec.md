# solim-property-bindings Specification

## Purpose
TBD - created by archiving change clean-up-solim-refactor. Update Purpose after archive.
## Requirements
### Requirement: Direct Reactive Element Property Bindings
Solim SHALL provide direct property binding mechanisms that mutate existing Arc scene elements when reactive signals or computeds change without requiring standalone `Effect` definitions in user code.

#### Scenario: Binding element width
- **WHEN** an element's width is bound to a reactive `Readable<Float>`
- **THEN** changes to the reactive width immediately update the element's width and invalidate its layout hierarchy without requiring manual Effect management

#### Scenario: Binding element color
- **WHEN** an element's color is bound to a reactive `Readable<Color>`
- **THEN** changes to the reactive color update the element's color in place on the existing element instance

#### Scenario: Binding label text
- **WHEN** an Arc label's text is bound to a reactive `Readable<String>`
- **THEN** changes to the reactive string update the label text directly without reconstructing the label element

### Requirement: Isolation of Arc Layout Lifecycle from Reactive Graph
Arc layout lifecycle methods (`getPrefWidth`, `getPrefHeight`, `layout`, `draw`, `act`) SHALL operate purely on conventional element properties without performing reactive `.get()` reads.

#### Scenario: Arc layout pass execution
- **WHEN** Arc invokes layout passes on Solim-backed elements
- **THEN** the layout passes execute without registering reactive dependencies or triggering unintended effect re-runs

