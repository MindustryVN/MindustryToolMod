package mindustrytool.features.translation.ui;

import arc.Core;
import mindustry.gen.Icon;
import mindustrytool.features.translation.TranslationFeature;
import solim.overlay.SolimDialog;

public class TranslationSettingsDialog extends SolimDialog {

	public TranslationSettingsDialog(TranslationFeature feature) {
		super(Core.bundle.get("feature.translation.settings.title", "Chat Translation Settings"));

		name = "translationSettingsDialog";
		addCloseButton();
		closeOnBack();

		actionButton(Core.bundle.get("feature.translation.settings.reset", "Reset to Defaults"), Icon.refresh, 220f, 64f, feature::resetToDefaults);

		content(new TranslationSettingsView(feature));
	}
}
