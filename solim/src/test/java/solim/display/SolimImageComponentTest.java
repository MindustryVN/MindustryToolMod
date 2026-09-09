package solim.display;

import static org.junit.jupiter.api.Assertions.*;

import arc.Core;
import arc.graphics.Color;
import arc.mock.MockApplication;
import arc.mock.MockGraphics;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import solim.signal.Signal;

class SolimImageComponentTest {

	@BeforeAll
	static void initArc() {
		if (Core.app == null) {
			Core.app = new MockApplication();
		}
		if (Core.graphics == null) {
			Core.graphics = new MockGraphics();
		}
	}

	@Test
	void imageCreatesElement() {
		SolimImage img = new SolimImage();
		assertNotNull(img.element());
		assertNotNull(img.image());
	}

	@Test
	void imageStaticFactory() {
		SolimImage img = SolimImage.of((arc.scene.style.Drawable) null);
		assertNotNull(img.element());
	}

	@Test
	void imageSizeModifier() {
		SolimImage img = new SolimImage();
		img.size(64f, 48f);
		assertEquals(64f, img.image().getWidth(), 0.01f);
		assertEquals(48f, img.image().getHeight(), 0.01f);
	}

	@Test
	void imageWidthAndHeight() {
		SolimImage img = new SolimImage();
		img.width(100f);
		img.height(50f);
		assertNotNull(img.image());
	}

	@Test
	void imageColorModifier() {
		SolimImage img = new SolimImage();
		img.color(Color.red);
		assertEquals(Color.red, img.image().color);
	}

	@Test
	void imageReactiveColor() {
		Signal<Color> colorSig = Signal.of(Color.green);
		SolimImage img = new SolimImage().color(colorSig);
		assertEquals(Color.green, img.image().color);
		colorSig.set(Color.blue);
		assertEquals(Color.blue, img.image().color);
	}

	@Test
	void imageVisibleModifier() {
		SolimImage img = new SolimImage();
		img.visible(false);
		assertFalse(img.image().visible);
		img.visible(true);
		assertTrue(img.image().visible);
	}

	@Test
	void imagePositionModifiers() {
		SolimImage img = new SolimImage();
		img.x(10f);
		assertEquals(10f, img.image().x, 0.01f);
		img.y(20f);
		assertEquals(20f, img.image().y, 0.01f);
		img.position(30f, 40f);
		assertEquals(30f, img.image().x, 0.01f);
		assertEquals(40f, img.image().y, 0.01f);
	}

	@Test
	void imageGrowModifiers() {
		SolimImage img = new SolimImage();
		img.growX();
		img.growY();
		img.grow();
		assertNotNull(img.image());
	}

	@Test
	void imageNameModifier() {
		SolimImage img = new SolimImage().name("my-image");
		assertEquals("my-image", img.element().name);
	}

	@Test
	void imagePaddingAndMargin() {
		SolimImage img = new SolimImage();
		img.padding(8f);
		img.margin(4f);
		img.paddingTop(2f);
		img.paddingBottom(3f);
		img.paddingLeft(4f);
		img.paddingRight(5f);
		img.marginTop(1f);
		img.marginBottom(2f);
		img.marginLeft(3f);
		img.marginRight(4f);
		assertNotNull(img.image());
	}

	@Test
	void imageImplementsComponent() {
		SolimImage img = new SolimImage();
		assertInstanceOf(solim.core.Component.class, img);
	}

	@Test
	void imageDispose() {
		SolimImage img = new SolimImage();
		assertDoesNotThrow(img::dispose);
	}
}
