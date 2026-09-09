package solim.display;

import static org.junit.jupiter.api.Assertions.*;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import arc.mock.MockApplication;
import arc.mock.MockGraphics;
import arc.scene.style.TextureRegionDrawable;
import arc.util.Scaling;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import solim.signal.Signal;

class NetworkImageComponentTest {

	@BeforeAll
	static void initArc() {
		if (Core.app == null) {
			Core.app = new MockApplication();
		}
		if (Core.graphics == null) {
			Core.graphics = new MockGraphics();
		}
	}

	@BeforeEach
	void setup() {
		NetworkImage.clearCache();
	}

	@Test
	void networkImageElementIsBackedImage() {
		NetworkImage.setImageLoader((url, success, error) -> {});
		NetworkImage img = new NetworkImage("https://example.com/test.png");
		assertSame(img.image(), img.element());
		img.dispose();
	}

	@Test
	void networkImagePlaceholder() {
		TextureRegion region = new TextureRegion();
		TextureRegionDrawable placeholder = new TextureRegionDrawable(region);
		NetworkImage.setImageLoader((url, success, error) -> {});
		NetworkImage img = new NetworkImage("https://example.com/test.png")
				.placeholder(placeholder);
		assertSame(placeholder, img.image().getDrawable());
		img.dispose();
	}

	@Test
	void networkImageSuccessLoadsAndCaches() {
		TextureRegion mockRegion = new TextureRegion();
		NetworkImage.setImageLoader((url, success, error) -> {
			success.get(mockRegion);
		});
		NetworkImage img = new NetworkImage("https://example.com/avatar.png");
		assertTrue(NetworkImage.isCached("https://example.com/avatar.png"));
		img.dispose();
	}

	@Test
	void networkImageFailureUsesFallback() {
		TextureRegion mockFallbackRegion = new TextureRegion();
		TextureRegionDrawable fallback = new TextureRegionDrawable(mockFallbackRegion);
		NetworkImage.setImageLoader((url, success, error) -> {
			error.get(new RuntimeException("404 Not Found"));
		});
		NetworkImage img = new NetworkImage("https://example.com/missing.png")
				.fallback(fallback);
		assertSame(fallback, img.image().getDrawable());
		img.dispose();
	}

	@Test
	void networkImageReactiveUrl() {
		TextureRegion regionA = new TextureRegion();
		TextureRegion regionB = new TextureRegion();
		NetworkImage.setImageLoader((url, success, error) -> {
			if ("https://example.com/a.png".equals(url)) {
				success.get(regionA);
			} else if ("https://example.com/b.png".equals(url)) {
				success.get(regionB);
			}
		});
		Signal<String> urlSignal = Signal.of("https://example.com/a.png");
		NetworkImage img = new NetworkImage(urlSignal);
		assertTrue(NetworkImage.isCached("https://example.com/a.png"));
		urlSignal.set("https://example.com/b.png");
		assertTrue(NetworkImage.isCached("https://example.com/b.png"));
		img.dispose();
	}

	@Test
	void networkImageSizingAndScaling() {
		NetworkImage img = new NetworkImage()
				.size(64f, 48f)
				.scaling(Scaling.fill);
		assertEquals(64f, img.sizeConstraints().prefWidth.get());
		assertEquals(48f, img.sizeConstraints().prefHeight.get());
		assertEquals(Scaling.fill, img.image().getScaling());
		img.dispose();
	}

	@Test
	void networkImageDisposeStopsReactiveUrlUpdates() {
		arc.graphics.g2d.TextureRegion regionA = new arc.graphics.g2d.TextureRegion();
		NetworkImage.setImageLoader((url, success, error) -> success.get(regionA));
		Signal<String> urlSignal = Signal.of("https://example.com/a.png");
		NetworkImage img = new NetworkImage(urlSignal);
		assertTrue(NetworkImage.isCached("https://example.com/a.png"));

		img.dispose();
		urlSignal.set("https://example.com/b.png");

		// After disposal the new URL must not trigger a load.
		assertFalse(NetworkImage.isCached("https://example.com/b.png"));
	}
}
