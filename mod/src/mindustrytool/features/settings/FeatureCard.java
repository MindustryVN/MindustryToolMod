package mindustrytool.features.settings;

import static solim.ui.Ui.*;

import arc.Core;
import arc.graphics.Color;
import arc.scene.Element;
import arc.util.Scaling;
import lombok.AllArgsConstructor;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.ui.Styles;
import mindustrytool.features.Feature;
import solim.core.BaseComponent;
import solim.signal.Readable;

/**
 * Component responsible for building and managing a single feature's visual
 * card. Handles display
 * of metadata, action shortcuts (help, settings, main dialog), and state
 * toggling with direct
 * property reactivity using pure Solim components.
 */
@AllArgsConstructor
public class FeatureCard extends BaseComponent {

    private final Feature feature;
    private final Readable<Float> cardWidth;

    @Override
    protected Element build() {
        var metadata = feature.getMetadata();

        return card(Styles.black8)
                .name("FeatureCard-" + metadata.getId())
                .height(unit(40))
                .padding(unit(2))
                .width(cardWidth.map(w -> Math.max(0f, w - 10f)))
                .color(feature.enabled().map(value -> Boolean.TRUE.equals(value) ? Color.green : Color.scarlet))
                .onClick(() -> feature.setEnabled(!feature.isEnabled()))
                .children(() -> {
                    column()
                            .grow()
                            .children(() -> {
                                row()
                                        .children(() -> {
                                            image(metadata.getIcon()).scaling(Scaling.fit).size(unit(6));

                                            text(feature.getName())
                                                    .style(Styles.defaultLabel)
                                                    .color(Color.white)
                                                    .ellipsis(true)
                                                    .left();

                                            spacer();

                                            if (feature.getMainDialog() != null) {
                                                button(() -> feature.getMainDialog().show())
                                                        .style(Styles.clearNonei)
                                                        .size(unit(9))
                                                        .tooltip(Core.bundle.get("feature.button.open-dialog"))
                                                        .children(() -> image(Icon.linkSmall));
                                            }

                                            if (feature.getSettingDialog() != null) {
                                                button(() -> feature.getSettingDialog().show())
                                                        .style(Styles.clearNonei)
                                                        .size(unit(9))
                                                        .tooltip(Core.bundle.get("feature.button.settings"))
                                                        .children(() -> image(Icon.settings));
                                            }

                                            button(() -> new FeatureHelpDialog(feature).show())
                                                    .style(Styles.clearNonei)
                                                    .size(unit(9))
                                                    .tooltip(Core.bundle.get("feature.button.help"))
                                                    .children(() -> image(Icon.infoCircle));
                                        });

                                text(feature.getDescription())
                                        .color(Color.lightGray)
                                        .fontScale(0.9f)
                                        .wrap(true)
                                        .ellipsis(true)
                                        .left();

                                spacer();

                                text(feature.enabled()
                                        .map(val -> Boolean.TRUE.equals(val)
                                                ? Core.bundle.get("feature.status.enabled")
                                                : Core.bundle.get("feature.status.disabled")))
                                        .style(Styles.defaultLabel)
                                        .color(feature.enabled()
                                                .map(val -> Boolean.TRUE.equals(val) ? Color.green : Color.scarlet))
                                        .left();

                                image(Tex.whiteui)
                                        .color(feature.enabled()
                                                .map(val -> Boolean.TRUE.equals(val) ? Color.green : Color.scarlet))
                                        .height(unit(0.5f));
                            });
                })
                .element();
    }
}
