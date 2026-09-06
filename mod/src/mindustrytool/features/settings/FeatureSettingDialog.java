package mindustrytool.features.settings;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustrytool.Config;
import mindustrytool.features.Feature;
import mindustrytool.features.FeatureManager;
import mindustrytool.features.FeatureStateChanged;

/**
 * Dialog for managing mod features, toggling their states, and accessing their
 * individual settings and documentation.
 */
public class FeatureSettingDialog extends BaseDialog {

    private String filter = "";
    private Table paneTable;

    public FeatureSettingDialog() {
        super(Core.bundle.get("feature.dialog.title"));

        addCloseButton();
        closeOnBack();

        buttons.button(Core.bundle.get("feature.button.report-bug"), Icon.infoCircle, () -> {
            if (!Core.app.openURI(Config.DISCORD_INVITE_URL)) {
                Core.app.setClipboardText(Config.DISCORD_INVITE_URL);
                Vars.ui.showInfoFade(Core.bundle.get("feature.toast.copied"));
            }
        }).size(180f, 50f);

        shown(this::rebuild);
        resized(this::rebuild);

        Events.on(FeatureStateChanged.class, event -> {
            if (isShown()) {
                rebuildPane();
            }
        });
    }

    private void rebuild() {
        cont.clear();
        cont.top();

        // Search & action bar
        cont.table(bar -> {
            bar.left().margin(10f);

            // Search input
            bar.image(Icon.zoom).padRight(8f);
            var searchField = bar.field(filter, text -> {
                filter = text.trim();
                rebuildPane();
            }).growX().get();
            searchField.setMessageText(Core.bundle.get("feature.search.placeholder"));

            // Re-enable button
            bar.button(Core.bundle.get("feature.button.re-enable"), Icon.refresh, () -> {
                FeatureManager.reenable();
                rebuildPane();
            }).padLeft(12f).height(46f).tooltip(Core.bundle.get("feature.button.re-enable.tooltip"));
        }).growX().pad(6f).row();

        // Scrollable feature cards pane
        cont.pane(table -> {
            this.paneTable = table;
            rebuildPane();
        }).scrollX(false).scrollY(true).grow();
    }

    private void rebuildPane() {
        if (paneTable == null) {
            return;
        }

        paneTable.clear();
        paneTable.top().left();

        float screenWidth = Core.graphics.getWidth() / Scl.scl() * 0.9f - 40f;
        int cols = Math.max(1, (int) (screenWidth / 340f));
        float cardWidth = screenWidth / cols;

        Seq<Feature> matchingFeatures = FeatureManager.getFeatures().select(this::matchesFilter);

        if (matchingFeatures.isEmpty()) {
            paneTable.add(Core.bundle.get("feature.search.empty"))
                    .style(Styles.defaultLabel)
                    .color(Color.gray)
                    .pad(40f)
                    .center();
            return;
        }

        int col = 0;
        for (Feature feature : matchingFeatures) {
            FeatureCard.build(paneTable, feature, cardWidth, this::rebuildPane);

            if (++col % cols == 0) {
                paneTable.row();
            }
        }

        if (col % cols != 0) {
            paneTable.row();
        }
    }

    private boolean matchesFilter(Feature feature) {
        if (filter.isEmpty()) {
            return true;
        }

        String query = filter.toLowerCase();
        return feature.getName().toLowerCase().contains(query)
                || feature.getDescription().toLowerCase().contains(query)
                || feature.getMetadata().getId().toLowerCase().contains(query);
    }
}
