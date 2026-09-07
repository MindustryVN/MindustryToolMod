package mindustrytool.features.settings;

import arc.Core;
import mindustry.gen.Icon;
import mindustrytool.Config;
import solim.overlay.SolimDialog;

public final class FeatureSettingDialog extends SolimDialog {
    public FeatureSettingDialog() {
        super(Core.bundle.get("feature.dialog.title", "Features"));
        addCloseButton();
        closeOnBack();
        content(new FeatureSettingsView());
        actionButton(Core.bundle.get("feature.button.report-bug"), Icon.infoCircle,
                () -> Core.app.openURI(Config.DISCORD_INVITE_URL));
    }
}
