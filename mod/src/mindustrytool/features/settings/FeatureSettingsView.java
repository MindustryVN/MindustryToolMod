package mindustrytool.features.settings;

import arc.graphics.Color;
import arc.scene.Element;
import arc.struct.Seq;
import mindustrytool.features.Feature;
import mindustrytool.features.FeatureManager;
import mindustrytool.features.FeatureStateChanged;
import solim.core.BaseComponent;
import solim.input.SolimTextField;
import solim.signal.Computed;
import solim.signal.Signal;

import java.util.HashMap;
import java.util.Map;

import static mindustrytool.features.settings.FeatureSettingDialog.bundle;
import static mindustrytool.features.settings.FeatureSettingDialog.calcContentWidth;
import static solim.ui.Ui.*;

/**
 * Declarative Solim component representing the complete feature settings view.
 * Encapsulates all signals, computeds, toolbar controls, and the reactive card grid.
 */
public final class FeatureSettingsView extends BaseComponent {

    private final Signal<String> filter = Signal.of("");
    private final Signal<Integer> revision = Signal.of(0);
    private final Signal<Float> contentWidth = Signal.of(calcContentWidth());
    private final Map<Feature, Signal<Boolean>> featureStates = new HashMap<>();

    private final Computed<Integer> columnCount = new Computed<>(() -> {
        float width = contentWidth.get();
        return Math.max(1, (int) (width / 340f));
    });

    private final Computed<Float> cardWidth = new Computed<>(() -> {
        return contentWidth.get() / columnCount.get();
    });

    private final Computed<Seq<Feature>> filteredFeatures = new Computed<>(() -> {
        revision.get(); // dynamic reactive dependency for structural changes
        String query = filter.get().trim().toLowerCase();
        Seq<Feature> all = FeatureManager.getFeatures();
        if (query.isEmpty()) {
            return all;
        }
        return all.select(f -> matchesFilter(f, query));
    });

    public FeatureSettingsView() {
        // Lifecycle-safe listener for global feature state changes
        listen(FeatureStateChanged.class, event -> {
            if (event != null && event.getFeature() != null) {
                updateFeatureState(event.getFeature());
            } else {
                syncAllFeatureStates();
            }
        });
    }

    public Signal<Boolean> getFeatureEnabledSignal(Feature feature) {
        Signal<Boolean> signal = featureStates.get(feature);
        if (signal == null) {
            signal = Signal.of(feature.isEnabled());
            featureStates.put(feature, signal);
        }
        return signal;
    }

    public void updateFeatureState(Feature feature) {
        Signal<Boolean> signal = featureStates.get(feature);
        if (signal != null) {
            signal.set(feature.isEnabled());
        }
    }

    public void syncAllFeatureStates() {
        for (Map.Entry<Feature, Signal<Boolean>> entry : featureStates.entrySet()) {
            entry.getValue().set(entry.getKey().isEnabled());
        }
    }

    public void onShown() {
        updateWidth(calcContentWidth());
        syncAllFeatureStates();
        refresh();
    }

    public void updateWidth(float width) {
        contentWidth.set(width);
    }

    public void refresh() {
        revision.update(val -> val + 1);
    }

    @Override
    protected Element build() {
        return column(() -> {
            toolbar();

            scroll(() -> {
                reactiveGrid(
                        columnCount,
                        filteredFeatures,
                        FeatureSettingsView::featureKey,
                        feature -> new FeatureCard(
                                feature,
                                cardWidth,
                                getFeatureEnabledSignal(feature),
                                () -> updateFeatureState(feature)
                        )
                ).empty(() -> {
                    text(bundle("feature.search.empty", "No features found"))
                            .color(Color.gray)
                            .padding(40f);
                });
            }).grow();
        }).grow().element();
    }

    private void toolbar() {
        row(() -> {
            icon(FeatureSettingDialog.icon("zoom"));

            SolimTextField searchField = textField(filter);
            searchField.placeholder(bundle("feature.search.placeholder"));

            button(bundle("feature.button.re-enable"), FeatureSettingDialog.icon("refresh"), () -> {
                FeatureManager.reenable();
                syncAllFeatureStates();
                refresh();
            }).tooltip(bundle("feature.button.re-enable.tooltip"));
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

    public Signal<String> filter() {
        return filter;
    }

    public Signal<Integer> revision() {
        return revision;
    }

    public Signal<Float> contentWidth() {
        return contentWidth;
    }

    public Computed<Integer> columnCount() {
        return columnCount;
    }

    public Computed<Float> cardWidth() {
        return cardWidth;
    }

    public Computed<Seq<Feature>> filteredFeatures() {
        return filteredFeatures;
    }
}
