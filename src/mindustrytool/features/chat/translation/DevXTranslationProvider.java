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

        java.util.Locale locale = Core.bundle.getLocale();
        String langCode = (locale != null && locale.getLanguage() != null && !locale.getLanguage().isEmpty())
                ? locale.getLanguage().toLowerCase()
                : "vi";

        if (locale != null && locale.getCountry() != null && !locale.getCountry().isEmpty() && langCode.equals("zh")) {
            langCode = "zh-" + locale.getCountry();
        }

        String pair = langCode.equals("en") ? "auto-en" : ("en-" + langCode);
        return translateWithPair(message, pair);
    }

    @Override
    public synchronized CompletableFuture<String> translate(String message, String sourceLang, String targetLang) {
        String s = (sourceLang != null && !sourceLang.isEmpty()) ? sourceLang.toLowerCase() : "vi";
        String t = (targetLang != null && !targetLang.isEmpty()) ? targetLang.toLowerCase() : "en";
        if (s.equals(t)) {
            return CompletableFuture.completedFuture(message);
        }
        String pair = s + "-" + t;
        return translateWithPair(message, pair);
    }

    private CompletableFuture<String> translateWithPair(String message, String pair) {
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
            body.put("temperature", 0.1);
            body.put("top_p", 0.7);
            body.put("max_tokens", 1024);

            Jval messages = Jval.newArray();

            Jval systemMessage = Jval.newObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", pair);
            messages.add(systemMessage);

            Jval userMessage = Jval.newObject();
            userMessage.put("role", "user");
            userMessage.put("content", message);
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
                                result = result.replaceAll("(?i)^(translation|translated|bản dịch|vietnamese|tiếng việt|english|tiếng anh)\\s*:\\s*", "").trim();
                                if (result.startsWith("\"") && result.endsWith("\"") && result.length() > 1) {
                                    result = result.substring(1, result.length() - 1).trim();
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
