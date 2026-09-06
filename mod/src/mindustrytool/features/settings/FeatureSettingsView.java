package mindustrytool.features.settings;

import arc.Core;
import arc.graphics.Color;
import arc.scene.Element;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import mindustry.gen.Icon;
import mindustry.ui.Styles;
import mindustrytool.features.Feature;
import mindustrytool.features.FeatureManager;
import mindustrytool.features.FeatureStateChanged;
import solim.core.BaseComponent;
import solim.input.SolimTextField;
import solim.layout.ReactiveGrid;
import solim.layout.Row;
import solim.signal.Computed;
import solim.signal.Signal;

import static solim.ui.Ui.reactiveGrid;

/**
 * Declarative Solim component representing the complete feature settings view.
 * Encapsulates all signals, computeds, toolbar controls, and the reactive card grid.
 */
public final class FeatureSettingsView extends BaseComponent {

    private final Signal<String> filter = Signal.of("");
    private final Signal<Integer> revision = Signal.of(0);
    private final Signal<Float> contentWidth = Signal.of(calcInitialWidth());

    private final Computed<Integer> columnCount = new Computed<>(() -> {
        float width = contentWidth.get();
        return Math.max(1, (int) (width / 340f));
    });

    private final Computed<Float> cardWidth = new Computed<>(() -> {
        return contentWidth.get() / columnCount.get();
    });

    private final Computed<Seq<Feature>> filteredFeatures = new Computed<>(() -> {
        revision.get(); // dynamic reactive dependency
        String query = filter.get().trim().toLowerCase();
        Seq<Feature> all = FeatureManager.getFeatures();
        if (query.isEmpty()) {
            return all;
        }
        return all.select(f -> matchesFilter(f, query));
    });

    public FeatureSettingsView() {
        // Lifecycle-safe listener for global feature state changes
        listen(FeatureStateChanged.class, event -> refresh());
    }

    public void onShown() {
        updateWidth(calcInitialWidth());
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
        Table root = new Table();
        root.top().left();

        // Toolbar row
        root.add(buildToolbar()).growX().pad(6f).row();

        // Scrollable reactive grid pane
        Table scrollTable = new Table();
        scrollTable.top().left();

        ReactiveGrid<Feature, String> grid = reactiveGrid(
                columnCount,
                filteredFeatures,
                f -> f.getMetadata() != null ? f.getMetadata().getId() : f.getName(),
                feature -> new FeatureCard(feature, cardWidth, this::refresh)
        ).emptyView(() -> new BaseComponent() {
            @Override
            protected Element build() {
                String emptyText = Core.bundle != null ? Core.bundle.get("feature.search.empty") : "No features found";
                Label label = new Label(emptyText);
                label.setStyle(Styles.defaultLabel);
                label.setColor(Color.gray);
                return label;
            }
        });

        ownChild(grid);

        scrollTable.add(grid.element()).growX().top().left();
        root.pane(scrollTable).scrollX(false).scrollY(true).grow();

        return root;
    }

    private Table buildToolbar() {
        Row bar = new Row();
        bar.padding(10f);
        Table barTable = bar.table();
        barTable.left();

        if (Icon.zoom != null) {
            barTable.image(Icon.zoom).padRight(8f);
        }

        // Two-way bound search field
        SolimTextField searchField = own(SolimTextField.of(filter));
        if (Core.bundle != null) {
            searchField.field().setMessageText(Core.bundle.get("feature.search.placeholder"));
        }
        barTable.add(searchField.field()).growX();

        // Re-enable button
        String reenableText = Core.bundle != null ? Core.bundle.get("feature.button.re-enable") : "Re-enable";
        String reenableTooltip = Core.bundle != null ? Core.bundle.get("feature.button.re-enable.tooltip") : "";
        barTable.button(reenableText, Icon.refresh, () -> {
            FeatureManager.reenable();
            refresh();
        }).padLeft(12f).height(46f).tooltip(reenableTooltip);

        return barTable;
    }

    private static float calcInitialWidth() {
        if (Core.graphics == null) {
            return 800f;
        }
        return Core.graphics.getWidth() / Scl.scl() * 0.9f - 40f;
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
