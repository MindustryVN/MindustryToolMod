package mindustrytool.features.chat;

import arc.util.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import mindustrytool.models.response.ChannelDto;
import mindustrytool.models.response.ChatMessage;
import mindustrytool.models.response.ChatUser;
import solim.signal.Computed;
import solim.signal.Readable;
import solim.signal.Signal;

public class ChatStore {

    private final Signal<List<ChannelDto>> channels = Signal.of(Collections.emptyList());
    private final Signal<String> activeChannelId = Signal.of("");
    private final Signal<Map<String, List<ChatMessage>>> messages = Signal.of(new HashMap<>());
    private final Signal<Map<String, List<ChatUser>>> users = Signal.of(new HashMap<>());
    private final Signal<Integer> unreadCount = Signal.of(0);
    private final Signal<Boolean> connected = Signal.of(false);
    private final Signal<ChatMessage> replyTarget = Signal.of(null);

    private final Computed<List<ChatMessage>> activeMessages = new Computed<>(() -> {
        String activeId = activeChannelId.get();
        if (activeId == null || activeId.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, List<ChatMessage>> map = messages.get();
        List<ChatMessage> list = map != null ? map.get(activeId) : null;
        return list != null ? list : Collections.emptyList();
    });

    private final Computed<List<ChatUser>> activeUsers = new Computed<>(() -> {
        String activeId = activeChannelId.get();
        if (activeId == null || activeId.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, List<ChatUser>> map = users.get();
        List<ChatUser> list = map != null ? map.get(activeId) : null;
        return list != null ? list : Collections.emptyList();
    });

    private final Computed<ChannelDto> activeChannel = new Computed<>(() -> {
        String activeId = activeChannelId.get();
        if (activeId == null || activeId.isEmpty()) {
            return null;
        }
        List<ChannelDto> list = channels.get();
        if (list == null) {
            return null;
        }
        for (ChannelDto c : list) {
            if (Objects.equals(c.getId(), activeId)) {
                return c;
            }
        }
        return null;
    });

    public Readable<List<ChannelDto>> channels() {
        return channels;
    }

    public Signal<String> activeChannelId() {
        return activeChannelId;
    }

    public Readable<List<ChatMessage>> activeMessages() {
        return activeMessages;
    }

    public Readable<List<ChatUser>> activeUsers() {
        return activeUsers;
    }

    public Readable<ChannelDto> activeChannel() {
        return activeChannel;
    }

    public Readable<Integer> unreadCount() {
        return unreadCount;
    }

    public Readable<Boolean> connected() {
        return connected;
    }

    public Signal<ChatMessage> replyTarget() {
        return replyTarget;
    }

    public void setChannels(List<ChannelDto> newChannels) {
        channels.set(newChannels != null ? new ArrayList<>(newChannels) : Collections.emptyList());
        if (!newChannels.isEmpty()) {
            String current = activeChannelId.peek();
            boolean exists = false;
            for (ChannelDto c : newChannels) {
                if (Objects.equals(c.getId(), current)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                activeChannelId.set(newChannels.get(0).getId());
            }
        }
    }

    public void setActiveChannelId(String channelId) {
        if (!Objects.equals(activeChannelId.peek(), channelId)) {
            activeChannelId.set(channelId);
            replyTarget.set(null);
        }
    }

    public void setMessages(String channelId, List<ChatMessage> newMessages) {
        Map<String, List<ChatMessage>> current = new HashMap<>(messages.peek() != null ? messages.peek() : Collections.emptyMap());
        current.put(channelId, newMessages != null ? new ArrayList<>(newMessages) : Collections.emptyList());
        messages.set(current);
    }

    public void appendMessage(ChatMessage message, boolean isWindowOpen) {
        if (message == null || message.getChannelId() == null) {
            return;
        }
        String chId = message.getChannelId();
        Map<String, List<ChatMessage>> current = new HashMap<>(messages.peek() != null ? messages.peek() : Collections.emptyMap());
        List<ChatMessage> list = current.containsKey(chId) ? new ArrayList<>(current.get(chId)) : new ArrayList<>();

        // Prevent duplicates
        for (ChatMessage existing : list) {
            if (Objects.equals(existing.getId(), message.getId())) {
                return;
            }
        }

        list.add(message);
        current.put(chId, list);
        messages.set(current);

        boolean isActive = Objects.equals(activeChannelId.peek(), chId);
        if (!isWindowOpen || !isActive) {
            Integer unread = unreadCount.peek();
            unreadCount.set((unread != null ? unread : 0) + 1);
        }
    }

    public void setUsers(String channelId, List<ChatUser> userList) {
        Map<String, List<ChatUser>> current = new HashMap<>(users.peek() != null ? users.peek() : Collections.emptyMap());
        current.put(channelId, userList != null ? new ArrayList<>(userList) : Collections.emptyList());
        users.set(current);
    }

    public void setConnected(boolean isConnected) {
        connected.set(isConnected);
    }

    public void clearUnread() {
        unreadCount.set(0);
    }

    public void setReplyTarget(@Nullable ChatMessage target) {
        replyTarget.set(target);
    }
}
