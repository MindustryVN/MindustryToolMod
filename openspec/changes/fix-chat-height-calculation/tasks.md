## 1. Synchronize MessageGroupView with Calculator Constants

- [x] 1.1 Set explicit height on the author header row using ChatMessageHeightCalculator.HEADER_HEIGHT
- [x] 1.2 Set explicit height on the reply preview row using ChatMessageHeightCalculator.REPLY_PREVIEW_HEIGHT
- [x] 1.3 Configure vertical padding on message item cards using ChatMessageHeightCalculator.MESSAGE_CARD_PADDING
- [x] 1.4 Harmonize outer container padding and gaps with UNIT_1, HEADER_GAP, and MESSAGE_GAP

## 2. Remake ChatMessageHeightCalculator for 100% Accuracy

- [x] 2.1 Refactor computeHeight to exactly match MessageGroupView element tree structure and bounds
- [x] 2.2 Update HORIZONTAL_PADDINGS and text wrap width calculation to match Arc Label available width
- [x] 2.3 Refine measureTextHeight to mirror Arc Label.getPrefHeight() layout math and descent correction
- [x] 2.4 Align handling for empty text, mention indicators, and reply previews across all conditions

## 3. Real Component Tree Height Tests

- [x] 3.1 Setup Arc test harness with valid font metrics for mounting and laying out MessageGroupView
- [x] 3.2 Implement test measuring real element height vs calculated height for single-line short text (avatar-bound)
- [x] 3.3 Implement test measuring real element height vs calculated height for multi-line wrapped text
- [x] 3.4 Implement test measuring real element height vs calculated height for empty or whitespace text
- [x] 3.5 Implement test measuring real element height vs calculated height for mentioned user text
- [x] 3.6 Implement test measuring real element height vs calculated height for text with reply preview
- [x] 3.7 Implement test measuring real element height vs calculated height for reply preview with multi-line text
- [x] 3.8 Implement test measuring real element height vs calculated height for multi-message groups
- [x] 3.9 Implement test measuring real element height across multiple container widths (narrow vs wide)

## 4. Verification and Compliance

- [x] 4.1 Run automated test suite (./gradlew :mod:test) and ensure 100% pass rate
- [x] 4.2 Verify compliance with AGENTS.md (Java 8 runtime API compatibility, Arc/Solim conventions, nullability)
