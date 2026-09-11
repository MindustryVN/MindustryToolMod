package mindustrytool.features.chat;

import arc.Core;
import arc.util.Log;
import arc.util.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import mindustrytool.models.response.ChatMessage;
import mindustrytool.models.response.UserData;
import mindustrytool.services.MindustryTool;
import mindustrytool.utils.JsonUtils;

public class ChatService {

    public static final int PAGE_SIZE = 50;

    private final ChatStore store;
    private final Supplier<Boolean> windowOpenSupplier;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final String chatId = UUID.randomUUID().toString();

    private Flow.Subscription streamSubscription;
    private StringBuilder dataBuffer = new StringBuilder();
    private String currentEvent = "data";

    public ChatService(ChatStore store, Supplier<Boolean> windowOpenSupplier) {
        this.store = store;
        this.windowOpenSupplier = windowOpenSupplier;

        store.activeChannelId().subscribe(channelId -> {
            if (channelId != null && !channelId.isEmpty()) {
                loadMessages(channelId);
                loadUsers(channelId);
            }
        });
    }

    public synchronized void start() {
        if (running.get()) {
            return;
        }
        running.set(true);
        refreshChannels();
        connectStream();
    }

    public synchronized void stop() {
        running.set(false);
        if (streamSubscription != null) {
            try {
                streamSubscription.cancel();
            } catch (Exception ignored) {
            }
            streamSubscription = null;
        }
        Core.app.post(() -> store.setConnected(false));
    }

    public void refreshChannels() {
        MindustryTool.getChatChannels().thenAccept(channels -> {
            Core.app.post(() -> {
                store.setChannels(channels);
                String activeId = store.activeChannelId().peek();
                if (activeId != null && !activeId.isEmpty()) {
                    loadMessages(activeId);
                    loadUsers(activeId);
                }
            });
        }).exceptionally(e -> {
            Log.err("Failed to fetch chat channels", e);
            return null;
        });
    }

    public void loadMessages(String channelId) {
        if (channelId == null || channelId.isEmpty()) {
            return;
        }
        MindustryTool.getChatMessages(channelId, null).thenAccept(messages -> {
            Core.app.post(() -> {
                if (messages != null) {
                    Collections.reverse(messages);
                }
                store.setMessages(channelId, messages);
                store.setFullyLoaded(channelId, messages == null || messages.size() < PAGE_SIZE);
                fetchMissingUsers(messages);
            });
        }).exceptionally(e -> {
            Log.err("Failed to fetch chat messages for " + channelId, e);
            return null;
        });
    }

    public void fetchOlderMessages(String channelId) {
        if (channelId == null || channelId.isEmpty()) {
            return;
        }
        if (Boolean.TRUE.equals(store.loadingOlder().peek())) {
            return;
        }
        if (store.isFullyLoaded(channelId)) {
            return;
        }

        List<ChatMessage> currentMsgs = store.activeMessages().peek();
        if (currentMsgs == null || currentMsgs.isEmpty()) {
            return;
        }

        String oldestId = currentMsgs.get(0).getId();
        store.setLoadingOlder(true);

        MindustryTool.getChatMessages(channelId, oldestId).thenAccept(older -> {
            Core.app.post(() -> {
                store.setLoadingOlder(false);
                if (older == null || older.isEmpty()) {
                    store.setFullyLoaded(channelId, true);
                } else {
                    Collections.reverse(older);
                    int added = store.prependMessages(channelId, older);
                    if (older.size() < PAGE_SIZE || added == 0) {
                        store.setFullyLoaded(channelId, true);
                    }
                    fetchMissingUsers(older);
                }
            });
        }).exceptionally(e -> {
            Core.app.post(() -> store.setLoadingOlder(false));
            Log.err("Failed to fetch older chat messages for " + channelId, e);
            return null;
        });
    }

    public void loadUsers(String channelId) {
        if (channelId == null || channelId.isEmpty()) {
            return;
        }
        MindustryTool.getChatUsers(channelId).thenAccept(users -> {
            Core.app.post(() -> store.setUsers(channelId, users));
        }).exceptionally(e -> {
            Log.err("Failed to fetch chat users for " + channelId, e);
            return null;
        });
    }

