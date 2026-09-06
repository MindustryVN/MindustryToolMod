package mindustrytool.services;

import java.util.concurrent.CompletableFuture;

public interface AuthProvider {

    CompletableFuture<Void> refreshIfNeeded();

    String getAccessToken();
}
