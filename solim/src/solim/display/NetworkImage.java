package solim.display;

import arc.Core;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.Texture.TextureFilter;
import arc.graphics.g2d.TextureRegion;
import arc.scene.Element;
import arc.scene.style.Drawable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.util.Http;
import arc.util.Nullable;
import arc.util.Scaling;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import solim.core.Component;
import solim.core.ComponentContext;
import solim.core.Disposable;
import solim.display.SolimImage.SizedImage;
import solim.layout.LayoutModifiers;
import solim.layout.SizeConstraints;
import solim.modifier.ElementModifiers;
import solim.signal.Effect;
import solim.signal.Readable;

/**
 * Asynchronous image component that loads images from HTTP/HTTPS URLs,
 * maintains an in-memory texture cache, and supports reactive URL bindings and fallbacks.
 */
public final class NetworkImage implements Component, LayoutModifiers<NetworkImage> {

	@FunctionalInterface
	public interface ImageLoader {
		void load(String url, Cons<TextureRegion> onSuccess, Cons<Throwable> onError);
	}

	private static final Map<String, TextureRegionDrawable> cache = new ConcurrentHashMap<>();

	private static ImageLoader loader = (url, onSuccess, onError) -> {
		if (url == null || url.trim().isEmpty()) {
			if (onError != null) onError.get(new IllegalArgumentException("Empty URL"));
			return;
		}
		try {
			Http.get(url)
					.timeout(10000)
					.error(onError != null ? onError : err -> {})
					.submit(response -> {
						byte[] bytes = response.getResult();
						if (bytes == null || bytes.length == 0) {
							if (onError != null) onError.get(new IllegalStateException("Empty response"));
							return;
						}
						if (Core.app != null) {
							Core.app.post(() -> {
								try {
									Pixmap pixmap = new Pixmap(bytes);
									Texture texture = new Texture(pixmap);
									texture.setFilter(TextureFilter.linear);
									TextureRegion region = new TextureRegion(texture);
									pixmap.dispose();
									if (onSuccess != null) onSuccess.get(region);
								} catch (Throwable t) {
									if (onError != null) onError.get(t);
								}
							});
						}
					});
		} catch (Throwable t) {
			if (onError != null) onError.get(t);
		}
	};

	public static void setImageLoader(ImageLoader customLoader) {
		loader = customLoader != null ? customLoader : (url, s, e) -> {};
	}

	public static void clearCache() {
		cache.clear();
	}

	public static boolean isCached(String url) {
		return url != null && cache.containsKey(url);
	}

	public static @Nullable TextureRegionDrawable getCached(String url) {
		return url != null ? cache.get(url) : null;
	}

	public static void putCache(String url, TextureRegion region) {
		if (url != null && region != null) {
			cache.put(url, new TextureRegionDrawable(region));
		}
	}

	private final SizedImage image;
	private @Nullable Drawable placeholder;
	private @Nullable Drawable fallback;
	private @Nullable Disposable binding;
	private @Nullable String currentUrl;

	private float padTop, padLeft, padBottom, padRight;
	private float marginTop, marginLeft, marginBottom, marginRight;

	public NetworkImage() {
		this.image = new SizedImage((Drawable) null);
		this.image.setScaling(Scaling.fit);
		ComponentContext.register(this);
	}

	public NetworkImage(String url) {
		this();
		url(url);
	}

	public NetworkImage(Readable<String> url) {
		this();
		url(url);
	}

	public NetworkImage placeholder(@Nullable Drawable placeholder) {
		this.placeholder = placeholder;
		if (image.getDrawable() == null && placeholder != null) {
			image.setDrawable(placeholder);
		}
		return this;
	}

	private boolean failed = false;

	public NetworkImage fallback(@Nullable Drawable fallback) {
		this.fallback = fallback;
		if (failed && fallback != null) {
			applyDrawable(fallback);
		}
		return this;
	}

	public NetworkImage scaling(Scaling scaling) {
		image.scaling(scaling);
		return this;
	}

	public NetworkImage url(@Nullable String url) {
		if (this.binding != null) {
			this.binding.dispose();
			this.binding = null;
		}
		loadUrl(url);
		return this;
	}

	public NetworkImage url(@Nullable Readable<String> url) {
		if (this.binding != null) {
			this.binding.dispose();
			this.binding = null;
		}
		if (url != null) {
			this.binding = Effect.of(() -> loadUrl(url.get()));
		}
		return this;
	}

	private void loadUrl(@Nullable String url) {
		this.currentUrl = url;
		this.failed = false;
		if (url == null || url.trim().isEmpty()) {
			this.failed = true;
			applyDrawable(fallback != null ? fallback : placeholder);
			return;
		}

		TextureRegionDrawable cached = cache.get(url);
		if (cached != null) {
			applyDrawable(cached);
			return;
		}

		if (placeholder != null) {
			applyDrawable(placeholder);
		}

		final String targetUrl = url;
		loader.load(targetUrl, region -> {
			TextureRegionDrawable drawable = new TextureRegionDrawable(region);
			cache.put(targetUrl, drawable);
			if (targetUrl.equals(this.currentUrl)) {
				this.failed = false;
				applyDrawable(drawable);
			}
		}, error -> {
			if (targetUrl.equals(this.currentUrl)) {
				this.failed = true;
				applyDrawable(fallback != null ? fallback : placeholder);
			}
		});
	}

	private void applyDrawable(@Nullable Drawable drawable) {
		if (drawable != null) {
			image.setDrawable(drawable);
		}
		image.invalidateHierarchy();
	}

	public NetworkImage size(float size) {
		return size(size, size);
	}

	public NetworkImage size(float width, float height) {
		width(width);
		height(height);
		return this;
	}

	public NetworkImage width(float width) {
		ElementModifiers.width(image, width);
		return this;
	}

	public NetworkImage height(float height) {
		ElementModifiers.height(image, height);
		return this;
	}


	public NetworkImage color(Color color) {
		image.color(color);
		return this;
	}

	public NetworkImage color(Readable<Color> color) {
		image.color(color);
		return this;
	}

	@Override
	public SizeConstraints sizeConstraints() {
		return image.getSizeConstraints();
	}

	@Override
	public NetworkImage name(String name) {
		ElementModifiers.name(image, name);
		return this;
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

	public NetworkImage padding(float p) {
		this.padTop = this.padLeft = this.padBottom = this.padRight = p;
		applySpacing();
		return this;
	}

	public NetworkImage margin(float m) {
		this.marginTop = this.marginLeft = this.marginBottom = this.marginRight = m;
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

	@Override
	public void dispose() {
		if (binding != null) {
			binding.dispose();
			binding = null;
		}
	}
}
