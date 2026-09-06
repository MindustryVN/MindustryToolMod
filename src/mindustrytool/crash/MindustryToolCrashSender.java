package mindustrytool.crash;

import mindustrytool.services.MindustryTool;

import java.util.concurrent.CompletableFuture;

/**
 * Production {@link CrashSender} that delegates to {@link MindustryTool#submitCrashReport(String)}.
 * All HTTP goes through {@link mindustrytool.services.Request} via the facade,
 * satisfying the HTTP client rule.
 */
public final class MindustryToolCrashSender implements CrashSender {

    @Override
    public CompletableFuture<Void> send(String content) {
        return MindustryTool.submitCrashReport(content);
    }
}
