package mindustrytool.crash;

import arc.Core;
import arc.files.Fi;
import arc.scene.ui.layout.Scl;
import arc.util.Log;
import mindustry.ui.dialogs.BaseDialog;

/**
 * UI dialog for crash report opt-in and submission.
 * Single Responsibility: render consent UI and delegate sending to {@link CrashSender}.
 * Uses the new API via {@link mindustrytool.services.MindustryTool} through the injected sender,
 * never constructing {@code HttpClient}/{@code HttpRequest} or {@code arc.util.Http} directly.
 */
public class CrashReportDialog extends BaseDialog {

    private static final String KEY_SEND = "mindustrytool.crash-report.send";

    public CrashReportDialog(Fi file, String data, CrashSender sender) {
        super("@crash-report.title");
        name = "crashReportDialog";

        boolean sendCrashReport = Core.settings.getBool(KEY_SEND, true);

        addCloseButton();
        closeOnBack();

        cont.table(container -> {
            container.add("@crash-report.content").padBottom(10f).wrapLabel(true).wrap().growX().row();
            container.check("@crash-report.send", sendCrashReport,
                    value -> Core.settings.put(KEY_SEND, value)).wrapLabel(false).growX().row();
            // Show file path for context; not user-editable.
            String path = file != null ? file.absolutePath() : "";
            container.add(path).padBottom(10f).wrapLabel(true).wrap().growX().row();
            container.pack();
        }).width(Math.min(600, Core.graphics.getWidth() / Scl.scl() / 1.2f));

        // Send only on dialog hide if user still consents; captures sender/data via closure.
        // Fixes race: checkbox listener already persisted choice, hidden reads latest value.
        hidden(() -> {
            if (!Core.settings.getBool(KEY_SEND, true)) return;
            if (data == null || data.isEmpty()) return;
            try {
                sender.send(data).exceptionally(err -> {
                    Log.err("Failed to submit crash report", err);
                    return null;
                });
            } catch (Exception err) {
                Log.err("Failed to submit crash report", err);
            }
        });
    }

    /** Convenience ctor for production — uses default sender. */
    public CrashReportDialog(Fi file, String data) {
        this(file, data, new MindustryToolCrashSender());
    }
}
