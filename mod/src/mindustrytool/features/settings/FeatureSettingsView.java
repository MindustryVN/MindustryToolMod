package mindustrytool.features.settings;

import static solim.ui.Ui.*;

import arc.Core;
import arc.graphics.Color;
import arc.scene.Element;
import arc.struct.Seq;
import mindustry.gen.Icon;
import mindustry.ui.Styles;
import mindustrytool.features.Feature;
import mindustrytool.features.FeatureManager;
import solim.core.BaseComponent;
import solim.signal.Computed;
import solim.signal.Signal;

public final class FeatureSettingsView extends BaseComponent {
    private final Signal<String> filter = Signal.of("");
    private final Computed<Float> contentWidth = dvw(90f).map(w -> w - unit(10));
    private final Computed<Integer> columnCount = new Computed<>(() -> Math.max(1, (int) (contentWidth.get() / 340f)));
    private final Computed<Float> cardWidth = new Computed<>(() -> contentWidth.get() / columnCount.get());
    private final Computed<Seq<Feature>> filteredFeatures = new Computed<>(
            () -> FeatureManager.getFeatures().select(f -> matchesFilter(f, filter.get().trim().toLowerCase())));

    @Override
    protected Element build() {
        return column().grow().children(() -> {
            toolbar();
            scroll().grow().children(() -> {
                grid(columnCount, //
                        filteredFeatures, //
                        feature -> feature.getMetadata().getId(), //
                        feature -> new FeatureCard(feature, cardWidth)//
                )//
                        .empty(() -> empty())//
                        .gap(unit(2));
            });
        }).element();
    }

    private void empty() {
        text(Core.bundle.get("feature.search.empty", "No features found")).color(Color.gray).padding(unit(4));
    }

    private void toolbar() {
        row().growX().gap(unit(2)).children(() -> {
            icon(Icon.zoom);
            textField(filter).growX().placeholder(Core.bundle.get("feature.search.placeholder"));

            button(FeatureManager::reenable).style(Styles.defaultb).width(unit(50)).height(unit(10))
                    .tooltip(Core.bundle.get("feature.button.re-enable.tooltip")).gap(unit(2)).children(() -> {
                        image(Icon.refresh);
                        text(Core.bundle.get("feature.button.re-enable"));
                    });
        });
    }

    static boolean matchesFilter(Feature feature, String query) {
        if (query == null || query.trim().isEmpty()) {
            return true;
        }

        String q = query.trim().toLowerCase();

        return feature.getName().toLowerCase().contains(q);
    }
}
