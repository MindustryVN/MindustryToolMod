package mindustrytool.features.chat.translation;

import arc.Core;
import arc.util.Http;
import arc.util.Log;
import arc.util.Http.HttpStatus;
import arc.util.Http.HttpStatusException;
import arc.util.serialization.Jval;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.scene.ui.Slider;
import java.util.concurrent.CompletableFuture;

public class DevXTranslationProvider implements TranslationProvider {
    private static final String API_URL = "https://integrate.api.nvidia.com/v1/chat/completions";
    private static final String MODEL = "nvidia/riva-translate-4b-instruct-v2";

    private Seq<String> lastMessages = new Seq<>();

    private int maxHistory() {
        return Core.settings.getInt(ChatTranslationConfig.DEVX_MAX_HISTORY, 0);
    }

    private void setMaxHistory(int maxHistory) {
        Core.settings.put(ChatTranslationConfig.DEVX_MAX_HISTORY, maxHistory);
    }

    private int getTimeout() {
        return Core.settings.getInt(ChatTranslationConfig.DEVX_TIMEOUT, 10);
    }

    private void setTimeout(int timeout) {
        Core.settings.put(ChatTranslationConfig.DEVX_TIMEOUT, timeout);
    }

    @Override
    public synchronized CompletableFuture<String> translate(String message) {
        lastMessages.add(message);
        if (lastMessages.size > maxHistory()) {
            lastMessages.remove(0);
        }

        CompletableFuture<String> future = new CompletableFuture<>();

        String apiKey = mindustrytool.BuildConfig.DEVX_API_KEY.trim();
        if (apiKey.isEmpty()) {
            future.completeExceptionally(
                    new RuntimeException(Core.bundle.get("chat-translation.devx.service-error", "DevX service unavailable")));
            return future;
        }

        try {
            Jval body = Jval.newObject();
            body.put("model", MODEL);
            body.put("temperature", 0.2);
            body.put("top_p", 0.7);
            body.put("max_tokens", 1024);

            StringBuilder history = new StringBuilder();
            Seq<String> historySnapshot = new Seq<>(lastMessages);

            for (int i = 0; i < Math.min(maxHistory(), historySnapshot.size); i++) {
                history.append(historySnapshot.get(historySnapshot.size - i - 1)).append("\n");
            }

            if (history.length() > 0) {
                history.insert(0, "Previous conversation history:\n");
            }

            String prompt = "Translate the following Mindustry game chat message to "
                    + Core.bundle.getLocale().getDisplayName()
                    + ". Only return the translated text directly without any explanation, markdown formatting, or thoughts. If it is already in "
                    + Core.bundle.getLocale().getDisplayName()
                    + ", just return it as is."
                    + (history.length() > 0 ? "\n" + history.toString() : "")
                    + "\nMessage to translate: "
                    + message;

            Jval messages = Jval.newArray();
            Jval userMessage = Jval.newObject();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messages.add(userMessage);

            body.put("messages", messages);

            Http.post(API_URL, body.toString())
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(getTimeout() * 1000)
                    .error(e -> {
                        if (e instanceof HttpStatusException httpStatusException) {
                            if (httpStatusException.status.code == 429
                                    || httpStatusException.status == HttpStatus.UNKNOWN_STATUS) {
                                future.completeExceptionally(new RuntimeException(
                                        Core.bundle.get("chat-translation.devx.rate-limit", "Rate limit exceeded")));
                            } else if (httpStatusException.status.code == 404) {
                                future.completeExceptionally(new RuntimeException(
                                        Core.bundle.get("chat-translation.devx.service-error", "Service unavailable")));
                            } else if (httpStatusException.status.code == 401) {
                                future.completeExceptionally(new RuntimeException(
                                        Core.bundle.get("chat-translation.devx.service-error", "Service unavailable")));
                            } else if (httpStatusException.status.code >= 500) {
                                future.completeExceptionally(new RuntimeException(
                                        Core.bundle.get("chat-translation.devx.server-error", "Server error")));
                            } else {
                                future.completeExceptionally(new RuntimeException(
                                        Core.bundle.get("chat-translation.error.prefix")
                                                + httpStatusException.response.getResultAsString()));
                            }
                            Log.err(e);
                            return;
                        }

                        future.completeExceptionally(
                                new RuntimeException(
                                        Core.bundle.get("chat-translation.error.prefix") + e.getMessage()));
                    })
                    .submit(res -> {
                        String jsonString = res.getResultAsString();
                        try {
                            Jval json = Jval.read(jsonString);
                            if (json.has("choices") && !json.get("choices").asArray().isEmpty()) {
                                Jval choice = json.get("choices").asArray().get(0);
                                String result = message;
                                if (choice.has("message")) {
                                    result = choice.get("message").getString("content", message).trim();
                                }

                                if (result.contains("<think>")) {
                                    result = result.replaceAll("(?s)<think>.*?</think>", "").trim();
                                }

                                future.complete(result);
                            } else {
                                future.complete(message);
                            }
                        } catch (Exception e) {
                            future.completeExceptionally(
                                    new RuntimeException(Core.bundle.get("chat-translation.devx.parse-error", "Parse Error"), e));
                        }
                    });
        } catch (Exception e) {
            future.completeExceptionally(new RuntimeException("DevX translation error", e));
        }

        return future;
    }

    @Override
    public Table settings() {
        Table table = new Table();

        table.add("[green]✓ " + Core.bundle.get("chat-translation.devx.ready", "Ready to use") + "[]").left().row();

        table.add(Core.bundle.get("chat-translation.devx.timeout-label", "Timeout") + ": " + getTimeout() + "s").left()
                .padTop(10)
                .update(l -> l
                        .setText(Core.bundle.get("chat-translation.devx.timeout-label", "Timeout") + ": " + getTimeout() + "s"))
                .row();

        Slider slider = new Slider(5, 30, 1, false);
        slider.setValue(getTimeout());
        slider.moved(val -> {
            setTimeout((int) val);
        });

        table.add(slider).growX().row();

        table.add(Core.bundle.get("chat-translation.devx.max-histories", "Max Histories") + ": " + maxHistory()).left()
                .padTop(10)
                .update(l -> l
                        .setText(Core.bundle.get("chat-translation.devx.max-histories", "Max Histories") + ": " + maxHistory()))
                .row();

        Slider historySlider = new Slider(0, 10, 1, false);
        historySlider.setValue(maxHistory());
        historySlider.moved(val -> {
            setMaxHistory((int) val);
        });

        table.add(historySlider).growX().row();

        return table;
    }

    @Override
    public String getName() {
        return Core.bundle.get("chat-translation.provider.devx", "DevX");
    }

    @Override
    public String getId() {
        return "devx";
    }
}
