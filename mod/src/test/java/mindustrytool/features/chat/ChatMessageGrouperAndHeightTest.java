package mindustrytool.features.chat;

import static org.junit.jupiter.api.Assertions.*;

import arc.Core;
import arc.mock.MockApplication;
import arc.mock.MockGraphics;
import arc.mock.MockSettings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import mindustrytool.features.chat.models.MessageGroup;
import mindustrytool.models.response.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ChatMessageGrouperAndHeightTest {

    @BeforeEach
    void setUp() {
        Core.app = new MockApplication();
        Core.graphics = new MockGraphics();
        Core.settings = new MockSettings();
        ChatMessageParser.clearCache();
        ChatMessageHeightCalculator.clearCache();
    }

    private static ChatMessage raw(String id, String author, String content) {
        ChatMessage message = new ChatMessage();
        message.setId(id);
        message.setCreatedBy(author);
        message.setCreatedAt("2026-09-12T10:00:00Z");
        message.setContent(content);
        return message;
    }

    private static List<String> idsOf(MessageGroup group) {
        List<String> ids = new ArrayList<>();
        for (var message : group.getMessages()) {
            ids.add(message.getId());
        }
        return ids;
    }

    @Test
    void testGrouperEmpty() {
        assertTrue(ChatMessageGrouper.group(Collections.emptyList()).isEmpty());
        assertTrue(ChatMessageGrouper.groupRaw(Collections.emptyList()).isEmpty());
    }

    @Test
    void testGrouperConsecutiveAuthor() {
        ChatMessage m1 = raw("1", "userA", "Hi 1");
        ChatMessage m2 = raw("2", "userA", "Hi 2");
        ChatMessage m3 = raw("3", "userB", "Hello from B");

        List<MessageGroup> grouped = ChatMessageGrouper.groupRaw(Arrays.asList(m1, m2, m3));
        assertEquals(2, grouped.size());

        // First group: consecutive messages from userA in order
        assertEquals("userA", grouped.get(0).getAuthorId());
        assertEquals(Arrays.asList("1", "2"), idsOf(grouped.get(0)));
        assertEquals("1_2_2", grouped.get(0).getKey());

        // Message from userB starts a new group
        assertEquals("userB", grouped.get(1).getAuthorId());
        assertEquals(Collections.singletonList("3"), idsOf(grouped.get(1)));
        assertEquals("3_1_3", grouped.get(1).getKey());
    }

    @Test
    void testGroupKeyChangesWhenMessageAppended() {
        List<MessageGroup> before = ChatMessageGrouper.groupRaw(Arrays.asList(
                raw("1", "alice", "Hello"),
                raw("2", "alice", "Again")));
        List<MessageGroup> after = ChatMessageGrouper.groupRaw(Arrays.asList(
                raw("1", "alice", "Hello"),
                raw("2", "alice", "Again"),
                raw("3", "alice", "Third")));

        assertEquals(1, before.size());
        assertEquals(1, after.size());
        assertEquals("1_2_2", before.get(0).getKey());
        assertEquals("1_3_3", after.get(0).getKey());
        assertNotEquals(before.get(0).getKey(), after.get(0).getKey());
    }

    @Test
    void testGroupCarriesAuthorAndFirstMessageTimestamp() {
        List<MessageGroup> grouped = ChatMessageGrouper.groupRaw(Arrays.asList(
                raw("1", "alice", "Hello"),
                raw("2", "alice", "Again")));

        MessageGroup group = grouped.get(0);
        assertEquals("alice", group.getAuthorId());
        assertEquals("2026-09-12T10:00:00Z", group.getCreatedAt());
        assertEquals(2, group.getMessageCount());
    }

    @Test
    void testGroupsWithSameContentAreEqual() {
        MessageGroup first = ChatMessageGrouper.groupRaw(
                Collections.singletonList(raw("1", "alice", "Hello"))).get(0);
        MessageGroup second = ChatMessageGrouper.groupRaw(
                Collections.singletonList(raw("1", "alice", "Hello"))).get(0);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void testHeightSingleLineGroupBoundByAvatar() {
        MessageGroup group = ChatMessageGrouper.groupRaw(
                Collections.singletonList(raw("1", "alice", "Hi"))).get(0);

        float height = ChatMessageHeightCalculator.calculateHeight(group, 400f);

        assertTrue(height >= ChatMessageHeightCalculator.AVATAR_SIZE + 8f,
                "Single-line group must be at least avatar height plus padding, was " + height);
    }

    @Test
    void testHeightGroupAccumulatesHeaderBodiesAndGaps() {
        MessageGroup group = ChatMessageGrouper.groupRaw(Arrays.asList(
                raw("1", "alice", "First"),
                raw("2", "alice", "Second"))).get(0);

        float textWidth = Math.max(100f, 400f - ChatMessageHeightCalculator.HORIZONTAL_PADDINGS);
        float first = ChatMessageHeightCalculator.measureTextHeight("First", textWidth, ChatMessageHeightCalculator.FONT_SCALE)
                + ChatMessageHeightCalculator.MESSAGE_CARD_PADDING;
        float second = ChatMessageHeightCalculator.measureTextHeight("Second", textWidth, ChatMessageHeightCalculator.FONT_SCALE)
                + ChatMessageHeightCalculator.MESSAGE_CARD_PADDING;
        float expected = Math.max(ChatMessageHeightCalculator.AVATAR_SIZE,
                ChatMessageHeightCalculator.HEADER_HEIGHT + ChatMessageHeightCalculator.HEADER_GAP
                        + first + second + ChatMessageHeightCalculator.MESSAGE_GAP) + 8f;

        assertEquals(expected, ChatMessageHeightCalculator.calculateHeight(group, 400f), 0.001f);
    }

    @Test
    void testHeightMultiMessageGroupTallerThanSingle() {
        MessageGroup single = ChatMessageGrouper.groupRaw(
                Collections.singletonList(raw("1", "alice", "First"))).get(0);
        MessageGroup multi = ChatMessageGrouper.groupRaw(Arrays.asList(
                raw("1", "alice", "First"),
                raw("2", "alice", "Second"))).get(0);

        assertTrue(ChatMessageHeightCalculator.calculateHeight(multi, 400f)
                > ChatMessageHeightCalculator.calculateHeight(single, 400f));
    }

    @Test
    void testHeightReplyPreviewIncreasesGroupHeight() {
        MessageGroup plain = ChatMessageGrouper.groupRaw(
                Collections.singletonList(raw("1", "alice", "Hello"))).get(0);

        ChatMessage reply = raw("2", "alice", "Hello");
        reply.setReplyTo("other-id");
        ChatMessageParser.clearCache();
        MessageGroup withReply = ChatMessageGrouper.groupRaw(
                Collections.singletonList(reply)).get(0);

        assertTrue(ChatMessageHeightCalculator.calculateHeight(withReply, 400f)
                > ChatMessageHeightCalculator.calculateHeight(plain, 400f));
    }

    @Test
    void testHeightCalculatorCaching() {
        MessageGroup group = ChatMessageGrouper.groupRaw(
                Collections.singletonList(raw("cache-msg", "bob", "Cached test text"))).get(0);

        float first = ChatMessageHeightCalculator.calculateHeight(group, 350f);
        float second = ChatMessageHeightCalculator.calculateHeight(group, 350f);
        assertEquals(first, second, 0.0001f);
    }

    @Test
    void testHeightCalculatorTextWrapNarrowVsWide() {
        MessageGroup group = ChatMessageGrouper.groupRaw(Collections.singletonList(raw("long-msg", "charlie",
                "This is a long message that should wrap to multiple lines when container width is small and fit fewer lines when wide."))).get(0);

        float wide = ChatMessageHeightCalculator.calculateHeight(group, 600f);
        float narrow = ChatMessageHeightCalculator.calculateHeight(group, 150f);

        assertTrue(narrow >= wide, "Narrow width should produce taller or equal height for wrapped text");
    }
}
