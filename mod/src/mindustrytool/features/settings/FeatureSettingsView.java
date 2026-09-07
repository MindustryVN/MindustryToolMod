package mindustrytool.features.settings;

import arc.Core;
import arc.graphics.Color;
import arc.scene.Element;
import arc.scene.ui.layout.Scl;
import arc.struct.Seq;
import mindustry.gen.Icon;
import mindustrytool.features.Feature;
import mindustrytool.features.FeatureManager;
import solim.core.BaseComponent;
import solim.input.SolimTextField;
import solim.signal.Computed;
import solim.signal.Signal;

import static solim.ui.Ui.*;

public final class FeatureSettingsView extends BaseComponent {
    private final Signal<String> filter = Signal.of("");
    private final Signal<Float> contentWidth = Signal.of(calcContentWidth());
    private final Computed<Integer> columnCount = new Computed<>(() -> Math.max(1, (int) (contentWidth.get() / 340f)));
    private final Computed<Float> cardWidth = new Computed<>(() -> contentWidth.get() / columnCount.get());
    private final Computed<Seq<Feature>> filteredFeatures = new Computed<>(
            () -> FeatureManager.getFeatures().select(f -> matchesFilter(f, filter.get().trim().toLowerCase())));

    public float calcContentWidth() {
        return Core.graphics == null ? 800f : Core.graphics.getWidth() / Scl.scl() * 0.9f - 40f;
    }

    public void updateWidth(float width) {
        contentWidth.set(width);
    }

    @Override
    protected Element build() {
        return column(() -> {
            toolbar();
            scroll(() -> {
                grid(
                        columnCount,
                        filteredFeatures,
                        FeatureSettingsView::featureKey,
                        feature -> new FeatureCard(feature, cardWidth, feature.enabled())//
                )
                        .empty(() -> text(Core.bundle.get("feature.search.empty", "No features found"))
                                .color(Color.gray)
                                .padding(40f));
            }).grow();
        }).grow().element();
    }

    private void toolbar() {
        row(() -> {
            icon(Icon.zoom);
            SolimTextField searchField = textField(filter);
            searchField.placeholder(Core.bundle.get("feature.search.placeholder"));
            button(Core.bundle.get("feature.button.re-enable"), Icon.refresh, FeatureManager::reenable)
                    .tooltip(Core.bundle.get("feature.button.re-enable.tooltip"));
        }).padding(10f);
    }

    static String featureKey(Feature feature) {
        return feature.getMetadata() != null ? feature.getMetadata().getId() : feature.getName();
    }

    static boolean matchesFilter(Feature feature, String query) {
        if (query == null || query.trim().isEmpty()) {
            return true;
        }

        String q = query.trim().toLowerCase();
        return (feature.getName() != null && feature.getName().toLowerCase().contains(q))
                || (feature.getDescription() != null && feature.getDescription().toLowerCase().contains(q))
                || (feature.getMetadata() != null && feature.getMetadata().getId() != null
                        && feature.getMetadata().getId().toLowerCase().contains(q));
    }
}
