package mindustrytool.features.settings;

import arc.Core;
import arc.graphics.Color;
import arc.scene.Element;
import arc.util.Scaling;
import lombok.AllArgsConstructor;
import mindustry.gen.Icon;
import mindustry.ui.Styles;
import mindustrytool.features.Feature;
import solim.core.BaseComponent;
import solim.signal.Readable;

import static solim.ui.Ui.*;

/**
 * Component responsible for building and managing a single feature's visual
 * card.
 * Handles display of metadata, action shortcuts (help, settings, main dialog),
 * and state toggling with direct property reactivity using pure Solim
 * components.
 */
@AllArgsConstructor
public class FeatureCard extends BaseComponent {

    private final Feature feature;
    private final Readable<Float> cardWidth;

    @Override
    protected Element build() {
        var metadata = feature.getMetadata();

        return card(Styles.black8, () -> {
            row(() -> {
                image(metadata.getIcon())
                        .scaling(Scaling.fit)
                        .size(24f);

                text(feature.getName())
                        .style(Styles.defaultLabel)
                        .color(Color.white)
                        .ellipsis(true)
                        .left();

                spacer();

                if (feature.getMainDialog() != null) {
                    iconButton(Icon.linkSmall, Styles.clearNonei,
                            () -> Core.app.post(() -> feature.getMainDialog().show()))
                            .size(32f)
                            .tooltip(Core.bundle.get("feature.button.open-dialog"));
                }

                if (feature.getSettingDialog() != null) {
                    iconButton(Icon.settings, Styles.clearNonei,
                            () -> Core.app.post(() -> feature.getSettingDialog().show()))
                            .size(32f)
                            .tooltip(Core.bundle.get("feature.button.settings"));
                }

                iconButton(Icon.infoCircle, Styles.clearNonei, () -> new FeatureHelpDialog(feature).show())
                        .size(32f)
                        .tooltip(Core.bundle.get("feature.button.help"));
            }).gap(8f);

            text(feature.getDescription())
                    .color(Color.lightGray)
                    .fontScale(0.9f)
                    .wrap(true)
                    .ellipsis(true)
                    .left();

            spacer();

            text(feature.enabled().map(val -> Boolean.TRUE.equals(val)
                    ? Core.bundle.get("feature.status.enabled")
                    : Core.bundle.get("feature.status.disabled"))//
            )
                    .style(Styles.defaultLabel)
                    .color(feature.enabled().map(val -> Boolean.TRUE.equals(val) ? Color.green : Color.scarlet))
                    .left();
        })
                .name("FeatureCard-" + metadata.getId())
                .prefHeight(180f)
                .padding(12f)
                .width(cardWidth.map(w -> Math.max(0f, w - 10f)))
                .color(feature.enabled().map(value -> Boolean.TRUE.equals(value) ? Color.green : Color.scarlet))
                .onClick(() -> feature.setEnabled(!feature.isEnabled()))
                .element();
    }
}