    public CompletableFuture<ChatMessage> sendMessage(String content, @Nullable String replyTo) {
        String activeId = store.activeChannelId().peek();
        if (activeId == null || activeId.isEmpty()) {
            CompletableFuture<ChatMessage> failed = new CompletableFuture<>();
            failed.completeExceptionally(new IllegalStateException("No active channel"));
            return failed;
        }

        return MindustryTool.sendChatMessage("messages", activeId, content, replyTo)
                .thenApply(msg -> {
                    Core.app.post(() -> {
                        store.appendMessage(msg, windowOpenSupplier.get());
                        store.setReplyTarget(null);
                    });
                    return msg;
                });
    }

    private void connectStream() {
        if (!running.get()) {
            return;
        }

        MindustryTool.chatStream(chatId).thenAccept(publisher -> {
            if (publisher == null) {
                scheduleReconnect();
                return;
            }

            publisher.subscribe(new Flow.Subscriber<String>() {
                @Override
                public void onSubscribe(Flow.Subscription subscription) {
                    streamSubscription = subscription;
                    subscription.request(Long.MAX_VALUE);
                    Core.app.post(() -> store.setConnected(true));
                }

                @Override
                public void onNext(String line) {
                    handleStreamLine(line);
                }

                @Override
                public void onError(Throwable throwable) {
                    Core.app.post(() -> store.setConnected(false));
                    scheduleReconnect();
                }

                @Override
                public void onComplete() {
                    Core.app.post(() -> store.setConnected(false));
                    scheduleReconnect();
                }
            });
        }).exceptionally(e -> {
            Core.app.post(() -> store.setConnected(false));
            scheduleReconnect();
            return null;
        });
    }

    private void handleStreamLine(String line) {
        if (line == null) {
            return;
        }

        if (line.isEmpty()) {
            dispatchCurrentEvent();
            return;
        }

        if (line.startsWith(":")) {
            return;
        }

        if (line.startsWith("event:")) {
            currentEvent = line.substring("event:".length()).trim();
            return;
        }

        if (line.startsWith("data:")) {
            if (dataBuffer.length() > 0) {
                dataBuffer.append('\n');
            }
            dataBuffer.append(line.substring("data:".length()).trim());
        }
    }

    private void dispatchCurrentEvent() {
        String data = dataBuffer.toString().trim();
        String event = currentEvent;
        dataBuffer.setLength(0);
        currentEvent = "data";

        if (data.isEmpty()) {
            return;
        }

        if ("heartbeat".equalsIgnoreCase(event) || "\"Connected\"".equals(data) || "Connected".equals(data)) {
            Core.app.post(() -> store.setConnected(true));
            return;
        }

        try {
            if (data.startsWith("[")) {
                List<ChatMessage> list = JsonUtils.fromJsonArray(ChatMessage.class, data);
                if (list != null) {
                    Core.app.post(() -> {
                        boolean open = windowOpenSupplier.get();
                        for (ChatMessage msg : list) {
                            store.appendMessage(msg, open);
                        }
                        fetchMissingUsers(list);
                    });
                }
            } else if (data.startsWith("{")) {
                ChatMessage msg = JsonUtils.fromJson(ChatMessage.class, data);
                if (msg != null && msg.getId() != null) {
                    Core.app.post(() -> {
                        store.appendMessage(msg, windowOpenSupplier.get());
                        fetchMissingUsers(Collections.singletonList(msg));
                    });
                }
            }
        } catch (Exception e) {
            Log.err("Error processing chat stream event", e);
        }
    }

    public void fetchMissingUsers(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) return;
        Map<String, UserData> cached = store.userCache().peek();
        List<String> missing = new ArrayList<>();
        for (ChatMessage msg : messages) {
            String authorId = msg.getCreatedBy();
            if (authorId != null && !authorId.isEmpty() && (cached == null || !cached.containsKey(authorId))) {
                if (!missing.contains(authorId)) {
                    missing.add(authorId);
                }
            }
        }
        if (!missing.isEmpty()) {
            MindustryTool.getUserBatch(missing).thenAccept(userDataList -> {
                if (userDataList != null) {
                    Core.app.post(() -> store.putUsers(userDataList));
                }
            }).exceptionally(e -> {
                Log.err("Failed to fetch user batch", e);
                return null;
            });
        }
    }

    private void scheduleReconnect() {
        if (!running.get()) {
            return;
        }
        new Thread(() -> {
            try {
                Thread.sleep(5000L);
            } catch (InterruptedException ignored) {
            }
            if (running.get()) {
                connectStream();
            }
        }, "ChatReconnectThread").start();
    }
}
