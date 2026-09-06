package mindustrytool.update;

import arc.Core;
import arc.util.Log;
import arc.util.serialization.Jval;
import mindustrytool.Main;
import mindustrytool.services.Github;
import mindustrytool.services.MindustryTool;

/**
 * Update check orchestrator. Directly delegates to {@link Github} / {@link MindustryTool}
 * and shows {@link UpdateDialog} — no indirection interfaces.
 */
public final class UpdateService {

    private static volatile UpdateService instance;

    private UpdateService() {
    }

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

        String currentVersionString = Main.self != null && Main.self.meta != null
                ? Main.self.meta.version
                : "0";
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
                    Log.info(bundleFormat("update.status.require-update", currentVerStr, latestVerStr, fallbackRequireUpdate(currentVerStr, latestVerStr)));
                    fetchReleasesAndShowDialog(currentVerStr, latestVerStr, finalDone);
                } else {
                    Log.info(bundleGet("update.status.up-to-date", "Mod up to date"));
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
                    msg = bundleFormat("update.error.fetch-releases-with-status", detail, "Could not fetch release notes: " + detail);
                } else {
                    msg = bundleGet("update.error.fetch-releases", "Could not fetch release notes.");
                }
                String finalMsg = msg;
                Core.app.post(() -> new UpdateDialog(currentVer, latestVer, finalMsg, done).show());
                return;
            }
            try {
                String changelog = ChangelogFormatter.format(body);
                if (changelog == null || changelog.isBlank()) {
                    changelog = bundleGet("update.error.parse-releases", "Could not parse release notes.");
                }
                String finalChangelog = changelog;
                Core.app.post(() -> new UpdateDialog(currentVer, latestVer, finalChangelog, done).show());
            } catch (Exception e) {
                Log.err("Failed to parse releases", e);
                String msg = bundleGet("update.error.parse-releases", "Could not parse release notes.");
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

    private static String bundleGet(String key, String fallback) {
        try {
            if (Core.bundle != null) {
                String val = Core.bundle.get(key);
                if (val != null && !val.equals(key)) return val;
            }
        } catch (Exception ignored) {
        }
        return fallback;
    }

    private static String bundleFormat(String key, String arg1, String arg2, String fallback) {
        try {
            if (Core.bundle != null) {
                String val = Core.bundle.format(key, arg1, arg2);
                if (val != null && !val.equals(key)) return val;
            }
        } catch (Exception ignored) {
        }
        return fallback;
    }

    private static String bundleFormat(String key, String arg, String fallback) {
        try {
            if (Core.bundle != null) {
                String val = Core.bundle.format(key, arg);
                if (val != null && !val.equals(key)) return val;
            }
        } catch (Exception ignored) {
        }
        return fallback;
    }

    private static String fallbackRequireUpdate(String cur, String latest) {
        return "Mod requires update, current version: " + cur + ", latest version: " + latest;
    }
}
