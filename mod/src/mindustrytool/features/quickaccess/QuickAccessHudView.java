package mindustrytool.features.quickaccess;

import static solim.ui.Ui.*;

import arc.graphics.Color;
import arc.scene.Element;
import arc.scene.ui.Dialog;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.Scaling;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustrytool.features.Feature;
import mindustrytool.features.FeatureManager;
import mindustrytool.features.FeatureMetadata;
import mindustrytool.features.settings.FeatureSettingDialog;
import solim.core.BaseComponent;
import solim.core.Component;
import solim.overlay.Hud;
import solim.signal.Readable;

/**
 * Fully reactive and declarative QuickAccess HUD overlay.
 * Uses reactive bindings for opacity, scale, position, column reflow, and feature state.
 */
public class QuickAccessHudView extends BaseComponent {

	private static class HudItem {
		final String id;
		final @Nullable Feature feature;

		HudItem(String id, @Nullable Feature feature) {
			this.id = id;
			this.feature = feature;
		}

		String id() {
			return id;
		}
	}

	private final QuickAccessFeature parentFeature;
	private @Nullable Hud hud;

	public QuickAccessHudView(QuickAccessFeature parentFeature) {
		this.parentFeature = parentFeature;
	}

	@Override
	protected Element build() {
		Readable<Float> scale = parentFeature.scaleConfig.signal();
		Readable<Float> buttonSize = scale.map(s -> unit(10) * s);
		Readable<Float> margin = scale.map(s -> unit(2) * s);

		Readable<List<HudItem>> items = parentFeature.hiddenFeaturesConfig.signal().map(this::computeVisibleItems);

		hud = hud(() -> {
			button()
					.style(Styles.clearNonei)
					.size(buttonSize)
					.children(() -> image(Icon.move).scaling(Scaling.fit))
					.draggable(parentFeature.xSignal, parentFeature.ySignal);

			image(Tex.whiteui)
					.color(Pal.accent)
					.width(2f)
                    .marginRight(2)
					.growY();

			grid(parentFeature.colsConfig.signal(), items, HudItem::id, item -> createItemButton(item, buttonSize, margin))
					.gap(0f);
		});

		hud.background(Styles.black6);
		hud.opacity(parentFeature.opacityConfig.signal());
		hud.position(parentFeature.xSignal, parentFeature.ySignal);

		return hud.element();
	}

	private List<HudItem> computeVisibleItems(@Nullable Set<String> hidden) {
		List<HudItem> list = new ArrayList<>();
		Seq<Feature> features = FeatureManager.getFeatures();
		for (Feature f : features) {
			if (f == parentFeature) continue;

			FeatureMetadata meta = f.getMetadata();
			if (!meta.isQuickAccess()) continue;
			if (hidden != null && hidden.contains(meta.getId())) continue;

			list.add(new HudItem(meta.getId(), f));
		}
		list.add(new HudItem("__settings__", null));
		return list;
	}

	private Component createItemButton(HudItem item, Readable<Float> buttonSize, Readable<Float> margin) {
		if (item.feature != null) {
			Feature f = item.feature;
			FeatureMetadata meta = f.getMetadata();

			return button()
					.style(Styles.clearNonei)
					.size(buttonSize)
					.tooltip(f.getName())
					.onClick(() -> f.setEnabled(!f.isEnabled()))
					.onLongClick(300L, () -> {
						Dialog settingDlg = f.getSettingDialog();
						if (settingDlg != null) {
							settingDlg.show();
						}
					})
					.children(() -> image(meta.getIcon())
							.scaling(Scaling.fit)
							.color(f.enabled().map(en -> en ? Color.white : Pal.gray)));
		} else {
			return button()
					.style(Styles.clearNonei)
					.size(buttonSize)
					.onClick(() -> new FeatureSettingDialog().show())
					.children(() -> image(Icon.settings).scaling(Scaling.fit));
		}
	}

	public @Nullable Hud getHud() {
		return hud;
	}

	public void keepInScreen() {
		if (hud != null) {
			hud.keepInScreen();
		}
	}
}
