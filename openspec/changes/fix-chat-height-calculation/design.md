## Context

The virtualized chat message list (VirtualList) depends on ChatMessageHeightCalculator to pre-calculate the height of message groups before rendering. Inaccuracies between the calculated height and the actual rendered element height in the Arc UI tree cause visual jumps, jittery scrolling, misaligned scroll offsets, and inaccurate scroll bounds.

Currently, MessageGroupView does not explicitly set or adhere to the padding and height constants defined in ChatMessageHeightCalculator (such as MESSAGE_CARD_PADDING, HEADER_HEIGHT, and REPLY_PREVIEW_HEIGHT). In addition, ChatMessageHeightCalculator makes assumptions about element paddings that do not correspond to the actual component tree in MessageGroupView.

## Goals / Non-Goals

**Goals:**
- Achieve 100% mathematical and layout agreement between ChatMessageHeightCalculator.calculateHeight and the real rendered element height (getPrefHeight() / layout height) of MessageGroupView.
- Synchronize MessageGroupView UI element dimensions by explicitly applying the constants defined in ChatMessageHeightCalculator (header height, message card padding, reply preview height, gaps, container padding).
- Unify available text width and wrapped text height calculations with Arc's Label layout engine.
- Provide comprehensive automated tests that construct the real MessageGroupView component, mount it in an element tree, measure real layout height, and assert exact parity with ChatMessageHeightCalculator.calculateHeight for all text message conditions.

**Non-Goals:**
- Modifying non-text message layout implementations (Schematic, Image, ToolLink, Invite) beyond aligning their fixed constants.
- Changing ChatMessageGrouper message grouping algorithms.
- Modifying backend APIs, network clients, or chat service layers.

## Decisions

### Decision 1: Explicit UI Height and Padding Enforcement in MessageGroupView
Configure MessageGroupView elements directly with the constants from ChatMessageHeightCalculator:
- **Header row**: explicitly set .height(ChatMessageHeightCalculator.HEADER_HEIGHT).
- **Reply preview row**: explicitly set .height(ChatMessageHeightCalculator.REPLY_PREVIEW_HEIGHT).
- **Message item cards**: explicitly configure vertical padding .padding(ChatMessageHeightCalculator.MESSAGE_CARD_PADDING / 2f) (4px top, 4px bottom = 8px vertical padding).
- **Group container**: explicitly apply UNIT_1 (4px top, 4px bottom = 8px vertical padding = UNIT_1 * 2).

*Rationale*: Rather than relying on implicit or emergent layout dimensions from nested Arc tables and labels, explicitly enforcing these heights on container rows guarantees exact synchronization between the layout calculator and the UI tree.

### Decision 2: Harmonized Available Text Wrap Width
Align ChatMessageHeightCalculator.HORIZONTAL_PADDINGS with the exact horizontal insets of MessageGroupView:
- Outer group card padding: unit(1) left + unit(1) right = 8px.
- Avatar width: unit(12) = 48px.
- Avatar gap: unit(1.5f) = 6px.
- Message card internal padding and margins.
- Scrollbar allowance: 16px.
Ensure the text wrapping width passed to GlyphLayout in measureTextHeight matches the exact width assigned to Label in Arc's table cell.

### Decision 3: Text Height Measurement Parity with Arc Label
Arc's Label.getPrefHeight() calculates height using GlyphLayout with font descent compensation:
loat descentCorrection = -font.getDescent() * 2f;
ChatMessageHeightCalculator.measureTextHeight must replicate this exact calculation. For test environments, initialize Fonts.def with consistent font data so that both ChatMessageHeightCalculator and Arc's Label measure text using identical font metrics.

### Decision 4: Real Element Tree Measurement in Tests
Instead of asserting synthetic arithmetic formulas in isolation, tests must:
1. Initialize mock Arc application, graphics, and font context.
2. Instantiate the real MessageGroupView Solim component.
3. Attach it to an Arc container table with a defined container width and trigger alidate() / layout().
4. Measure the resulting layout height (getPrefHeight() / table height).
5. Compare the measured height directly against ChatMessageHeightCalculator.calculateHeight(group, width) across all text message conditions:
   - Single short message (avatar bound)
   - Multi-line wrapped text (content bound)
   - Empty / blank text
   - Mentioned text (isMentionsCurrentUser)
   - Reply preview (eplyTo)
   - Reply preview with multi-line text
   - Consecutive multi-message group (message gap accumulation)
   - Variable container widths (narrow vs wide)

## Risks / Trade-offs

- **[Risk]** Font differences between test environment and Mindustry runtime.
  → **Mitigation**: ChatMessageHeightCalculator dynamically accesses Fonts.def. The test suite sets up Fonts.def with standard metrics, and the calculator's text height formula directly delegates to Arc's GlyphLayout with descent compensation identical to Arc's Label.

- **[Risk]** Dynamic UI state (such as failed message retry buttons) altering height unexpectedly.
  → **Mitigation**: Keep normal rendered message height deterministic; failed/pending states can either preserve standard heights or recalculate when state transitions occur.
