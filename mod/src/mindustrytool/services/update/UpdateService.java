package mindustrytool.services.update;

import arc.Core;
import arc.util.Log;
import arc.util.serialization.Jval;
import mindustrytool.Main;
import mindustrytool.services.Github;
import mindustrytool.services.MindustryTool;

/**
 * Update check orchestrator. Directly delegates to {@link Github} / {@link MindustryTool} and shows
 * {@link UpdateDialog} — no indirection interfaces.
 */
public final class UpdateService {

	private static volatile UpdateService instance;

	private UpdateService() {}

	public static UpdateService getInstance() {
		if (instance == null) {
			synchronized (UpdateService.class) {
				if (instance == null) {
					instance = new UpdateService();
				}
			}
		}
		return instance;
	}

	public void checkForUpdate(Runnable done) {
		if (done == null) done = () -> {};
		final Runnable finalDone = done;

		String currentVersionString = Main.self != null && Main.self.meta != null ? Main.self.meta.version : "0";
		int[] currentVersion = VersionUtils.parseVersion(currentVersionString);
		String currentVerStr = VersionUtils.format(currentVersion);

		// fire-and-forget ping
		try {
			MindustryTool.ping("mod-v8").exceptionally(e -> null);
		} catch (Exception e) {
			Log.err("Ping failed", e);
		}

		Github.getModHjson().whenComplete((body, err) -> {
			if (err != null) {
				Log.err(err);
				safeDone(finalDone);
				return;
			}
			try {
				Jval json = Jval.read(body);
				String latestVersionStr = json.getString("version");
				int[] latestVersion = VersionUtils.parseVersion(latestVersionStr);
				String latestVerStr = VersionUtils.format(latestVersion);

				if (VersionUtils.isGreater(latestVersion, currentVersion)) {
					Log.info(Core.bundle.format("update.status.require-update", currentVerStr, latestVerStr));
					fetchReleasesAndShowDialog(currentVerStr, latestVerStr, finalDone);
				} else {
					Log.info(Core.bundle.get("update.status.up-to-date"));
					safeDone(finalDone);
				}
			} catch (Exception e) {
				safeDone(finalDone);
				Log.err("Failed to check update", e);
			}
		});
	}

	private void fetchReleasesAndShowDialog(String currentVer, String latestVer, Runnable done) {
		Github.getReleases().whenComplete((body, err) -> {
			if (err != null) {
				Log.err("Failed to fetch releases", err);
				String detail = err.getMessage() != null ? err.getMessage() : "";
				String msg;
				if (!detail.isEmpty()) {
					msg = Core.bundle.format("update.error.fetch-releases-with-status", detail);
				} else {
					msg = Core.bundle.get("update.error.fetch-releases");
				}
				String finalMsg = msg;
				Core.app.post(() -> new UpdateDialog(currentVer, latestVer, finalMsg, done).show());
				return;
			}
			try {
				String changelog = ChangelogFormatter.format(body);
				if (changelog == null || changelog.isBlank()) {
					changelog = Core.bundle.get("update.error.parse-releases");
				}
				String finalChangelog = changelog;
				Core.app.post(() -> new UpdateDialog(currentVer, latestVer, finalChangelog, done).show());
			} catch (Exception e) {
				Log.err("Failed to parse releases", e);
				String msg = Core.bundle.get("update.error.parse-releases");
				Core.app.post(() -> new UpdateDialog(currentVer, latestVer, msg, done).show());
			}
		});
	}

	private static void safeDone(Runnable done) {
		try {
			done.run();
		} catch (Exception e) {
			Log.err(e);
		}
	}
}
