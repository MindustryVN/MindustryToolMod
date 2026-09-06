package mindustrytool.services;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import mindustrytool.services.auth.AuthProvider;

public final class Request {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);

    private final String baseUrl;
    private final Duration timeout;
    private final AuthProvider authProvider;

    private Request(String baseUrl, Duration timeout, AuthProvider authProvider) {
        this.baseUrl = baseUrl;
        this.timeout = timeout != null ? timeout : DEFAULT_TIMEOUT;
        this.authProvider = authProvider;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String baseUrl;
        private Duration timeout;
        private AuthProvider authProvider;

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder authProvider(AuthProvider authProvider) {
            this.authProvider = authProvider;
            return this;
        }

        public Request build() {
            return new Request(baseUrl, timeout, authProvider);
        }
    }

    // ─── HTTP verbs ────────────────────────────────────────────────

    public RequestBuilder get(String url) {
        return new RequestBuilder(this, "GET", url);
    }

    public RequestBuilder post(String url) {
        return new RequestBuilder(this, "POST", url);
    }

    public RequestBuilder put(String url) {
        return new RequestBuilder(this, "PUT", url);
    }

    public RequestBuilder delete(String url) {
        return new RequestBuilder(this, "DELETE", url);
    }

    // ─── Builder ───────────────────────────────────────────────────

    public static final class RequestBuilder {
        private final Request outer;
        private final String method;
        private final String url;
        private Duration timeoutOverride;
        private final Map<String, String> headers = new LinkedHashMap<>();
        private HttpRequest.BodyPublisher bodyPublisher;
        private String bodyStringForContentType;
        private boolean useAuth = true;

        private RequestBuilder(Request outer, String method, String url) {
            this.outer = outer;
            this.method = method;
            this.url = url;
        }

        public RequestBuilder header(String name, String value) {
            headers.put(name, value);
            return this;
        }

        public RequestBuilder timeout(Duration timeout) {
            this.timeoutOverride = timeout;
            return this;
        }

        public RequestBuilder body(String body) {
            this.bodyPublisher = BodyPublishers.ofString(body != null ? body : "");
            return this;
        }

        public RequestBuilder json(String json) {
            this.bodyPublisher = BodyPublishers.ofString(json != null ? json : "");
            headers.put("Content-Type", "application/json");
            return this;
        }

        public RequestBuilder bytes(byte[] bytes) {
            this.bodyPublisher = BodyPublishers.ofByteArray(bytes != null ? bytes : new byte[0]);
            return this;
        }

        public RequestBuilder withoutAuth() {
            this.useAuth = false;
            return this;
        }

        public CompletableFuture<HttpResponse<String>> sendAsync() {
            return sendAsync(BodyHandlers.ofString());
        }

        public <T> CompletableFuture<HttpResponse<T>> sendAsync(BodyHandler<T> handler) {
            String resolvedUrl = resolveUrl(outer.baseUrl, url);
            Duration effectiveTimeout = timeoutOverride != null ? timeoutOverride : outer.timeout;

            if (useAuth && outer.authProvider != null) {
                return outer.authProvider.refreshIfNeeded().thenCompose(v -> {
                    String token = outer.authProvider.getAccessToken();
                    HttpRequest request = buildHttpRequest(resolvedUrl, effectiveTimeout, token);
                    return CLIENT.sendAsync(request, handler);
                });
            } else {
                HttpRequest request = buildHttpRequest(resolvedUrl, effectiveTimeout, null);
                return CLIENT.sendAsync(request, handler);
            }
        }

        private HttpRequest buildHttpRequest(String resolvedUrl, Duration effectiveTimeout, String token) {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(resolvedUrl))
                    .timeout(effectiveTimeout);

            headers.forEach(builder::header);

            if (token != null) {
                builder.header("Authorization", "Bearer " + token);
            }

            switch (method) {
                case "GET" -> builder.GET();
                case "DELETE" -> builder.DELETE();
                case "POST" -> {
                    if (bodyPublisher != null) {
                        builder.POST(bodyPublisher);
                    } else {
                        builder.POST(BodyPublishers.noBody());
                    }
                }
                case "PUT" -> {
                    if (bodyPublisher != null) {
                        builder.PUT(bodyPublisher);
                    } else {
                        builder.PUT(BodyPublishers.noBody());
                    }
                }
                default -> throw new IllegalArgumentException("Unsupported method: " + method);
            }

            return builder.build();
        }

        private static String resolveUrl(String baseUrl, String url) {
            if (url == null) return baseUrl != null ? baseUrl : "";
            if (url.isEmpty()) return baseUrl != null ? baseUrl : "";
            if (url.startsWith("http://") || url.startsWith("https://")) {
                return url;
            }
            if (baseUrl == null || baseUrl.isEmpty()) {
                return url;
            }
            // Query or fragment should append directly without extra slash
            if (url.startsWith("?") || url.startsWith("#")) {
                return baseUrl + url;
            }
            boolean baseEnds = baseUrl.endsWith("/");
            boolean urlStarts = url.startsWith("/");
            if (baseEnds && urlStarts) {
                return baseUrl + url.substring(1);
            }
            if (!baseEnds && !urlStarts) {
                return baseUrl + "/" + url;
            }
            return baseUrl + url;
        }
    }

    // ─── Multipart helper (kept for upload) ────────────────────────

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

    // Expose shared client if needed (kept for compatibility)
    public static HttpClient client() {
        return CLIENT;
    }
}
