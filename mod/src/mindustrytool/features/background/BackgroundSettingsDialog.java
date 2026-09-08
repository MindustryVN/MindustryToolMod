package mindustrytool.features.background;

import arc.Core;
import solim.overlay.SolimDialog;

public class BackgroundSettingsDialog extends SolimDialog {

    public BackgroundSettingsDialog(BackgroundFeature feature) {
        super(Core.bundle.get("feature.background.settings.title"));

        name = "backgroundSettingDialog";
        addCloseButton();
        closeOnBack();

        content(new BackgroundSettingsView(feature));
    }
}
