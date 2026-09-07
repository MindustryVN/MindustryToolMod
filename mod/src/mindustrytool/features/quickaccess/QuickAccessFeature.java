package mindustrytool.features.quickaccess;

import arc.Core;
import arc.Events;
import arc.scene.ui.Dialog;
import arc.util.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustrytool.config.ConfigGroup;
import mindustrytool.config.ConfigValue;
import mindustrytool.features.Feature;
import mindustrytool.features.FeatureMetadata;
import mindustrytool.features.FeatureStateChanged;
import solim.signal.Signal;

public class QuickAccessFeature extends Feature {

	private final ConfigGroup config;
	private final ConfigValue<Float> opacityConfig;
	private final ConfigValue<Float> scaleConfig;
	private final ConfigValue<Integer> colsConfig;
	private final ConfigValue<Set<String>> hiddenFeaturesConfig;

	private final ConfigGroup portraitGroup;
	private final ConfigGroup landscapeGroup;

	private final Signal<Float> xSignal;
	private final Signal<Float> ySignal;

	private @Nullable QuickAccessHudView hudView;
	private @Nullable QuickAccessSettingsDialog settingsDialog;

	public QuickAccessFeature() {
		super(FeatureMetadata.builder()
				.id("quick-access")
				.icon(Icon.menu)
				.order(10)
				.enabledByDefault(true)
				.quickAccess(false)
				.build());

		config = ConfigGroup.of(getMetadata());

		opacityConfig = config.floatValue("opacity", 1f);
		scaleConfig = config.floatValue("scale", 1f);
		colsConfig = config.intValue("cols", 6);
		hiddenFeaturesConfig = config.setValue("hidden", String.class, Collections.emptySet());

		portraitGroup = config.group("portrait");
		landscapeGroup = config.group("landscape");

		xSignal = Signal.of(x());
		ySignal = Signal.of(y());

		opacityConfig.signal().subscribe(val -> rebuildHud());
		scaleConfig.signal().subscribe(val -> rebuildHud());
		colsConfig.signal().subscribe(val -> rebuildHud());
		hiddenFeaturesConfig.signal().subscribe(val -> rebuildHud());
	}

	private ConfigGroup currentOrientationGroup() {
		return Core.graphics.isPortrait() ? portraitGroup : landscapeGroup;
	}

	public float x() {
		Float val = currentOrientationGroup().floatValue("x", Core.graphics.getWidth() / 2f).get();
		return val != null ? val : Core.graphics.getWidth() / 2f;
	}

	public void x(float value) {
		currentOrientationGroup().floatValue("x", Core.graphics.getWidth() / 2f).set(value);
		xSignal.set(value);
	}

	public float y() {
		Float val = currentOrientationGroup().floatValue("y", Core.graphics.getHeight() / 2f).get();
		return val != null ? val : Core.graphics.getHeight() / 2f;
	}

	public void y(float value) {
		currentOrientationGroup().floatValue("y", Core.graphics.getHeight() / 2f).set(value);
		ySignal.set(value);
	}

	public float opacity() {
		Float val = opacityConfig.get();
		return val != null ? val : 1f;
	}

	public void opacity(float value) {
		opacityConfig.set(value);
	}

	public float scale() {
		Float val = scaleConfig.get();
		return val != null ? val : 1f;
	}

	public void scale(float value) {
		scaleConfig.set(value);
	}

	public int cols() {
		Integer val = colsConfig.get();
		return val != null ? val : 6;
	}

	public void cols(int value) {
		colsConfig.set(value);
	}

	public boolean isFeatureVisible(String id) {
		Set<String> hidden = hiddenFeaturesConfig.get();
		return hidden == null || !hidden.contains(id);
	}

	public void setFeatureVisible(String id, boolean visible) {
		Set<String> current = hiddenFeaturesConfig.get();
		Set<String> hidden = current != null ? new HashSet<>(current) : new HashSet<>();
		if (visible) {
			hidden.remove(id);
		} else {
			hidden.add(id);
		}
		hiddenFeaturesConfig.set(hidden);
	}

	public void resetPosition() {
		x(Core.graphics.getWidth() / 2f);
		y(Core.graphics.getHeight() / 2f);
	}

	public ConfigGroup getConfigGroup() {
		return config;
	}

	public ConfigValue<Float> getOpacityConfig() {
		return opacityConfig;
	}

	public ConfigValue<Float> getScaleConfig() {
		return scaleConfig;
	}

	public ConfigValue<Integer> getColsConfig() {
		return colsConfig;
	}

	public ConfigValue<Set<String>> getHiddenFeaturesConfig() {
		return hiddenFeaturesConfig;
	}

	public Signal<Float> getOpacitySignal() {
		return opacityConfig.signal();
	}

	public Signal<Float> getScaleSignal() {
		return scaleConfig.signal();
	}

	public Signal<Integer> getColsSignal() {
		return colsConfig.signal();
	}

	public Signal<Set<String>> getHiddenFeaturesSignal() {
		return hiddenFeaturesConfig.signal();
	}

	public Signal<Float> getXSignal() {
		return xSignal;
	}

	public Signal<Float> getYSignal() {
		return ySignal;
	}

	@Override
	public void onEnable() {
		if (Vars.ui.hudGroup != null) {
			if (hudView != null) {
				hudView.remove();
			}

			hudView = new QuickAccessHudView(this);
			hudView.name = "quick-access-hud";
			hudView.visible(() -> Vars.ui.hudfrag != null && Vars.ui.hudfrag.shown && Vars.state != null && Vars.state.isGame());

			Core.app.post(() -> {
				if (hudView != null && Vars.ui.hudGroup != null) {
					Vars.ui.hudGroup.addChild(hudView);
				}
			});

			Events.on(FeatureStateChanged.class, event -> rebuildHud());
		}
	}

	@Override
	public void onDisable() {
		if (hudView != null) {
			hudView.remove();
			hudView = null;
		}
	}

	public void rebuildHud() {
		if (hudView != null) {
			hudView.rebuild();
		}
	}

	@Override
	public @Nullable Dialog getSettingDialog() {
		if (settingsDialog == null) {
			settingsDialog = new QuickAccessSettingsDialog(this);
		}
		return settingsDialog;
	}
}
