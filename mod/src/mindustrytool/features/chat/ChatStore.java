package mindustrytool.features.chat;

import arc.Events;
import arc.util.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import mindustrytool.events.LoginEvent;
import mindustrytool.events.LogoutEvent;
import mindustrytool.events.SessionLoadEvent;
import mindustrytool.models.response.ChannelDto;
import mindustrytool.models.response.ChatMessage;
import mindustrytool.models.response.ChatUser;
import mindustrytool.models.response.UserData;
import mindustrytool.services.auth.MindustryAuthProvider;
import solim.signal.Computed;
import solim.signal.Readable;
import solim.signal.Signal;

public class ChatStore {

    private final Signal<Boolean> loggedIn = Signal.of(MindustryAuthProvider.getInstance().isLoggedIn());
    private final Signal<List<ChannelDto>> channels = Signal.of(Collections.emptyList());
    private final Signal<String> activeChannelId = Signal.of("");
    private final Signal<Map<String, List<ChatMessage>>> messages = Signal.of(new HashMap<>());
    private final Signal<Map<String, List<ChatUser>>> users = Signal.of(new HashMap<>());
    private final Signal<Map<String, UserData>> userCache = Signal.of(new HashMap<>());
    private final Signal<Integer> unreadCount = Signal.of(0);
    private final Signal<Map<String, Integer>> channelUnread = Signal.of(new HashMap<>());
    private final Signal<Boolean> connected = Signal.of(false);
    private final Signal<ChatMessage> replyTarget = Signal.of(null);
    private final Signal<Boolean> loadingOlder = Signal.of(false);
    private final Signal<Map<String, Boolean>> fullyLoadedChannels = Signal.of(new HashMap<>());
    private final Signal<String> expandedMessageId = Signal.of(null);
    private final Signal<Map<String, String>> translatedMessages = Signal.of(new HashMap<>());
    private final Signal<String> translatingMessageId = Signal.of(null);

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

    private final Computed<Boolean> activeChannelFullyLoaded = new Computed<>(() -> {
        String activeId = activeChannelId.get();
        if (activeId == null || activeId.isEmpty()) {
            return false;
        }
        Map<String, Boolean> map = fullyLoadedChannels.get();
        return map != null && Boolean.TRUE.equals(map.get(activeId));
    });

    public ChatStore() {
        Events.on(SessionLoadEvent.class, e -> loggedIn.set(MindustryAuthProvider.getInstance().isLoggedIn()));
        Events.on(LoginEvent.class, e -> loggedIn.set(true));
        Events.on(LogoutEvent.class, e -> loggedIn.set(false));
    }

    public Readable<Boolean> loggedIn() {
        return loggedIn;
    }

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
            expandedMessageId.set(null);

            // Clear unread for selected channel
            Map<String, Integer> unreads = new HashMap<>(channelUnread.peek() != null ? channelUnread.peek() : Collections.emptyMap());
            unreads.put(channelId, 0);
            channelUnread.set(unreads);
        }
    }

    public Readable<Integer> channelUnread(String channelId) {
        return channelUnread.map(map -> (map != null && channelId != null) ? map.getOrDefault(channelId, 0) : 0);
    }

    public Readable<Boolean> loadingOlder() {
        return loadingOlder;
    }

    public void setLoadingOlder(boolean loading) {
        loadingOlder.set(loading);
    }

    public Readable<Boolean> fullyLoaded(String channelId) {
        return fullyLoadedChannels.map(map -> (map != null && channelId != null) && Boolean.TRUE.equals(map.get(channelId)));
    }

    public Readable<Boolean> activeChannelFullyLoaded() {
        return activeChannelFullyLoaded;
    }

    public boolean isFullyLoaded(String channelId) {
        Map<String, Boolean> map = fullyLoadedChannels.peek();
        return channelId != null && map != null && Boolean.TRUE.equals(map.get(channelId));
    }

    public void setFullyLoaded(String channelId, boolean fullyLoaded) {
        if (channelId != null) {
            Map<String, Boolean> map = new HashMap<>(fullyLoadedChannels.peek() != null ? fullyLoadedChannels.peek() : Collections.emptyMap());
            map.put(channelId, fullyLoaded);
            fullyLoadedChannels.set(map);
        }
    }

    public Signal<String> expandedMessageId() {
        return expandedMessageId;
    }

    public void toggleExpanded(String messageId) {
        if (Objects.equals(expandedMessageId.peek(), messageId)) {
            expandedMessageId.set(null);
        } else {
            expandedMessageId.set(messageId);
        }
    }

    public Readable<String> translation(String messageId) {
        return translatedMessages.map(map -> (map != null && messageId != null) ? map.get(messageId) : null);
    }

    public void setTranslation(String messageId, String translation) {
        if (messageId != null) {
            Map<String, String> map = new HashMap<>(translatedMessages.peek() != null ? translatedMessages.peek() : Collections.emptyMap());
            if (translation != null) {
                map.put(messageId, translation);
            } else {
                map.remove(messageId);
            }
            translatedMessages.set(map);
        }
    }

    public Signal<String> translatingMessageId() {
        return translatingMessageId;
    }

    public void setMessages(String channelId, List<ChatMessage> newMessages) {
        Map<String, List<ChatMessage>> current = new HashMap<>(messages.peek() != null ? messages.peek() : Collections.emptyMap());
        current.put(channelId, newMessages != null ? new ArrayList<>(newMessages) : Collections.emptyList());
        messages.set(current);
    }

    public int prependMessages(String channelId, List<ChatMessage> oldMessages) {
        if (oldMessages == null || oldMessages.isEmpty() || channelId == null) {
            return 0;
        }
        Map<String, List<ChatMessage>> current = new HashMap<>(messages.peek() != null ? messages.peek() : Collections.emptyMap());
        List<ChatMessage> existing = current.containsKey(channelId) ? new ArrayList<>(current.get(channelId)) : new ArrayList<>();
        List<ChatMessage> merged = new ArrayList<>();
        for (ChatMessage m : oldMessages) {
            boolean found = false;
            for (ChatMessage ex : existing) {
                if (Objects.equals(ex.getId(), m.getId())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                merged.add(m);
            }
        }
        if (merged.isEmpty()) {
            return 0;
        }
        int added = merged.size();
        merged.addAll(existing);
        current.put(channelId, merged);
        messages.set(current);
        return added;
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

            Map<String, Integer> unreads = new HashMap<>(channelUnread.peek() != null ? channelUnread.peek() : Collections.emptyMap());
            int count = unreads.getOrDefault(chId, 0);
            unreads.put(chId, count + 1);
            channelUnread.set(unreads);
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
        String active = activeChannelId.peek();
        if (active != null && !active.isEmpty()) {
            Map<String, Integer> unreads = new HashMap<>(channelUnread.peek() != null ? channelUnread.peek() : Collections.emptyMap());
            unreads.put(active, 0);
            channelUnread.set(unreads);
        }
    }

    public void setReplyTarget(@Nullable ChatMessage target) {
        replyTarget.set(target);
    }

    public Readable<Map<String, UserData>> userCache() {
        return userCache;
    }

    public Readable<UserData> user(String userId) {
        return userCache.map(map -> map != null ? map.get(userId) : null);
    }

    public void putUsers(List<UserData> users) {
        if (users == null || users.isEmpty()) return;
        Map<String, UserData> map = new HashMap<>(userCache.peek() != null ? userCache.peek() : Collections.emptyMap());
        for (UserData u : users) {
            if (u.getId() != null) {
                map.put(u.getId(), u);
            }
        }
        userCache.set(map);
    }
}
