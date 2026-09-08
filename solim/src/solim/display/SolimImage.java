package solim.display;

import arc.graphics.Color;
import arc.scene.Element;
import arc.scene.style.Drawable;
import arc.scene.ui.Image;
import arc.util.Nullable;
import arc.util.Scaling;
import java.util.function.Consumer;
import solim.core.Component;
import solim.core.ComponentContext;
import solim.core.Disposable;
import solim.modifier.ElementModifiers;
import solim.signal.Effect;
import solim.signal.Readable;
import solim.signal.Signal;

/** Display widget for drawable content. */
public final class SolimImage implements Component, Disposable {

	public static class SizedImage extends Image {
		private float customPrefWidth = -1f;
		private float customPrefHeight = -1f;

		public SizedImage() {
			super();
		}

		public SizedImage(Drawable drawable) {
			super(drawable);
		}

		public SizedImage(Drawable drawable, Scaling scaling) {
			super(drawable, scaling);
		}

		public SizedImage size(float size) {
			return size(size, size);
		}

		public SizedImage size(float width, float height) {
			this.customPrefWidth = width;
			this.customPrefHeight = height;
			setSize(width, height);
			invalidateHierarchy();
			return this;
		}

		public SizedImage scaling(Scaling scaling) {
			setScaling(scaling);
			return this;
		}

		public SizedImage color(Color color) {
			if (color != null) {
				setColor(color);
			}
			return this;
		}

		public SizedImage color(Readable<Color> color) {
			if (color != null) {
				Effect e = Effect.of(() -> {
					Color c = color.get();
					if (c != null) {
						setColor(c);
					}
				});
				ComponentContext.register(e);
			}
			return this;
		}

		public SizedImage height(float height) {
			this.customPrefHeight = height;
			setHeight(height);
			invalidateHierarchy();
			return this;
		}

		public SizedImage width(float width) {
			this.customPrefWidth = width;
			setWidth(width);
			invalidateHierarchy();
			return this;
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

	private final Image image = new Image();
	private @Nullable Effect binding;

	public SolimImage() {
		this.image.name = "solim-image-image";
	}

	public SolimImage(Drawable d) {
		this();
		image.setDrawable(d);
	}

	public static SolimImage of(Drawable d) {
		return new SolimImage(d);
	}

	public static SolimImage of(Signal<Drawable> s) {
		SolimImage img = new SolimImage();
		Effect e = Effect.of((Consumer<Effect.Cleanup>) cleanup -> {
			img.image.setDrawable(s.get());
		});
		img.binding = e;
		return img;
	}

	public SolimImage width(float width) {
		ElementModifiers.width(image, width);
		return this;
	}

	public SolimImage height(float height) {
		ElementModifiers.height(image, height);
		return this;
	}

	public SolimImage size(float width, float height) {
		ElementModifiers.size(image, width, height);
		return this;
	}

	public SolimImage size(float size) {
		ElementModifiers.size(image, size);
		return this;
	}

	public SolimImage x(float x) {
		ElementModifiers.x(image, x);
		return this;
	}

	public SolimImage y(float y) {
		ElementModifiers.y(image, y);
		return this;
	}

	public SolimImage position(float x, float y) {
		ElementModifiers.position(image, x, y);
		return this;
	}

	public SolimImage visible(boolean visible) {
		ElementModifiers.visible(image, visible);
		return this;
	}

	public SolimImage color(Color color) {
		if (color != null) {
			image.setColor(color);
		}
		return this;
	}

	public SolimImage color(Readable<Color> color) {
		if (color != null) {
			Effect e = Effect.of(() -> {
				Color c = color.get();
				if (c != null) {
					image.setColor(c);
				}
			});
			ComponentContext.register(e);
		}
		return this;
	}

	public Image image() {
		return image;
	}

	@Override
	public Element element() {
		return image;
	}

	@Override
	public SolimImage name(String name) {
		ElementModifiers.name(image, name);
		return this;
	}

	@Override
	public void dispose() {
		if (binding != null) binding.dispose();
	}
}
