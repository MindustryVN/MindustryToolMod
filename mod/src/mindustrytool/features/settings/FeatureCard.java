package mindustrytool.features.settings;

import arc.Core;
import arc.graphics.Color;
import arc.scene.Element;
import arc.scene.style.Drawable;
import arc.util.Log;
import arc.util.Scaling;
import mindustry.gen.Icon;
import mindustry.ui.Styles;
import mindustrytool.features.Feature;
import solim.core.BaseComponent;
import solim.signal.Readable;

import static solim.ui.Ui.*;

/**
 * Component responsible for building and managing a single feature's visual card.
 * Handles display of metadata, action shortcuts (help, settings, main dialog),
 * and state toggling with direct property reactivity using pure Solim components.
 */
public class FeatureCard extends BaseComponent {

    private final Feature feature;
    private final Readable<Float> cardWidth;
    private final Readable<Boolean> enabled;
    private final Runnable onStateChanged;

    public FeatureCard(Feature feature, Readable<Float> cardWidth, Readable<Boolean> enabled, Runnable onStateChanged) {
        this.feature = feature;
        this.cardWidth = cardWidth;
        this.enabled = enabled != null ? enabled : (feature != null ? feature.enabled() : null);
        this.onStateChanged = onStateChanged;
    }

    public FeatureCard(Feature feature, Readable<Float> cardWidth, Readable<Boolean> enabled) {
        this(feature, cardWidth, enabled, null);
    }

    @Override
    protected Element build() {
        var metadata = feature.getMetadata();

        return card(Styles.black8, () -> {
            // Card Header
            row(() -> {
                Drawable icon = metadata != null ? metadata.getIcon() : null;
                if (icon != null) {
                    image(icon)
                            .scaling(Scaling.fit)
                            .size(24f);
                }

                text(feature.getName() != null ? feature.getName() : "")
                        .style(Styles.defaultLabel)
                        .color(Color.white)
                        .ellipsis(true)
                        .left();

                spacer();

                // Main dialog button (if feature has a dedicated primary dialog)
                if (feature.getMainDialog() != null) {
                    iconButton(Icon.linkSmall, Styles.clearNonei, () -> Core.app.post(() -> feature.getMainDialog().show()))
                            .size(32f)
                            .tooltip(Core.bundle.get("feature.button.open-dialog"));
                }

                // Settings button (if feature provides settings dialog)
                if (feature.getSettingDialog() != null) {
                    iconButton(Icon.settings, Styles.clearNonei, () -> Core.app.post(() -> feature.getSettingDialog().show()))
                            .size(32f)
                            .tooltip(Core.bundle.get("feature.button.settings"));
                }

                // Help button
                iconButton(Icon.infoCircle, Styles.clearNonei, () -> new FeatureHelpDialog(feature).show())
                        .size(32f)
                        .tooltip(Core.bundle.get("feature.button.help"));
            }).gap(8f);

            // Feature description
            text(feature.getDescription() != null ? feature.getDescription() : "")
                    .color(Color.lightGray)
                    .fontScale(0.9f)
                    .wrap(true)
                    .ellipsis(true)
                    .left();

            // Spacer consuming vertical space
            spacer();

            // Enabled / Disabled status label with direct reactive property binding
            text(enabled.map(val -> Boolean.TRUE.equals(val)
                    ? Core.bundle.get("feature.status.enabled")
                    : Core.bundle.get("feature.status.disabled")))
                    .style(Styles.defaultLabel)
                    .color(enabled.map(val -> Boolean.TRUE.equals(val) ? Color.green : Color.scarlet))
                    .left();
        })
                .name("FeatureCard-" + (metadata != null ? metadata.getId() : "unknown"))
                .prefHeight(180f)
                .padding(12f)
                .width(cardWidth != null ? cardWidth.map(w -> Math.max(0f, w - 10f)) : null)
                .color(enabled.map(value -> Boolean.TRUE.equals(value) ? Color.green : Color.scarlet))
                .onClick(this::toggleFeature)
                .element();
    }

    private void toggleFeature() {
        try {
            feature.setEnabled(!feature.isEnabled());
            if (onStateChanged != null) {
                onStateChanged.run();
            }
        } catch (Exception e) {
            Log.err("Failed to toggle feature " + (feature.getMetadata() != null ? feature.getMetadata().getId() : ""), e);
        }
    }

    public Readable<Boolean> enabled() {
        return enabled;
    }
}
