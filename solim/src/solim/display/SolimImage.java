package solim.display;

import arc.graphics.Color;
import arc.scene.Element;
import arc.scene.style.Drawable;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.util.Nullable;
import arc.util.Scaling;
import java.util.function.Consumer;
import solim.core.Component;
import solim.core.ComponentContext;
import solim.core.Disposable;
import solim.layout.ConstrainedElement;
import solim.layout.SizeConstraints;
import solim.modifier.ElementModifiers;
import solim.signal.Effect;
import solim.signal.Readable;
import solim.signal.Signal;

/** Display widget for drawable content. */
public final class SolimImage implements Component, Disposable {

	public static class SizedImage extends Image implements ConstrainedElement {
		private final SizeConstraints constraints = new SizeConstraints();
		private float customPrefWidth = -1f;
		private float customPrefHeight = -1f;

		@Override
		public SizeConstraints getSizeConstraints() {
			return constraints;
		}

		public SizedImage() {
			super((Drawable) null);
		}

		public SizedImage(Drawable drawable) {
			super(drawable);
		}

		public SizedImage(Drawable drawable, Scaling scaling) {
			super(drawable, scaling);
		}

		public SizedImage growX() {
			constraints.growX = true;
			constraints.applyGrowToParentCell(this);
			return this;
		}

		public SizedImage growY() {
			constraints.growY = true;
			constraints.applyGrowToParentCell(this);
			return this;
		}

