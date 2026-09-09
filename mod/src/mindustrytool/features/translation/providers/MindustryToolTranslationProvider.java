package mindustrytool.features.translation.providers;

import arc.Core;
import arc.util.serialization.Jval;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import mindustrytool.Config;
import mindustrytool.features.translation.TranslationFeature;
import mindustrytool.features.translation.TranslationProvider;
import mindustrytool.services.Request;
import mindustrytool.services.auth.MindustryAuthProvider;

/**
 * Translation provider using MindustryTool backend translation service.
 */
public class MindustryToolTranslationProvider implements TranslationProvider {

	public static final String ID = "mindustrytool";
	private final TranslationFeature feature;

	public MindustryToolTranslationProvider(TranslationFeature feature) {
		this.feature = feature;
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return Core.bundle.get("feature.translation.provider.mindustrytool", "MindustryTool Account");
	}

	@Override
	public boolean isConfigured() {
		return MindustryAuthProvider.getInstance().isLoggedIn();
	}

	@Override
	public CompletableFuture<String> translate(String text, String targetLanguage) {
		if (text == null || text.trim().isEmpty()) {
			return CompletableFuture.completedFuture("");
		}

		if (!MindustryAuthProvider.getInstance().isLoggedIn()) {
			CompletableFuture<String> failed = new CompletableFuture<>();
			failed.completeExceptionally(
					new IllegalStateException(Core.bundle.get("feature.translation.error.not-logged-in")));
			return failed;
		}

		int timeout = feature.mindustryToolTimeoutConfig.get();

		Jval body = Jval.newObject();
		body.put("content", text);
		body.put("target", targetLanguage);

		Request api = Request.builder()
				.baseUrl(Config.API_URL)
				.timeout(Duration.ofSeconds(Math.max(5, timeout)))
				.authProvider(MindustryAuthProvider.getInstance())
				.build();

		return api.post("/translations/translate")
				.json(body.toString())
				.sendAsync()
				.thenApply(response -> parseMindustryToolResponse(response.statusCode(), response.body(), text));
	}

	public static String parseMindustryToolResponse(int statusCode, String body, String fallback) {
		if (statusCode == 401 || statusCode == 403) {
			throw new RuntimeException(Core.bundle != null
					? Core.bundle.get("feature.translation.error.not-logged-in", "Not logged in")
					: "Not logged in");
		}
		if (statusCode == 429) {
			throw new RuntimeException(Core.bundle != null
					? Core.bundle.get("feature.translation.error.rate-limit", "Rate limit exceeded")
					: "Rate limit exceeded");
		}
		if (statusCode >= 500) {
			throw new RuntimeException(Core.bundle != null
					? Core.bundle.get("feature.translation.error.server-error", "Server error")
					: "Server error");
		}
		if (statusCode != 200) {
			throw new RuntimeException((Core.bundle != null
					? Core.bundle.get("feature.translation.error.network", "Network error")
					: "Network error") + " (HTTP " + statusCode + ")");
		}

		if (body == null || body.trim().isEmpty()) {
			return fallback;
		}

		String clean = body.trim();
		// If response is a JSON string or JSON object containing "translated"
		if (clean.startsWith("\"") && clean.endsWith("\"") && clean.length() >= 2) {
			clean = clean.substring(1, clean.length() - 1);
		} else if (clean.startsWith("{")) {
			try {
				Jval json = Jval.read(clean);
				if (json.has("translated")) {
					return json.getString("translated", fallback);
				}
				if (json.has("result")) {
					return json.getString("result", fallback);
				}
			} catch (Exception ignored) {
			}
		}
		return clean;
	}
}
