package mindustrytool.features.settings;

import arc.Core;
import arc.scene.style.Drawable;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustrytool.Config;
import solim.overlay.SolimDialog;

public final class FeatureSettingDialog extends SolimDialog {
    private final FeatureSettingsView view;

    public FeatureSettingDialog() {
        super(Core.bundle.get("feature.dialog.title", "Features"));
        addCloseButton();
        closeOnBack();
        this.view = new FeatureSettingsView();
        content(view);
        actionButton(Core.bundle.get("feature.button.report-bug"), icon("infoCircle"), 180f, 50f,
                () -> openUri(Config.DISCORD_INVITE_URL, Core.bundle.get("feature.toast.copied")));
    }

    public static Drawable icon(String name) {
        if (name == null)
            return null;
        try {
            return (Drawable) Icon.class.getField(name).get(null);
        } catch (Throwable t) {
            return null;
        }
    }

    public static boolean openUri(String uri, String fallbackToast) {
        if (Core.app != null && Core.app.openURI(uri))
            return true;
        if (Core.app != null && uri != null)
            Core.app.setClipboardText(uri);
        if (Vars.ui != null && fallbackToast != null)
            Vars.ui.showInfoFade(fallbackToast);
        return false;
    }

    public FeatureSettingsView view() {
        return view;
    }
}
