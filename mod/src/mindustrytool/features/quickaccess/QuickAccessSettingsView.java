package mindustrytool.features.quickaccess;

import static solim.ui.Ui.*;

import arc.Core;
import arc.graphics.Color;
import arc.scene.Element;
import mindustry.ui.Styles;
import mindustrytool.features.Feature;
import mindustrytool.features.FeatureManager;
import mindustrytool.features.FeatureMetadata;
import solim.core.BaseComponent;

public class QuickAccessSettingsView extends BaseComponent {

    private final QuickAccessFeature feature;

    public QuickAccessSettingsView(QuickAccessFeature feature) {
        this.feature = feature;
    }

    @Override
    protected Element build() {
        return column()
                .grow()
                .children(() -> {
                    scroll()
                            .grow()
                            .children(() -> {
                                column()
                                        .growX()
                                        .gap(unit(2))
                                        .children(() -> {
                                            row()
                                                    .center()
                                                    .growX()
                                                    .gap(unit(2))
                                                    .children(() -> {
                                                        text(Core.bundle.get("feature.quick-access.settings.opacity"))
                                                                .left();
                                                        slider(feature.opacityConfig.signal(), 0.05f, 1.0f, 0.05f);
                                                        text(feature.opacityConfig.signal()
                                                                .map(v -> String.format("%.0f%%", v * 100)));
                                                    });

                                            row()
                                                    .center()
                                                    .growX()
                                                    .gap(unit(2))
                                                    .children(() -> {
                                                        text(Core.bundle.get("feature.quick-access.settings.scale"))
                                                                .left();
                                                        slider(feature.scaleConfig.signal(), 0.5f, 1.5f, 0.1f);
                                                        text(feature.scaleConfig.signal()
                                                                .map(v -> String.format("%.0f%%", v * 100)));
                                                    });

                                            row()
                                                    .center()
                                                    .growX()
                                                    .gap(unit(2))
                                                    .children(() -> {
                                                        text(Core.bundle.get("feature.quick-access.settings.columns"))
                                                                .left();
                                                        slider(feature.colsConfig.signal(), 1, 9, 1);
                                                        text(feature.colsConfig.signal().map(String::valueOf));
                                                    });

                                            divider();

                                            text(Core.bundle.get("feature.quick-access.settings.visible-features"))
                                                    .center()
                                                    .color(Color.white);

                                            for (Feature f : FeatureManager.getFeatures()
                                                    .select(f -> f != feature && f.getMetadata().isQuickAccess())) {

                                                FeatureMetadata meta = f.getMetadata();

                                                checkbox(f.getName(), feature.isFeatureVisible(meta.getId()),
                                                        visible -> feature.setFeatureVisible(meta.getId(), visible));
                                            }

                                            button(Core.bundle.get("feature.quick-access.settings.reset-position"),
                                                    feature::resetPosition)
                                                    .style(Styles.defaultb);
                                        });
                            });
                })
                .element();
    }
}
