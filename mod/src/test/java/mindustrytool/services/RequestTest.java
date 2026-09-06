package mindustrytool.services;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import mindustrytool.services.auth.AuthProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestTest {

    private static HttpServer server;
    private static int serverPort;

    @BeforeAll
    static void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/test-get", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                byte[] response = "hello world".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                exchange.getResponseHeaders().set("X-Custom-Header", "test-val");
                exchange.sendResponseHeaders(200, response.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response);
                }
            }
        });
        server.createContext("/test-post", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                byte[] reqBytes = Request.readAllBytes(exchange.getRequestBody());
                String body = new String(reqBytes, StandardCharsets.UTF_8);
                String auth = exchange.getRequestHeaders().getFirst("Authorization");
                String echo = "echo:" + body + "|auth:" + auth;
                byte[] response = echo.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                exchange.sendResponseHeaders(200, response.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response);
                }
            }
        });
        server.createContext("/test-lines", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                byte[] response = "line1\nline2\nline3".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, response.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response);
                }
            }
        });
        server.createContext("/test-error", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                byte[] response = "{\"error\":\"not found\"}".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(404, response.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response);
                }
            }
        });
        server.setExecutor(null);
        server.start();
        serverPort = server.getAddress().getPort();
    }

    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void testUrlResolution() {
        assertEquals("https://api.com/v1/users", Request.RequestBuilder.resolveUrl("https://api.com/v1", "users"));
        assertEquals("https://api.com/v1/users", Request.RequestBuilder.resolveUrl("https://api.com/v1/", "/users"));
        assertEquals("https://api.com/v1/users", Request.RequestBuilder.resolveUrl("https://api.com/v1/", "users"));
        assertEquals("https://api.com/v1/users", Request.RequestBuilder.resolveUrl("https://api.com/v1", "/users"));
        assertEquals("https://api.com/v1?page=1", Request.RequestBuilder.resolveUrl("https://api.com/v1", "?page=1"));
        assertEquals("https://other.com/data", Request.RequestBuilder.resolveUrl("https://api.com/v1", "https://other.com/data"));
        assertEquals("users", Request.RequestBuilder.resolveUrl(null, "users"));
    }

    @Test
    void testGetRequest() throws Exception {
        Request client = Request.builder()
                .baseUrl("http://127.0.0.1:" + serverPort)
                .timeout(Duration.ofSeconds(5))
                .build();

        Request.Response<String> res = client.get("/test-get").sendAsync().get();
        assertEquals(200, res.statusCode());
        assertEquals("hello world", res.body());
        assertEquals("test-val", res.header("X-Custom-Header"));
    }

    @Test
    void testGetByteArray() throws Exception {
        Request client = Request.builder()
                .baseUrl("http://127.0.0.1:" + serverPort)
                .build();

        Request.Response<byte[]> res = client.get("/test-get")
                .sendAsync(Request.BodyHandlers.ofByteArray())
                .get();
        assertEquals(200, res.statusCode());
        assertArrayEquals("hello world".getBytes(StandardCharsets.UTF_8), res.body());
    }

    @Test
    void testGetLines() throws Exception {
        Request client = Request.builder()
                .baseUrl("http://127.0.0.1:" + serverPort)
                .build();

        Request.Response<java.util.stream.Stream<String>> res = client.get("/test-lines")
                .sendAsync(Request.BodyHandlers.ofLines())
                .get();
        assertEquals(200, res.statusCode());
        assertEquals("line1,line2,line3", res.body().collect(Collectors.joining(",")));
    }

    @Test
    void testPostJsonWithAuth() throws Exception {
        AtomicInteger refreshCalls = new AtomicInteger();
        AuthProvider authProvider = new AuthProvider() {
            @Override
            public CompletableFuture<Void> refreshIfNeeded() {
                refreshCalls.incrementAndGet();
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public String getAccessToken() {
                return "secret-token";
            }
        };

        Request client = Request.builder()
                .baseUrl("http://127.0.0.1:" + serverPort)
                .authProvider(authProvider)
                .build();

        Request.Response<String> res = client.post("/test-post")
                .json("{\"foo\":\"bar\"}")
                .sendAsync()
                .get();

        assertEquals(200, res.statusCode());
        assertEquals("echo:{\"foo\":\"bar\"}|auth:Bearer secret-token", res.body());
        assertEquals(1, refreshCalls.get());
    }

    @Test
    void testPostWithoutAuth() throws Exception {
        AuthProvider authProvider = new AuthProvider() {
            @Override
            public CompletableFuture<Void> refreshIfNeeded() {
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public String getAccessToken() {
                return "secret-token";
            }
        };

        Request client = Request.builder()
                .baseUrl("http://127.0.0.1:" + serverPort)
                .authProvider(authProvider)
                .build();

        Request.Response<String> res = client.post("/test-post")
                .withoutAuth()
                .body("raw-data")
                .sendAsync()
                .get();

        assertEquals(200, res.statusCode());
        assertEquals("echo:raw-data|auth:null", res.body());
    }

    @Test
    void testErrorResponse() throws Exception {
        Request client = Request.builder()
                .baseUrl("http://127.0.0.1:" + serverPort)
                .build();

        Request.Response<String> res = client.get("/test-error").sendAsync().get();
        assertEquals(404, res.statusCode());
        assertEquals("{\"error\":\"not found\"}", res.body());
    }

    @Test
    void testMultipartBodyBuilder() {
        byte[] body = Request.buildMultipartBody("test-boundary", "file-content".getBytes(StandardCharsets.UTF_8), "file.txt", "abc123hash");
        String text = new String(body, StandardCharsets.UTF_8);
        assertTrue(text.contains("--test-boundary"));
        assertTrue(text.contains("name=\"hash\""));
        assertTrue(text.contains("abc123hash"));
        assertTrue(text.contains("name=\"file\"; filename=\"file.txt\""));
        assertTrue(text.contains("file-content"));
    }
}
