package mindustrytool.features.settings;

import arc.Core;
import arc.graphics.Color;
import arc.scene.Element;
import arc.scene.event.ClickListener;
import arc.scene.event.InputEvent;
import arc.scene.style.Drawable;
import arc.scene.ui.Button;
import arc.scene.ui.Label;
import arc.util.Log;
import arc.util.Scaling;
import mindustry.gen.Icon;
import mindustry.ui.Styles;
import mindustrytool.features.Feature;
import solim.core.BaseComponent;
import solim.signal.Readable;
import solim.ui.Binding;

/**
 * Component responsible for building and managing a single feature's visual card.
 * Handles display of metadata, action shortcuts (help, settings, main dialog),
 * and state toggling with direct property reactivity.
 */
public class FeatureCard extends BaseComponent {

    private final Feature feature;
    private final Readable<Float> cardWidth;
    private final Readable<Boolean> enabled;
    private final Runnable onStateChanged;

    public FeatureCard(Feature feature, Readable<Float> cardWidth, Readable<Boolean> enabled, Runnable onStateChanged) {
        this.feature = feature;
        this.cardWidth = cardWidth;
        this.enabled = enabled != null ? enabled : Readable.of(feature.isEnabled());
        this.onStateChanged = onStateChanged;
    }

    public FeatureCard(Feature feature, Readable<Float> cardWidth, Readable<Boolean> enabled) {
        this(feature, cardWidth, enabled, null);
    }

    public FeatureCard(Feature feature, Readable<Float> cardWidth, Runnable onStateChanged) {
        this(feature, cardWidth, Readable.of(feature.isEnabled()), onStateChanged);
    }

    public FeatureCard(Feature feature, float cardWidth, Runnable onStateChanged) {
        this(feature, Readable.of(cardWidth), Readable.of(feature.isEnabled()), onStateChanged);
    }

    @Override
    protected Element build() {
        var metadata = feature.getMetadata();

        var card = new Button(Styles.black8) {
            @Override
            public float getPrefHeight() {
                return 180f;
            }
        };

        card.name = "FeatureCard-" + (metadata != null ? metadata.getId() : "unknown");
        card.top().left();

        // Direct property bindings without manual Effect
        if (cardWidth != null) {
            Binding.bindWidth(card, cardWidth.map(w -> Math.max(0f, w - 10f)));
        }

        Binding.bindColor(card, enabled.map(value -> Boolean.TRUE.equals(value) ? Color.green : Color.scarlet));

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
                    Log.err("Failed to toggle feature " + (metadata != null ? metadata.getId() : ""), e);
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

                Drawable icon = metadata != null ? metadata.getIcon() : null;
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
                            .tooltip(Core.bundle != null ? Core.bundle.get("feature.button.open-dialog") : "")
                            .get();
                    attachChildClick(mainBtn, () -> Core.app.post(() -> feature.getMainDialog().show()));
                }

                // Settings button (if feature provides settings dialog)
                if (feature.getSettingDialog() != null) {
                    var settingBtn = header.button(Icon.settings, Styles.clearNonei, () -> {})
                            .size(32f)
                            .tooltip(Core.bundle != null ? Core.bundle.get("feature.button.settings") : "")
                            .get();
                    attachChildClick(settingBtn, () -> Core.app.post(() -> feature.getSettingDialog().show()));
                }

                // Help button
                var helpBtn = header.button(Icon.infoCircle, Styles.clearNonei, () -> {})
                        .size(32f)
                        .tooltip(Core.bundle != null ? Core.bundle.get("feature.button.help") : "")
                        .get();
                attachChildClick(helpBtn, () -> new FeatureHelpDialog(feature).show());
            }).growX().row();

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

            // Enabled / Disabled status label with direct reactive property binding
            Label statusLabel = new Label("");
            statusLabel.setStyle(Styles.defaultLabel);
            Binding.bindText(statusLabel, enabled.map(val -> {
                if (Core.bundle != null) {
                    return Boolean.TRUE.equals(val)
                            ? Core.bundle.get("feature.status.enabled")
                            : Core.bundle.get("feature.status.disabled");
                }
                return Boolean.TRUE.equals(val) ? "Enabled" : "Disabled";
            }));
            Binding.bindColor(statusLabel, enabled.map(val -> Boolean.TRUE.equals(val) ? Color.green : Color.scarlet));
            container.add(statusLabel).left();
        }).pad(4f).grow().top().left();

        return card;
    }

    public Readable<Boolean> enabled() {
        return enabled;
    }

    public Readable<Float> cardWidth() {
        return cardWidth;
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
