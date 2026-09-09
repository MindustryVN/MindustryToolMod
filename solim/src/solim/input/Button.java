package solim.input;

import arc.scene.Element;
import arc.scene.event.ClickListener;
import arc.scene.event.InputEvent;
import arc.scene.ui.Button.ButtonStyle;
import arc.scene.ui.Label;
import arc.scene.ui.Tooltip;
import arc.util.Log;
import arc.util.Nullable;
import java.util.ArrayList;
import java.util.List;
import solim.core.Component;
import solim.core.ComponentContext;
import solim.core.Disposable;
import solim.layout.ConstrainedElement;
import solim.layout.Row;
import solim.layout.SizeConstraints;
import solim.modifier.ElementModifiers;
import solim.overlay.Hud;
import solim.signal.Computed;
import solim.signal.Effect;
import solim.signal.Readable;
import solim.signal.Signal;
import solim.style.Style;
import solim.style.StyleBinding;
import solim.ui.ParentStack;

/**
 * Pure Button container widget supporting explicit children composition, custom width/height sizing, and reactive state.
 */
public final class Button implements Component {

	public static class SizedButton extends arc.scene.ui.Button implements ConstrainedElement {
		private final SizeConstraints constraints = new SizeConstraints();
		private float customPrefWidth = -1f;
		private float customPrefHeight = -1f;

		public SizedButton() {
			this(null);
		}

		public SizedButton(@Nullable ButtonStyle style) {
			super(style != null ? style : new ButtonStyle());
		}

		@Override
		public SizeConstraints getSizeConstraints() {
			return constraints;
		}

		public SizedButton growX() {
			constraints.growX = true;
			constraints.applyGrowToParentCell(this);
			return this;
		}

		public SizedButton growY() {
			constraints.growY = true;
			constraints.applyGrowToParentCell(this);
			return this;
		}

		public SizedButton grow() {
			return growX().growY();
		}

		public void setCustomPrefWidth(float width) {
			this.customPrefWidth = width;
			invalidateHierarchy();
		}

		public void setCustomPrefHeight(float height) {
			this.customPrefHeight = height;
			invalidateHierarchy();
		}

		@Override
		public float getPrefWidth() {
			return customPrefWidth >= 0 ? customPrefWidth : super.getPrefWidth();
		}

		@Override
		public float getPrefHeight() {
			return customPrefHeight >= 0 ? customPrefHeight : super.getPrefHeight();
		}
	}

	private final SizedButton sizedButton;
	private final List<Disposable> bindings = new ArrayList<>();
	private boolean stopClickPropagation = true;
	private @Nullable Runnable onClick;
	private @Nullable Runnable onLongClick;
	private long longClickDuration = 300L;
	private boolean longPressed = false;
	private long pressTime = -1L;
	private boolean hasClickListener = false;
	private boolean hasLongClickListener = false;

	public Button() {
		this(new SizedButton());
	}

	public Button(@Nullable ButtonStyle style) {
		this(new SizedButton(style));
	}

	public Button(@Nullable Runnable onClick) {
		this(new SizedButton());
		onClick(onClick);
	}

	public Button(@Nullable ButtonStyle style, @Nullable Runnable onClick) {
		this(new SizedButton(style));
		onClick(onClick);
	}

	public Button(SizedButton sizedButton) {
		this.sizedButton = sizedButton;
		this.sizedButton.name = "solim-button-sizedButton";
		this.sizedButton.center();
	}

	public Button children(@Nullable Runnable r) {
		ParentStack.push(sizedButton, Row.ATTACHER);
		try {
			if (r != null) {
				r.run();
			}
		} finally {
			ParentStack.pop();
		}
		return this;
	}

	public Button onClick(@Nullable Runnable action) {
		this.onClick = action;
		if (action != null) {
			ensureClickListener();
		}
		return this;
	}

	public Button onLongClick(@Nullable Runnable action) {
		return onLongClick(300L, action);
	}

