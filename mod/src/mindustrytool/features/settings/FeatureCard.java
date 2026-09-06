package mindustrytool.features.settings;

import arc.Core;
import arc.graphics.Color;
import arc.scene.event.ClickListener;
import arc.scene.event.InputEvent;
import arc.scene.style.Drawable;
import arc.scene.ui.Button;
import arc.scene.ui.layout.Table;
import arc.util.Log;
import arc.util.Scaling;
import mindustry.gen.Icon;
import mindustry.ui.Styles;
import mindustrytool.features.Feature;

/**
 * Component responsible for building and managing a single feature's visual card.
 * Handles display of metadata, action shortcuts (help, settings, main dialog),
 * and state toggling.
 */
public class FeatureCard {

    /**
     * Builds a feature card and attaches it to the given parent table.
     *
     * @param parent         the parent container table
     * @param feature        the feature to render
     * @param cardWidth      computed card width for responsive layout
     * @param onStateChanged callback to invoke when feature state is modified
     */
    public static void build(Table parent, Feature feature, float cardWidth, Runnable onStateChanged) {
        boolean enabled = feature.isEnabled();
        var metadata = feature.getMetadata();

        var card = parent.button(Styles.black8, () -> {})
                .name("FeatureCard-" + metadata.getId())
                .width(cardWidth - 10f)
                .height(180f)
                .pad(5f)
                .top()
                .left()
                .color(enabled ? Color.green : Color.scarlet)
                .get();

        card.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (event.stopped) {
                    return;
                }
                try {
                    feature.setEnabled(!feature.isEnabled());
                    if (onStateChanged != null) {
                        onStateChanged.run();
                    }
                } catch (Exception e) {
                    Log.err("Failed to toggle feature " + metadata.getId(), e);
                }
            }
        });

        card.top().left();
        card.table(container -> {
            container.name = "cardContainer";
            container.top().left().margin(12f);

            // Card Header
            container.table(header -> {
                header.left();

                Drawable icon = metadata.getIcon();
                if (icon != null) {
                    header.image(icon)
                            .scaling(Scaling.fit)
                            .size(24f)
                            .padRight(8f);
                }

                header.add(feature.getName())
                        .style(Styles.defaultLabel)
                        .color(Color.white)
                        .ellipsis(true)
                        .left();

                header.table().minWidth(5f).growX();

                // Main dialog button (if feature has a dedicated primary dialog)
                if (feature.getMainDialog() != null) {
                    var mainBtn = header.button(Icon.linkSmall, Styles.clearNonei, () -> {})
                            .size(32f)
                            .tooltip(Core.bundle.get("feature.button.open-dialog"))
                            .get();
                    attachChildClick(mainBtn, () -> Core.app.post(() -> feature.getMainDialog().show()));
                }

                // Settings button (if feature provides settings dialog)
                if (feature.getSettingDialog() != null) {
                    var settingBtn = header.button(Icon.settings, Styles.clearNonei, () -> {})
                            .size(32f)
                            .tooltip(Core.bundle.get("feature.button.settings"))
                            .get();
                    attachChildClick(settingBtn, () -> Core.app.post(() -> feature.getSettingDialog().show()));
                }

                // Help button
                var helpBtn = header.button(Icon.infoCircle, Styles.clearNonei, () -> {})
                        .size(32f)
                        .tooltip(Core.bundle.get("feature.button.help"))
                        .get();
                attachChildClick(helpBtn, () -> new FeatureHelpDialog(feature).show());
            }).width(cardWidth - 34f).growX().row();

            // Feature description
            container.add(feature.getDescription())
                    .color(Color.lightGray)
                    .fontScale(0.9f)
                    .wrap()
                    .growX()
                    .padTop(10f)
                    .ellipsis(true)
                    .row();

            // Spacer
            container.add().growY().row();

            // Enabled / Disabled status
            String statusText = enabled
                    ? Core.bundle.get("feature.status.enabled")
                    : Core.bundle.get("feature.status.disabled");
            container.add(statusText)
                    .color(enabled ? Color.green : Color.scarlet)
                    .left();
        }).pad(4f).grow().top().left();
    }

    private static void attachChildClick(Button button, Runnable action) {
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                event.stop();
                if (action != null) {
                    action.run();
                }
            }
        });
    }
}
