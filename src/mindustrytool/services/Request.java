package mindustrytool.services;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

public final class Request {

    public static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static final int TIMEOUT_MS = 10_000;
    public static final int LONG_TIMEOUT_MS = 60_000;

    private static volatile AuthTokenProvider tokenProvider = AuthTokenProvider.noop();

    private Request() {
    }

    public static void setAuthTokenProvider(AuthTokenProvider provider) {
        tokenProvider = provider != null ? provider : AuthTokenProvider.noop();
    }

    public static AuthTokenProvider getAuthTokenProvider() {
        return tokenProvider;
    }

    // ─── Unauthenticated ───────────────────────────────────────────

    public static CompletableFuture<HttpResponse<String>> get(String url) {
        return get(url, TIMEOUT_MS);
    }

    public static CompletableFuture<HttpResponse<String>> get(String url, int timeoutMs) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(timeoutMs))
                .GET()
                .build();
        return CLIENT.sendAsync(request, BodyHandlers.ofString());
    }

    public static CompletableFuture<HttpResponse<byte[]>> getBytes(String url) {
        return getBytes(url, TIMEOUT_MS);
    }

    public static CompletableFuture<HttpResponse<byte[]>> getBytes(String url, int timeoutMs) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(timeoutMs))
                .GET()
                .build();
        return CLIENT.sendAsync(request, BodyHandlers.ofByteArray());
    }

    public static CompletableFuture<HttpResponse<String>> post(String url, String jsonBody) {
        return post(url, jsonBody, TIMEOUT_MS);
    }

    public static CompletableFuture<HttpResponse<String>> post(String url, String jsonBody, int timeoutMs) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(timeoutMs))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(jsonBody))
                .build();
        return CLIENT.sendAsync(request, BodyHandlers.ofString());
    }

    public static CompletableFuture<HttpResponse<String>> put(String url, String jsonBody) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(TIMEOUT_MS))
                .header("Content-Type", "application/json")
                .PUT(BodyPublishers.ofString(jsonBody))
                .build();
        return CLIENT.sendAsync(request, BodyHandlers.ofString());
    }

    public static CompletableFuture<HttpResponse<String>> delete(String url) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(TIMEOUT_MS))
                .DELETE()
                .build();
        return CLIENT.sendAsync(request, BodyHandlers.ofString());
    }

    public static CompletableFuture<HttpResponse<String>> postWithAuth(String url, String jsonBody, String token) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(TIMEOUT_MS))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(jsonBody));
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        return CLIENT.sendAsync(builder.build(), BodyHandlers.ofString());
    }

    // ─── Authenticated ─────────────────────────────────────────────

    public static CompletableFuture<HttpResponse<String>> authGet(String url) {
        return authGet(url, TIMEOUT_MS);
    }

    public static CompletableFuture<HttpResponse<String>> authGet(String url, int timeoutMs) {
        return tokenProvider.refreshIfNeeded().thenCompose(v -> {
            String token = tokenProvider.getToken();
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMillis(timeoutMs))
                    .GET();
            if (token != null) {
                builder.header("Authorization", "Bearer " + token);
            }
            return CLIENT.sendAsync(builder.build(), BodyHandlers.ofString());
        });
    }

    public static CompletableFuture<HttpResponse<String>> authPost(String url, String jsonBody) {
        return authPost(url, jsonBody, TIMEOUT_MS);
    }

    public static CompletableFuture<HttpResponse<String>> authPost(String url, String jsonBody, int timeoutMs) {
        return tokenProvider.refreshIfNeeded().thenCompose(v -> {
            String token = tokenProvider.getToken();
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMillis(timeoutMs))
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString(jsonBody));
            if (token != null) {
                builder.header("Authorization", "Bearer " + token);
            }
            return CLIENT.sendAsync(builder.build(), BodyHandlers.ofString());
        });
    }

    public static CompletableFuture<HttpResponse<String>> authPut(String url, String jsonBody) {
        return tokenProvider.refreshIfNeeded().thenCompose(v -> {
            String token = tokenProvider.getToken();
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMillis(TIMEOUT_MS))
                    .header("Content-Type", "application/json")
                    .PUT(BodyPublishers.ofString(jsonBody));
            if (token != null) {
                builder.header("Authorization", "Bearer " + token);
            }
            return CLIENT.sendAsync(builder.build(), BodyHandlers.ofString());
        });
    }

    public static CompletableFuture<HttpResponse<String>> authDelete(String url) {
        return tokenProvider.refreshIfNeeded().thenCompose(v -> {
            String token = tokenProvider.getToken();
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMillis(TIMEOUT_MS))
                    .DELETE();
            if (token != null) {
                builder.header("Authorization", "Bearer " + token);
            }
            return CLIENT.sendAsync(builder.build(), BodyHandlers.ofString());
        });
    }

    // ─── Authenticated upload ──────────────────────────────────────

    public static CompletableFuture<Void> authUpload(String url, byte[] fileBytes, String fileName, String hash) {
        return tokenProvider.refreshIfNeeded().thenCompose(v -> {
            String token = tokenProvider.getToken();
            String boundary = "----MindustryTool" + System.currentTimeMillis();
            byte[] body = buildMultipartBody(boundary, fileBytes, fileName, hash);

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMillis(LONG_TIMEOUT_MS))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(BodyPublishers.ofByteArray(body));
            if (token != null) {
                builder.header("Authorization", "Bearer " + token);
            }
            return CLIENT.sendAsync(builder.build(), BodyHandlers.ofString())
                    .thenAccept(response -> {
                        int code = response.statusCode();
                        if (code != 200 && code != 201) {
                            throw new RuntimeException("Upload failed: HTTP " + code + " " + response.body());
                        }
                    });
        });
    }

    // ─── Streaming ─────────────────────────────────────────────────

    public static CompletableFuture<Flow.Publisher<String>> authStream(String url, String chatId) {
        return tokenProvider.refreshIfNeeded().thenApply(v -> {
            String token = tokenProvider.getToken();
            SubmissionPublisher<String> publisher = new SubmissionPublisher<>();

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMillis(0))
                    .header("Accept", "text/event-stream")
                    .header("x-chat-id", chatId)
                    .GET();
            if (token != null) {
                builder.header("Authorization", "Bearer " + token);
            }

            CLIENT.sendAsync(builder.build(), BodyHandlers.ofLines())
                    .thenAccept(response -> {
                        response.body().forEach(publisher::submit);
                        publisher.close();
                    })
                    .exceptionally(e -> {
                        publisher.close();
                        return null;
                    });

            return publisher;
        });
    }

    // ─── Multipart builder ─────────────────────────────────────────

    public static byte[] buildMultipartBody(String boundary, byte[] fileBytes, String fileName, String hash) {
        String CRLF = "\r\n";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            out.write(("--" + boundary + CRLF).getBytes(StandardCharsets.UTF_8));
            out.write(("Content-Disposition: form-data; name=\"hash\"" + CRLF).getBytes(StandardCharsets.UTF_8));
            out.write(CRLF.getBytes(StandardCharsets.UTF_8));
            out.write((hash + CRLF).getBytes(StandardCharsets.UTF_8));

            out.write(("--" + boundary + CRLF).getBytes(StandardCharsets.UTF_8));
            out.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"" + CRLF)
                    .getBytes(StandardCharsets.UTF_8));
            out.write(("Content-Type: application/octet-stream" + CRLF).getBytes(StandardCharsets.UTF_8));
            out.write(CRLF.getBytes(StandardCharsets.UTF_8));
            out.write(fileBytes);
            out.write(CRLF.getBytes(StandardCharsets.UTF_8));

            out.write(("--" + boundary + "--" + CRLF).getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("Failed to build multipart body", e);
        }
        return out.toByteArray();
    }
}
