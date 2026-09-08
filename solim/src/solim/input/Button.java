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
import solim.layout.Row;
import solim.modifier.ElementModifiers;
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
public final class Button implements Component, Disposable {

	public static class SizedButton extends arc.scene.ui.Button {
		private float customPrefWidth = -1f;
		private float customPrefHeight = -1f;

		public SizedButton() {
			this(null);
		}

		public SizedButton(@Nullable ButtonStyle style) {
			super(style != null ? style : new ButtonStyle());
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
	private boolean hasClickListener = false;

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
		if (action != null && !hasClickListener) {
			hasClickListener = true;
			sizedButton.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					if (stopClickPropagation && event != null) {
						event.stop();
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
		return this;
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

	public Button gap(float g) {
		ElementModifiers.gap(sizedButton, g);
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

	public SizedButton sizedButton() {
		return sizedButton;
	}

	public Button name(String name) {
		ElementModifiers.name(sizedButton, name);
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
