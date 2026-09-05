package mindustrytool.features.chat.translation;

import arc.Core;
import arc.scene.ui.Slider;
import arc.scene.ui.layout.Table;
import arc.util.Http;
import arc.util.Http.HttpStatusException;
import arc.util.Log;
import arc.util.serialization.Jval;

import java.util.concurrent.CompletableFuture;

public class DevXTranslationProvider implements TranslationProvider {
    private static final String API_URL = "https://integrate.api.nvidia.com/v1/chat/completions";
    private static final String MODEL = "nvidia/riva-translate-4b-instruct-v2";
    public static final String DEFAULT_API_KEY = "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";
    private static final String DEFAULT_TARGET_LANG = "English";

    private String getApiKey() {
        String key = Core.settings.getString(ChatTranslationConfig.DEVX_API_KEY, DEFAULT_API_KEY);
        return (key == null || key.trim().isEmpty()) ? DEFAULT_API_KEY : key.trim();
    }

    private int getTimeoutSeconds() {
        return Core.settings.getInt(ChatTranslationConfig.DEVX_TIMEOUT, 15);
    }

    private void setTimeoutSeconds(int timeout) {
        Core.settings.put(ChatTranslationConfig.DEVX_TIMEOUT, timeout);
    }

    private String resolveTargetLanguage(String targetLang) {
        if (targetLang != null && !targetLang.trim().isEmpty()) {
            return targetLang.trim();
        }
        return Core.bundle.getLocale() != null ? Core.bundle.getLocale().getDisplayName() : DEFAULT_TARGET_LANG;
    }

    @Override
    public CompletableFuture<String> translate(String message) {
        return translate(message, resolveTargetLanguage(null));
    }

    @Override
    public CompletableFuture<String> translate(String message, String targetLang) {
        CompletableFuture<String> future = new CompletableFuture<>();

        if (message == null || message.trim().isEmpty()) {
            future.complete("");
            return future;
        }

        String apiKey = getApiKey();
        if (apiKey.isEmpty()) {
            future.completeExceptionally(new IllegalArgumentException(Core.bundle.get("chat-translation.no-api-key", "No API Key")));
            return future;
        }

        String lang = resolveTargetLanguage(targetLang);
        executeTranslationRequest(message, lang, apiKey, future);
        return future;
    }

    private void executeTranslationRequest(String message, String targetLang, String apiKey, CompletableFuture<String> future) {
        try {
            String jsonPayload = buildRequestBody(message, targetLang);
            Http.post(API_URL, jsonPayload)
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(getTimeoutSeconds() * 1000)
                    .error(error -> handleHttpError(error, future))
                    .submit(res -> handleHttpResponse(res.getResultAsString(), message, future));
        } catch (Exception e) {
            future.completeExceptionally(new RuntimeException("DevX request build error", e));
        }
    }

    private String buildRequestBody(String message, String targetLang) {
        String prompt = "Translate the following Mindustry game chat message to " + targetLang
                + ". Return ONLY the translated text without quotes, explanation, or notes:\n" + message;

        Jval msgObj = Jval.newObject();
        msgObj.put("role", "user");
        msgObj.put("content", prompt);

        Jval messages = Jval.newArray();
        messages.add(msgObj);

        Jval body = Jval.newObject();
        body.put("model", MODEL);
        body.put("messages", messages);
        body.put("temperature", 0.2);
        body.put("max_tokens", 1024);

        return body.toString();
    }

    private void handleHttpError(Throwable error, CompletableFuture<String> future) {
        Log.err(error);
        if (error instanceof HttpStatusException httpEx) {
            future.completeExceptionally(new RuntimeException(mapHttpStatusMessage(httpEx)));
            return;
        }
        String prefix = Core.bundle.get("chat-translation.error.prefix", "Error: ");
        future.completeExceptionally(new RuntimeException(prefix + error.getMessage()));
    }

    private String mapHttpStatusMessage(HttpStatusException httpEx) {
        int code = httpEx.status.code;
        if (code == 429) {
            return Core.bundle.get("chat-translation.gemini.rate-limit", "Rate limit exceeded");
        }
        if (code == 401) {
            return Core.bundle.get("chat-translation.gemini.invalid-token", "Invalid API Token");
        }
        if (code >= 500) {
            return Core.bundle.get("chat-translation.gemini.server-error", "Server error");
        }
        String errorMsg = httpEx.response != null ? httpEx.response.getResultAsString() : httpEx.getMessage();
        return Core.bundle.get("chat-translation.error.prefix", "Error: ") + errorMsg;
    }

    private void handleHttpResponse(String responseBody, String fallback, CompletableFuture<String> future) {
        try {
            String translated = parseTranslationResult(responseBody, fallback);
            future.complete(translated);
        } catch (Exception e) {
            future.completeExceptionally(new RuntimeException(Core.bundle.get("chat-translation.parse-error", "Parse Error"), e));
        }
    }

    private String parseTranslationResult(String jsonString, String fallback) {
        Jval json = Jval.read(jsonString);
        if (!json.has("choices") || json.get("choices").asArray().isEmpty()) {
            return fallback;
        }

        Jval firstChoice = json.get("choices").asArray().get(0);
        String result = "";
        if (firstChoice.has("message") && firstChoice.get("message").has("content")) {
            result = firstChoice.get("message").getString("content", fallback).trim();
        } else if (firstChoice.has("text")) {
            result = firstChoice.getString("text", fallback).trim();
        }

        return result.isEmpty() ? fallback : result;
    }

    @Override
    public Table settings() {
        Table table = new Table();
        String label = Core.bundle.get("chat-translation.timeout-label", "Timeout");

        table.add(label + ": " + getTimeoutSeconds() + "s").left()
                .padTop(4)
                .update(l -> l.setText(label + ": " + getTimeoutSeconds() + "s"))
                .row();

        Slider slider = new Slider(2, 30, 1, false);
        slider.setValue(getTimeoutSeconds());
        slider.moved(val -> setTimeoutSeconds((int) val));
        table.add(slider).growX().row();

        return table;
    }

    @Override
    public String getName() {
        return "DevX";
    }

    @Override
    public String getId() {
        return "devx";
    }
}
