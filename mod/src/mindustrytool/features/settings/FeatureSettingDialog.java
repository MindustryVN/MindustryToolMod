package mindustrytool.features.settings;

import arc.Core;
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
        actionButton(Core.bundle.get("feature.button.report-bug"), Icon.infoCircle,
                () -> Core.app.openURI(Config.DISCORD_INVITE_URL));
    }

    public FeatureSettingsView view() {
        return view;
    }
}
