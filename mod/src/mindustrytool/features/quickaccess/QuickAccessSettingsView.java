package mindustrytool.features.quickaccess;

import static solim.ui.Ui.*;

import arc.Core;
import arc.graphics.Color;
import arc.scene.Element;
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
                                                    .gap(unit(2))
                                                    .children(() -> {
                                                        text(Core.bundle.get("feature.quick-access.settings.opacity"))
                                                                .left();
                                                        slider(feature.getOpacityConfig().signal(), 0.05f, 1.0f, 0.05f);
                                                        text(feature.getOpacityConfig().signal()
                                                                .map(v -> String.format("%.0f%%", v * 100)));
                                                    });

                                            row()
                                                    .gap(unit(2))
                                                    .children(() -> {
                                                        text(Core.bundle.get("feature.quick-access.settings.scale"))
                                                                .left();
                                                        slider(feature.getScaleConfig().signal(), 0.5f, 1.5f, 0.1f);
                                                        text(feature.getScaleConfig().signal()
                                                                .map(v -> String.format("%.0f%%", v * 100)));
                                                    });

                                            row()
                                                    .gap(unit(2))
                                                    .children(() -> {
                                                        text(Core.bundle.get("feature.quick-access.settings.columns"))
                                                                .left();
                                                        slider(feature.getColsConfig().signal(), 1, 9, 1);
                                                        text(feature.getColsConfig().signal().map(String::valueOf));
                                                    });

                                            divider();

                                            text(Core.bundle.get("feature.quick-access.settings.visible-features"))
                                                    .color(Color.white);

                                            
                                            for (Feature f : FeatureManager.getFeatures()
                                                    .select(f -> f != feature && f.getMetadata().isQuickAccess())) {

                                                FeatureMetadata meta = f.getMetadata();

                                                checkbox(f.getName(), feature.isFeatureVisible(meta.getId()),
                                                        visible -> feature.setFeatureVisible(meta.getId(), visible));
                                            }

                                            button(Core.bundle.get("feature.quick-access.settings.reset-position"),
                                                    feature::resetPosition);
                                        });
                            });
                })
                .element();
    }
}
