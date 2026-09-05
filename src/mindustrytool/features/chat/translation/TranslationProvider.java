package mindustrytool.features.chat.translation;

import arc.scene.ui.layout.Table;

import java.util.concurrent.CompletableFuture;

public interface TranslationProvider {
    CompletableFuture<String> translate(String message);

    default CompletableFuture<String> translate(String message, String targetLang) {
        return translate(message);
    }

    Table settings();

    default void init() {
    };

    String getName();

    String getId();
}
