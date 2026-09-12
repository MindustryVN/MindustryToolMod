package mindustrytool.features.chat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mindustrytool.features.chat.models.GroupedMessageItem;
import mindustrytool.features.chat.models.ParsedChatMessage;
import mindustrytool.models.response.ChatMessage;

/**
 * Groups consecutive chat messages from the same author, marking first-in-group and last-in-group
 * flags for layout and height calculation.
 */
public final class ChatMessageGrouper {

    private ChatMessageGrouper() {
    }

    public static List<GroupedMessageItem> groupRaw(List<ChatMessage> rawMessages) {
        if (rawMessages == null || rawMessages.isEmpty()) {
            return Collections.emptyList();
        }
        List<ParsedChatMessage> parsed = new ArrayList<>(rawMessages.size());
        for (ChatMessage m : rawMessages) {
            parsed.add(ChatMessageParser.parse(m));
        }
        return group(parsed);
    }

    public static List<GroupedMessageItem> group(List<ParsedChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return Collections.emptyList();
        }
        int n = messages.size();
        List<GroupedMessageItem> result = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            ParsedChatMessage cur = messages.get(i);
            String author = cur.getCreatedBy();
            boolean first = (i == 0) || !Objects.equals(author, messages.get(i - 1).getCreatedBy());
            boolean last = (i == n - 1) || !Objects.equals(author, messages.get(i + 1).getCreatedBy());
            result.add(new GroupedMessageItem(cur, first, last));
        }
        return result;
    }
}
