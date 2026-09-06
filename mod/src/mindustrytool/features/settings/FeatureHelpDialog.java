package mindustrytool.features.settings;

import arc.Core;
import arc.graphics.Color;
import mindustry.ui.dialogs.BaseDialog;
import mindustrytool.features.Feature;

/**
 * Dedicated dialog displaying help and usage documentation for a specific {@link Feature}.
 */
public class FeatureHelpDialog extends BaseDialog {

    public FeatureHelpDialog(Feature feature) {
        super(Core.bundle.format("feature.help.title", feature.getName()));

        addCloseButton();
        closeOnBack();

        String help = feature.getHelp();
        boolean hasHelp = help != null && !help.trim().isEmpty();
        String text = hasHelp ? help : Core.bundle.get("feature.help.no-description");

        cont.pane(pane -> {
            pane.margin(16f);
            var label = pane.add(text)
                    .wrap()
                    .width(480f)
                    .left();
            if (!hasHelp) {
                label.color(Color.lightGray);
            }
        }).scrollX(false).scrollY(true).grow();
    }
}