		public SizedImage grow() {
			return growX().growY();
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

		private Scaling scaling = Scaling.fit;

		public Scaling getScaling() {
			return scaling;
		}

		public SizedImage scaling(Scaling scaling) {
			this.scaling = scaling;
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

		public SizedImage height(@Nullable Readable<Float> height) {
			if (height != null) {
				Effect e = Effect.of(() -> {
					Float h = height.get();
					if (h != null) height(h);
				});
				ComponentContext.register(e);
			}
			return this;
		}

		public SizedImage width(float width) {
			this.customPrefWidth = width;
			setWidth(width);
			invalidateHierarchy();
			return this;
		}

		public SizedImage width(@Nullable Readable<Float> width) {
			if (width != null) {
				Effect e = Effect.of(() -> {
					Float w = width.get();
					if (w != null) width(w);
				});
				ComponentContext.register(e);
			}
			return this;
		}

		public SizedImage size(@Nullable Readable<Float> size) {
			if (size != null) {
				width(size);
				height(size);
			}
			return this;
		}

		public SizedImage size(@Nullable Readable<Float> width, @Nullable Readable<Float> height) {
			if (width != null) width(width);
			if (height != null) height(height);
			return this;
		}

		private float padTop;
		private float padLeft;
		private float padBottom;
		private float padRight;

		private float marginTop;
		private float marginLeft;
		private float marginBottom;
		private float marginRight;

		public SizedImage padding(float p) {
			this.padTop = this.padLeft = this.padBottom = this.padRight = p;
			applySpacing();
			return this;
		}

		public SizedImage padding(float top, float left, float bottom, float right) {
			this.padTop = top;
			this.padLeft = left;
			this.padBottom = bottom;
			this.padRight = right;
			applySpacing();
			return this;
		}

		public SizedImage paddingTop(float top) {
			this.padTop = top;
			applySpacing();
			return this;
		}

		public SizedImage paddingBottom(float bottom) {
			this.padBottom = bottom;
			applySpacing();
			return this;
		}

		public SizedImage paddingLeft(float left) {
			this.padLeft = left;
			applySpacing();
			return this;
		}

		public SizedImage paddingRight(float right) {
			this.padRight = right;
			applySpacing();
			return this;
		}

		public SizedImage margin(float m) {
			this.marginTop = this.marginLeft = this.marginBottom = this.marginRight = m;
			applySpacing();
			return this;
		}

		public SizedImage margin(float top, float left, float bottom, float right) {
			this.marginTop = top;
			this.marginLeft = left;
			this.marginBottom = bottom;
			this.marginRight = right;
			applySpacing();
			return this;
		}

		public SizedImage marginTop(float top) {
			this.marginTop = top;
			applySpacing();
			return this;
		}

		public SizedImage marginBottom(float bottom) {
			this.marginBottom = bottom;
			applySpacing();
			return this;
		}

		public SizedImage marginLeft(float left) {
			this.marginLeft = left;
			applySpacing();
			return this;
		}

		public SizedImage marginRight(float right) {
			this.marginRight = right;
			applySpacing();
			return this;
		}

		public void applySpacing() {
			if (parent instanceof Table) {
				Cell<?> cell = ((Table) parent).getCell(this);
				if (cell != null) {
					cell.pad(padTop + marginTop, padLeft + marginLeft, padBottom + marginBottom, padRight + marginRight);
				}
			}
		}

		public void setCustomPrefWidth(float w) {
			this.customPrefWidth = w;
		}

		public void setCustomPrefHeight(float h) {
			this.customPrefHeight = h;
		}

		public float getCustomPrefWidth() {
			return customPrefWidth;
		}

		public float getCustomPrefHeight() {
			return customPrefHeight;
		}

		@Override
		public float getPrefWidth() {
			if (constraints != null && constraints.prefWidth != null) {
				Float v = constraints.prefWidth.get();
				if (v != null) return Math.max(0f, v);
			}
			return customPrefWidth >= 0 ? customPrefWidth : super.getPrefWidth();
		}

		@Override
		public float getPrefHeight() {
			if (constraints != null && constraints.prefHeight != null) {
				Float v = constraints.prefHeight.get();
				if (v != null) return Math.max(0f, v);
			}
			return customPrefHeight >= 0 ? customPrefHeight : super.getPrefHeight();
		}

		@Override
		public float getMinWidth() {
			if (constraints != null && constraints.minWidth != null) {
				Float v = constraints.minWidth.get();
				if (v != null) return Math.max(0f, v);
			}
			return super.getMinWidth();
		}

		@Override
		public float getMinHeight() {
			if (constraints != null && constraints.minHeight != null) {
				Float v = constraints.minHeight.get();
				if (v != null) return Math.max(0f, v);
			}
			return super.getMinHeight();
		}

		@Override
		public float getMaxWidth() {
			if (constraints != null && constraints.maxWidth != null) {
				Float v = constraints.maxWidth.get();
				if (v != null) return Math.max(0f, v);
			}
			return super.getMaxWidth();
		}

		@Override
		public float getMaxHeight() {
			if (constraints != null && constraints.maxHeight != null) {
				Float v = constraints.maxHeight.get();
				if (v != null) return Math.max(0f, v);
			}
			return super.getMaxHeight();
		}
	}

	private final SizedImage image = new SizedImage();
	private @Nullable Effect binding;

	private float padTop;
	private float padLeft;
	private float padBottom;
	private float padRight;

	private float marginTop;
	private float marginLeft;
	private float marginBottom;
	private float marginRight;

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

	public SolimImage growX() {
		image.growX();
		return this;
	}

	public SolimImage growY() {
		image.growY();
		return this;
	}

	public SolimImage grow() {
		return growX().growY();
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

	public SolimImage padding(float p) {
		this.padTop = this.padLeft = this.padBottom = this.padRight = p;
		applySpacing();
		return this;
	}

	public SolimImage padding(float top, float left, float bottom, float right) {
		this.padTop = top;
		this.padLeft = left;
		this.padBottom = bottom;
		this.padRight = right;
		applySpacing();
		return this;
	}

	public SolimImage paddingTop(float top) {
		this.padTop = top;
		applySpacing();
		return this;
	}

	public SolimImage paddingBottom(float bottom) {
		this.padBottom = bottom;
		applySpacing();
		return this;
	}

	public SolimImage paddingLeft(float left) {
		this.padLeft = left;
		applySpacing();
		return this;
	}

	public SolimImage paddingRight(float right) {
		this.padRight = right;
		applySpacing();
		return this;
	}

	public SolimImage margin(float m) {
		this.marginTop = this.marginLeft = this.marginBottom = this.marginRight = m;
		applySpacing();
		return this;
	}

	public SolimImage margin(float top, float left, float bottom, float right) {
		this.marginTop = top;
		this.marginLeft = left;
		this.marginBottom = bottom;
		this.marginRight = right;
		applySpacing();
		return this;
	}

	public SolimImage marginTop(float top) {
		this.marginTop = top;
		applySpacing();
		return this;
	}

	public SolimImage marginBottom(float bottom) {
		this.marginBottom = bottom;
		applySpacing();
		return this;
	}

	public SolimImage marginLeft(float left) {
		this.marginLeft = left;
		applySpacing();
		return this;
	}

	public SolimImage marginRight(float right) {
		this.marginRight = right;
		applySpacing();
		return this;
	}

	public void applySpacing() {
		if (image.parent instanceof Table) {
			Cell<?> cell = ((Table) image.parent).getCell(image);
			if (cell != null) {
				cell.pad(padTop + marginTop, padLeft + marginLeft, padBottom + marginBottom, padRight + marginRight);
			}
		}
	}

	public SizedImage image() {
		applySpacing();
		return image;
	}

	@Override
	public Element element() {
		applySpacing();
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
