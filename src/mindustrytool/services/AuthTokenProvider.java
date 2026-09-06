package mindustrytool.services;

import java.util.concurrent.CompletableFuture;

public interface AuthTokenProvider {

    CompletableFuture<Void> refreshIfNeeded();

    String getToken();

    static AuthTokenProvider noop() {
        return new AuthTokenProvider() {
            @Override
            public CompletableFuture<Void> refreshIfNeeded() {
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public String getToken() {
                return null;
            }
        };
    }
}