	public Button onLongClick(long durationMs, @Nullable Runnable action) {
		this.onLongClick = action;
		this.longClickDuration = durationMs;
		if (action != null) {
			ensureClickListener();
			ensureLongClickListener();
		}
		return this;
	}

	private void ensureClickListener() {
		if (hasClickListener) return;
		hasClickListener = true;
		sizedButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (stopClickPropagation && event != null) {
					event.stop();
				}
				if (longPressed) {
					longPressed = false;
					return;
				}
				if (Button.this.onClick != null) {
					try {
						Button.this.onClick.run();
					} catch (Exception e) {
						Log.err("Error executing button onClick", e);
					}
				}
			}
		});
	}

	private void ensureLongClickListener() {
		if (hasLongClickListener) return;
		hasLongClickListener = true;
		sizedButton.update(() -> {
			if (sizedButton.isPressed()) {
				if (pressTime == -1L) {
					pressTime = arc.util.Time.millis();
					longPressed = false;
				} else if (!longPressed && arc.util.Time.timeSinceMillis(pressTime) >= longClickDuration) {
					longPressed = true;
					if (Button.this.onLongClick != null) {
						try {
							Button.this.onLongClick.run();
						} catch (Exception e) {
							Log.err("Error executing button onLongClick", e);
						}
					}
				}
			} else {
				pressTime = -1L;
			}
		});
	}

	public Button stopClickPropagation(boolean stop) {
		this.stopClickPropagation = stop;
		return this;
	}

	public Button tooltip(@Nullable String tip) {
		if (tip != null && !tip.isEmpty()) {
			try {
				sizedButton.addListener(new Tooltip(t -> t.add(tip)));
			} catch (Throwable ignored) {
			}
		}
		return this;
	}

	public Button tooltip(@Nullable Readable<String> tip) {
		if (tip != null) {
			try {
				sizedButton.addListener(new Tooltip(t -> {
					Label label = new Label("");
					Effect e = Effect.of(() -> label.setText(tip.get() != null ? tip.get() : ""));
					bindings.add(e);
					ComponentContext.register(e);
					t.add(label);
				}));
			} catch (Throwable ignored) {
			}
		}
		return this;
	}

	public Button enabled(@Nullable Readable<Boolean> signal) {
		if (signal != null) {
			Effect e = Effect.of(() -> sizedButton.setDisabled(!Boolean.TRUE.equals(signal.get())));
			bindings.add(e);
			ComponentContext.register(e);
		}
		return this;
	}

	public Button checked(@Nullable Readable<Boolean> signal) {
		if (signal != null) {
			Effect e = Effect.of(() -> sizedButton.setChecked(Boolean.TRUE.equals(signal.get())));
			bindings.add(e);
			ComponentContext.register(e);
		}
		return this;
	}

	public Button visible(@Nullable Readable<Boolean> signal) {
		if (signal != null) {
			Effect e = Effect.of(() -> sizedButton.visible = Boolean.TRUE.equals(signal.get()));
			bindings.add(e);
			ComponentContext.register(e);
		}
		return this;
	}

	public Button style(@Nullable Style style) {
		if (style != null) {
			StyleBinding.apply(sizedButton, style, (sb, s) -> {});
		}
		return this;
	}

	public Button style(@Nullable ButtonStyle style) {
		if (style != null) {
			sizedButton.setStyle(style);
		}
		return this;
	}

	public Button style(@Nullable Signal<Style> s) {
		if (s != null) {
			Effect e = StyleBinding.bind(s, sizedButton, (sb, st) -> {});
			bindings.add(e);
			ComponentContext.register(e);
		}
		return this;
	}

	public Button style(@Nullable Computed<Style> s) {
		if (s != null) {
			Effect e = StyleBinding.bind(s, sizedButton, (sb, st) -> {});
			bindings.add(e);
			ComponentContext.register(e);
		}
		return this;
	}

	public Button width(float width) {
		ElementModifiers.width(sizedButton, width);
		return this;
	}

	public Button width(@Nullable Readable<Float> width) {
		if (width != null) {
			Effect e = Effect.of(() -> {
				Float w = width.get();
				if (w != null) {
					width(w);
				}
			});
			bindings.add(e);
			ComponentContext.register(e);
		}
		return this;
	}

	public Button height(float height) {
		ElementModifiers.height(sizedButton, height);
		return this;
	}

	public Button height(@Nullable Readable<Float> height) {
		if (height != null) {
			Effect e = Effect.of(() -> {
				Float h = height.get();
				if (h != null) {
					height(h);
				}
			});
			bindings.add(e);
			ComponentContext.register(e);
		}
		return this;
	}

	public Button size(float width, float height) {
		ElementModifiers.size(sizedButton, width, height);
		return this;
	}

	public Button size(float size) {
		ElementModifiers.size(sizedButton, size);
		return this;
	}

	public Button size(@Nullable Readable<Float> size) {
		if (size != null) {
			width(size);
			height(size);
		}
		return this;
	}

	public Button size(@Nullable Readable<Float> width, @Nullable Readable<Float> height) {
		if (width != null) width(width);
		if (height != null) height(height);
		return this;
	}

	public Button growX() {
		sizedButton.growX();
		return this;
	}

	public Button growY() {
		sizedButton.growY();
		return this;
	}

	public Button grow() {
		return growX().growY();
	}

	public Button gap(float g) {
		ElementModifiers.gap(sizedButton, g);
		return this;
	}

	public Button margin(float m) {
		sizedButton.margin(m);
		return this;
	}

	public Button margin(@Nullable Readable<Float> margin) {
		if (margin != null) {
			Effect e = Effect.of(() -> {
				Float m = margin.get();
				if (m != null) {
					margin(m);
				}
			});
			bindings.add(e);
			ComponentContext.register(e);
		}
		return this;
	}

	public Button margin(float top, float left, float bottom, float right) {
		sizedButton.margin(top, left, bottom, right);
		return this;
	}

	public Button x(float x) {
		ElementModifiers.x(sizedButton, x);
		return this;
	}

	public Button y(float y) {
		ElementModifiers.y(sizedButton, y);
		return this;
	}

	public Button position(float x, float y) {
		ElementModifiers.position(sizedButton, x, y);
		return this;
	}

	public Button left() {
		sizedButton.left();
		sizedButton.defaults().left();
		return this;
	}

	public Button right() {
		sizedButton.right();
		sizedButton.defaults().right();
		return this;
	}

	public Button center() {
		sizedButton.center();
		sizedButton.defaults().center();
		return this;
	}

	public Button top() {
		sizedButton.top();
		sizedButton.defaults().top();
		return this;
	}

	public Button bottom() {
		sizedButton.bottom();
		sizedButton.defaults().bottom();
		return this;
	}

	public SizedButton sizedButton() {
		return sizedButton;
	}

	public Button name(String name) {
		ElementModifiers.name(sizedButton, name);
		return this;
	}

	public Button draggable() {
		return draggable((Hud) null);
	}

	public Button draggable(@Nullable Signal<Float> xSignal, @Nullable Signal<Float> ySignal) {
		return draggable((Hud) null, xSignal, ySignal);
	}

	public Button draggable(@Nullable Hud hud) {
		ElementModifiers.draggable(sizedButton, hud);
		return this;
	}

	public Button draggable(@Nullable Hud hud, @Nullable Signal<Float> xSignal, @Nullable Signal<Float> ySignal) {
		ElementModifiers.draggable(sizedButton, hud, xSignal, ySignal);
		return this;
	}

	@Override
	public Element element() {
		return sizedButton;
	}

	@Override
	public void dispose() {
		for (Disposable d : bindings) {
			d.dispose();
		}
		bindings.clear();
	}
}
