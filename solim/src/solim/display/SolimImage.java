package solim.display;

import arc.scene.style.Drawable;
import arc.scene.ui.Image;
import arc.util.Scaling;
import java.util.function.Consumer;
import solim.signal.Effect;
import solim.signal.Signal;

/** Display widget for drawable content. */
public final class SolimImage {

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
	private Effect binding;

	public SolimImage() {}

	public SolimImage(Drawable d) {
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

	public Image image() {
		return image;
	}

	public void dispose() {
		if (binding != null) binding.dispose();
	}
}
