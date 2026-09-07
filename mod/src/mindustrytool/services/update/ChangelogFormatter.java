package mindustrytool.services.update;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Pure changelog formatting extracted from {@code UpdateService.fetchReleasesAndShowDialog}. No
 * network, no Arc, no bundle dependency — pure Java for unit testing.
 */
public final class ChangelogFormatter {

	private static final int MAX_RELEASES = 20;
	private static final DateTimeFormatter DATE_FORMATTER =
			DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());
	private static final ObjectMapper MAPPER = new ObjectMapper();

	private ChangelogFormatter() {}

	/** Release DTO for pure formatting. */
	public static final class ReleaseInfo {
		public final String tagName;
		public final String body;
		public final String publishedAt;
		public final int downloadCount;

		public ReleaseInfo(String tagName, String body, String publishedAt, int downloadCount) {
			this.tagName = tagName != null ? tagName : "";
			this.body = body;
			this.publishedAt = publishedAt != null ? publishedAt : "";
			this.downloadCount = downloadCount;
		}
	}

	/** Formats raw GitHub releases JSON string (array) into Mindustry markup. Caps at 20 entries. */
	public static String format(String releasesJson) {
		if (releasesJson == null || releasesJson.isBlank()) {
			return "Could not parse release notes.";
		}
		try {
			JsonNode root = MAPPER.readTree(releasesJson);
			if (!root.isArray()) {
				return "Could not parse release notes.";
			}
			List<ReleaseInfo> list = new ArrayList<>();
			for (JsonNode node : root) {
				if (list.size() >= MAX_RELEASES) break;
				String tagName = node.path("tag_name").asText("");
				String body = node.has("body") && !node.path("body").isNull()
						? node.path("body").asText("No description provided.")
						: "No description provided.";
				String publishedAt = node.path("published_at").asText("");
				int downloadCount = 0;
				JsonNode assets = node.path("assets");
				if (assets.isArray()) {
					for (JsonNode asset : assets) {
						downloadCount += asset.path("download_count").asInt(0);
					}
				}
				list.add(new ReleaseInfo(tagName, body, publishedAt, downloadCount));
			}
			return formatReleases(list);
		} catch (Exception e) {
			return "Could not parse release notes.";
		}
	}

	/** Pure formatting from structured releases. */
	public static String formatReleases(List<ReleaseInfo> releases) {
		return formatReleases(releases, ZoneId.systemDefault(), DATE_FORMATTER);
	}

	/** Overload with injectable zone/formatter for deterministic tests. */
	public static String formatReleases(List<ReleaseInfo> releases, ZoneId zoneId, DateTimeFormatter formatter) {
		if (releases == null || releases.isEmpty()) {
			return "";
		}
		StringBuilder changelog = new StringBuilder();
		int count = 0;
		for (ReleaseInfo release : releases) {
			if (count >= MAX_RELEASES) break;
			changelog.append("[accent]").append(release.tagName).append("[white]\n");

			if (release.publishedAt != null && !release.publishedAt.isEmpty()) {
				try {
					Instant instant = Instant.parse(release.publishedAt);
					DateTimeFormatter fmt = formatter != null ? formatter : DATE_FORMATTER;
					if (fmt.getZone() == null && zoneId != null) {
						fmt = fmt.withZone(zoneId);
					}
					changelog.append("[lightgray]").append(fmt.format(instant)).append("[white] - ");
				} catch (Exception ignored) {
				}
			}

			changelog
					.append("[gold]Download count: ")
					.append(release.downloadCount)
					.append("[white]\n");

			String body = release.body != null && !release.body.isEmpty() ? release.body : "No description provided.";
			changelog.append(renderMarkdown(body)).append("\n\n");
			count++;
		}
		return changelog.toString();
	}

	/** Markdown to Mindustry markup, copied from {@code old.mindustrytool.Utils.renderMarkdown}. */
	public static String renderMarkdown(String text) {
		if (text == null) return "";
		text = text.replaceAll("\\[(.*?)\\]\\((.*?)\\)", "[sky]$1[white]");
		text = text.replaceAll("(?m)^#{1,6}\\s+(.*)$", "[accent]$1[white]");
		text = text.replaceAll("(?m)^\\s*[-*]\\s+(.*)$", "• $1");
		text = text.replaceAll("\\*\\*(.*?)\\*\\*", "[white]$1[white]");
		text = text.replaceAll("(?<!\\*)\\*(?!\\*)(.*?)(?<!\\*)\\*(?!\\*)", "[lightgray]$1[white]");
		text = text.replaceAll("`([^`]*)`", "[cyan]$1[white]");
		return text;
	}
}
