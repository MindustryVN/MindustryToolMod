package mindustrytool.features.chat.models;

/**
 * Layout item representing a chat message with group metadata (whether it begins or ends
 * a consecutive group of messages from the same author).
 */
public class GroupedMessageItem {

    private final ParsedChatMessage message;
    private final boolean firstInGroup;
    private final boolean lastInGroup;

    public GroupedMessageItem(ParsedChatMessage message, boolean firstInGroup, boolean lastInGroup) {
        this.message = message;
        this.firstInGroup = firstInGroup;
        this.lastInGroup = lastInGroup;
    }

    public ParsedChatMessage getMessage() {
        return message;
    }

    public String getId() {
        return message != null ? message.getId() : "";
    }

    public boolean isFirstInGroup() {
        return firstInGroup;
    }

    public boolean isLastInGroup() {
        return lastInGroup;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupedMessageItem that = (GroupedMessageItem) o;
        return firstInGroup == that.firstInGroup
                && lastInGroup == that.lastInGroup
                && java.util.Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(getId(), firstInGroup, lastInGroup);
    }
}
