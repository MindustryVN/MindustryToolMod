package mindustrytool.features.chat.translation;

import arc.Core;
import arc.scene.ui.Slider;
import arc.scene.ui.layout.Table;
import arc.util.Http;
import arc.util.Http.HttpStatusException;
import arc.util.Log;
import arc.util.serialization.Jval;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class DevXTranslationProvider implements TranslationProvider {
    private static final String API_URL = "https://integrate.api.nvidia.com/v1/chat/completions";
    private static final String MODEL = "nvidia/riva-translate-4b-instruct-v2";
    public static final String DEFAULT_API_KEY = "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";
    private static final String DEFAULT_TARGET_LANG = "Vietnamese";

    private String getApiKey() {
        String key = Core.settings.getString(ChatTranslationConfig.DEVX_API_KEY, DEFAULT_API_KEY);
        return (key == null || key.trim().isEmpty()) ? DEFAULT_API_KEY : key.trim();
    }

    private int getTimeoutSeconds() {
        int timeout = Core.settings.getInt(ChatTranslationConfig.DEVX_TIMEOUT, 35);
        return Math.max(timeout, 25);
    }

    private void setTimeoutSeconds(int timeout) {
        Core.settings.put(ChatTranslationConfig.DEVX_TIMEOUT, timeout);
    }

    private String normalizeLanguage(String lang) {
        if (lang == null || lang.trim().isEmpty()) {
            return resolveDefaultTargetLanguage();
        }
        String clean = lang.trim().toLowerCase();
        return switch (clean) {
            case "vi", "vn", "vietnamese", "tiếng việt" -> "Vietnamese";
            case "en", "us", "uk", "english", "tiếng anh" -> "English";
            case "ru", "russian", "tiếng nga" -> "Russian";
            case "zh", "cn", "chinese", "tiếng trung" -> "Chinese";
            case "ja", "japanese", "tiếng nhật" -> "Japanese";
            case "ko", "korean", "tiếng hàn" -> "Korean";
            case "fr", "french", "tiếng pháp" -> "French";
            case "de", "german", "tiếng đức" -> "German";
            case "es", "spanish", "tiếng tây ban nha" -> "Spanish";
            default -> lang.trim();
        };
    }

    private String resolveDefaultTargetLanguage() {
        Locale locale = Core.bundle.getLocale();
        if (locale != null && locale.getLanguage() != null) {
            String l = locale.getLanguage().toLowerCase();
            if (l.equals("vi")) return "Vietnamese";
            if (l.equals("en")) return "English";
            if (l.equals("ru")) return "Russian";
            if (l.equals("zh")) return "Chinese";
            if (l.equals("ja")) return "Japanese";
            if (l.equals("ko")) return "Korean";
        }
        return DEFAULT_TARGET_LANG;
    }

    @Override
    public CompletableFuture<String> translate(String message) {
        return translate(message, resolveDefaultTargetLanguage());
    }

    @Override
    public CompletableFuture<String> translate(String message, String targetLang) {
        return translate(message, "auto", targetLang);
    }

    @Override
    public CompletableFuture<String> translate(String message, String sourceLang, String targetLang) {
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

        String resolvedTarget = normalizeLanguage(targetLang);
        executeTranslationRequest(message, resolvedTarget, apiKey, future);
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
        String prompt = "Translate to " + targetLang + ": " + message;

        Jval msgObj = Jval.newObject();
        msgObj.put("role", "user");
        msgObj.put("content", prompt);

        Jval messages = Jval.newArray();
        messages.add(msgObj);

        Jval body = Jval.newObject();
        body.put("model", MODEL);
        body.put("messages", messages);
        body.put("temperature", 0.1);
        body.put("max_tokens", 256);

        return body.toString();
    }

    private void handleHttpError(Throwable error, CompletableFuture<String> future) {
        Log.err(error);
        if (error instanceof HttpStatusException httpEx) {
            future.completeExceptionally(new RuntimeException(mapHttpStatusMessage(httpEx)));
            return;
        }
        String errorMsg = error.getMessage() != null ? error.getMessage() : error.toString();
        if (errorMsg.contains("timed out") || errorMsg.contains("Timeout")) {
            future.completeExceptionally(new RuntimeException(Core.bundle.get("chat-translation.error.timeout", "Request timed out. Please try again or increase timeout in settings.")));
            return;
        }
        String prefix = Core.bundle.get("chat-translation.error.prefix", "Error: ");
        future.completeExceptionally(new RuntimeException(prefix + errorMsg));
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

        if (result.contains("<think>")) {
            result = result.replaceAll("(?s)<think>.*?</think>", "").trim();
        }
        result = result.replaceAll("(?i)^(translation|translated|bản dịch|vietnamese|tiếng việt|english|tiếng anh)\\s*:\\s*", "").trim();
        if (result.startsWith("\"") && result.endsWith("\"") && result.length() > 1) {
            result = result.substring(1, result.length() - 1).trim();
        }

        return result.isEmpty() ? fallback : result;
    }

    @Override
    public Table settings() {
        Table table = new Table();
        table.add("[green]✓ " + Core.bundle.get("chat-translation.devx.ready", "Ready to use (Built-in API)") + "[]").left().row();

        String label = Core.bundle.get("chat-translation.timeout-label", "Timeout");
        table.add(label + ": " + getTimeoutSeconds() + "s").left()
                .padTop(4)
                .update(l -> l.setText(label + ": " + getTimeoutSeconds() + "s"))
                .row();

        Slider slider = new Slider(5, 60, 5, false);
        slider.setValue(getTimeoutSeconds());
        slider.moved(val -> setTimeoutSeconds((int) val));
        table.add(slider).growX().row();

        return table;
    }

    @Override
    public String getName() {
        return "DevX (Nvidia Riva)";
    }

    @Override
    public String getId() {
        return "devx";
    }
}
