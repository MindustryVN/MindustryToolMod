package mindustrytool.features.quickaccess;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.event.Touchable;
import arc.scene.ui.Button;
import arc.scene.ui.Dialog;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Scaling;
import arc.util.Time;
import mindustry.game.EventType;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustrytool.features.Feature;
import mindustrytool.features.FeatureManager;
import mindustrytool.features.FeatureMetadata;
import mindustrytool.features.settings.FeatureSettingDialog;

public class QuickAccessHudView extends Table {

	private final QuickAccessFeature parentFeature;

	public QuickAccessHudView(QuickAccessFeature parentFeature) {
		this.parentFeature = parentFeature;
		touchable = Touchable.childrenOnly;
		setPosition(parentFeature.x(), parentFeature.y());

		Events.on(EventType.ResizeEvent.class, event -> {
			rebuild();
			keepInScreen();
		});

		Core.app.post(this::rebuild);
	}

	public void rebuild() {
		clear();

		Table container = new Table();
		container.background(Styles.black6);
		container.setColor(1f, 1f, 1f, parentFeature.opacity());
		container.touchable = Touchable.enabled;

		float scale = parentFeature.scale();
		float buttonSize = 48f * scale;
		float margin = 8f * scale;

		container.button(Icon.move, Styles.clearNonei, () -> {})
				.size(buttonSize)
				.margin(margin)
				.get()
				.addListener(new InputListener() {
					float lastX;
					float lastY;

					@Override
					public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
						lastX = x;
						lastY = y;
						return true;
					}

					@Override
					public void touchDragged(InputEvent event, float x, float y, int pointer) {
						try {
							moveBy(x - lastX, y - lastY);

							float sw = Core.graphics.getWidth();
							float sh = Core.graphics.getHeight();

							QuickAccessHudView.this.x = Mathf.clamp(QuickAccessHudView.this.x, 0, sw - 40f);
							QuickAccessHudView.this.y = Mathf.clamp(QuickAccessHudView.this.y, 0, sh - 40f);

							parentFeature.x(QuickAccessHudView.this.x);
							parentFeature.y(QuickAccessHudView.this.y);
							keepInScreen();
						} catch (Exception e) {
							Log.err(e);
						}
					}
				});

		float sw = Core.graphics.getWidth();
		float sh = Core.graphics.getHeight();

		x = Mathf.clamp(x, 0, sw - 40f);
		y = Mathf.clamp(y, 0, sh - 40f);

		parentFeature.x(x);
		parentFeature.y(y);

		Image sep = new Image(Tex.whiteui);
		sep.setColor(Pal.accent);
		container.add(sep).width(2f).fillY();

		Table content = new Table();
		populateContent(content);
		container.add(content);

		add(container).pad(0).margin(0);
		pack();
		keepInScreen();
	}

	public void keepInScreen() {
		if (getScene() == null) {
			return;
		}

		float w = getWidth();
		float h = getHeight();
		float sw = getScene().getWidth();
		float sh = getScene().getHeight();

		if (x < 0) x = 0;
		if (y < 0) y = 0;
		if (x + w > sw) x = sw - w;
		if (y + h > sh) y = sh - h;
	}

	private void populateContent(Table table) {
		table.background(Styles.black6);

		Seq<Feature> features = FeatureManager.getFeatures();
		int count = 0;
		int cols = parentFeature.cols();
		float scale = parentFeature.scale();
		float buttonSize = 48f * scale;
		float margin = 8f * scale;

		for (Feature f : features) {
			if (f == parentFeature) {
				continue;
			}

			FeatureMetadata meta = f.getMetadata();

			if (!meta.isQuickAccess()) {
				continue;
			}

			if (!parentFeature.isFeatureVisible(meta.getId())) {
				continue;
			}

			Button[] btnRef = new Button[1];
			long[] pressTime = {-1};
			boolean[] longPressed = {false};

			btnRef[0] = table.button(
							b -> b.image(meta.getIcon())
									.scaling(Scaling.fit)
									.update(l -> l.setColor(f.isEnabled() ? Color.white : Pal.gray)),
							Styles.clearNonei,
							() -> {
								if (!longPressed[0]) {
									f.setEnabled(!f.isEnabled());
								}
							})
					.size(buttonSize)
					.margin(margin)
					.tooltip(f.getName())
					.get();

			btnRef[0].update(() -> {
				if (btnRef[0].isPressed()) {
					if (pressTime[0] == -1) {
						pressTime[0] = Time.millis();
						longPressed[0] = false;
					} else if (!longPressed[0] && Time.timeSinceMillis(pressTime[0]) >= 300) {
						longPressed[0] = true;
						Dialog settingDlg = f.getSettingDialog();
						if (settingDlg != null) {
							settingDlg.show();
						}
					}
				} else {
					pressTime[0] = -1;
				}
			});

			if (++count % cols == 0) {
				table.row();
			}
		}

		table.button(
						b -> b.image(Icon.settings).scaling(Scaling.fit),
						Styles.clearNonei,
						() -> new FeatureSettingDialog().show())
				.size(buttonSize)
				.margin(margin);
	}
}
