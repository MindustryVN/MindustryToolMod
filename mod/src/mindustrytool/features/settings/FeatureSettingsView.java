package mindustrytool.features.settings;

import static solim.ui.Ui.*;

import arc.Core;
import arc.graphics.Color;
import arc.scene.Element;
import arc.struct.Seq;
import mindustry.gen.Icon;
import mindustrytool.features.Feature;
import mindustrytool.features.FeatureManager;
import solim.core.BaseComponent;
import solim.input.SolimTextField;
import solim.signal.Computed;
import solim.signal.Signal;

public final class FeatureSettingsView extends BaseComponent {
	private final Signal<String> filter = Signal.of("");
	private final Computed<Float> contentWidth = dvw(90f).map(w -> w - 40f);
	private final Computed<Integer> columnCount = new Computed<>(() -> Math.max(1, (int) (contentWidth.get() / 340f)));
	private final Computed<Float> cardWidth = new Computed<>(() -> contentWidth.get() / columnCount.get());
	private final Computed<Seq<Feature>> filteredFeatures = new Computed<>(() -> FeatureManager.getFeatures()
			.select(f -> matchesFilter(f, filter.get().trim().toLowerCase())));

	@Override
	protected Element build() {
		return column().grow()
				.children(() -> {
					toolbar();
					scroll().grow().children(() -> {
						grid(
										columnCount,
										filteredFeatures,
										feature -> feature.getMetadata().getId(),
										feature -> new FeatureCard(feature, cardWidth))
								.empty(() -> text(Core.bundle.get("feature.search.empty", "No features found"))
										.color(Color.gray)
										.padding(40f));
					});
				})
				.element();
	}

	private void toolbar() {
		row().padding(10f).children(() -> {
			icon(Icon.zoom);
			SolimTextField searchField = textField(filter);
			searchField.placeholder(Core.bundle.get("feature.search.placeholder"));
			button(Core.bundle.get("feature.button.re-enable"), Icon.refresh, FeatureManager::reenable)
					.tooltip(Core.bundle.get("feature.button.re-enable.tooltip"));
		});
	}

	static boolean matchesFilter(Feature feature, String query) {
		if (query == null || query.trim().isEmpty()) {
			return true;
		}

		String q = query.trim().toLowerCase();
		return (feature.getName() != null && feature.getName().toLowerCase().contains(q))
				|| (feature.getDescription() != null
						&& feature.getDescription().toLowerCase().contains(q))
				|| (feature.getMetadata() != null
						&& feature.getMetadata().getId() != null
						&& feature.getMetadata().getId().toLowerCase().contains(q));
	}
}
