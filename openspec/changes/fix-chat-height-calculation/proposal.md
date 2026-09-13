## Why

The virtualized chat message list (VirtualList) depends on ChatMessageHeightCalculator to pre-calculate the height of message groups before rendering. Inaccuracies between the calculated height and the actual rendered element height in the Arc UI tree cause visual jumps, jittery scrolling, misaligned scroll offsets, and inaccurate scroll bounds. Currently, MessageGroupView does not explicitly set or adhere to the padding and height constants defined in ChatMessageHeightCalculator (such as MESSAGE_CARD_PADDING, HEADER_HEIGHT, and REPLY_PREVIEW_HEIGHT), and the calculator lacks 100% accuracy across all text message layout conditions. Furthermore, tests only asserted arithmetic formulas in isolation rather than measuring real UI element heights in the scene tree.

## What Changes

- Remake ChatMessageHeightCalculator to achieve 100% precision with real rendered element heights for all text message conditions (single-line, multi-line wrapping, empty text, reply previews, mentions, multi-message groups, and avatar-constrained heights).
- Align MessageGroupView with ChatMessageHeightCalculator by explicitly using the calculator's height and padding constants across the header row, message item cards, reply preview rows, and container layouts.
- Account for exact padding, gaps, avatar sizes, mention indicators, reply preview heights, and text wrapping widths uniformly in both the calculator and the UI layout.
- Implement comprehensive automated unit tests that instantiate the real MessageGroupView component, mount it in an element hierarchy at various container widths, compute real layout and preferred heights via Arc, and assert exact 1:1 parity with ChatMessageHeightCalculator.calculateHeight.

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- chat-message-group-layout: Tighten requirements on MessageGroup layout height calculation and MessageGroupView element construction to guarantee 100% layout height accuracy between calculated heights and measured UI tree heights across all text message conditions.

## Impact

- Affected code:
  - mindustrytool.features.chat.ChatMessageHeightCalculator: Remade for 100% layout accuracy and font measurement consistency.
  - mindustrytool.features.chat.ChatMessageListView.MessageGroupView: Uses calculator constants for heights, padding, and gaps.
  - mindustrytool.features.chat.ChatMessageGrouperAndHeightTest: Enhanced or supplemented with real UI tree measurement tests covering all text message conditions.
- No breaking changes to public APIs.
