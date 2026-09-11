package solim.graphics;

import static org.junit.jupiter.api.Assertions.*;

import arc.scene.style.Drawable;
import org.junit.jupiter.api.Test;

class CircleDrawableTest {

	@Test
	void testInstanceNotNull() {
		assertNotNull(CircleDrawable.INSTANCE);
		assertTrue(CircleDrawable.INSTANCE instanceof Drawable);
	}

	@Test
	void testDefaultDimensionsAreZero() {
		CircleDrawable cd = CircleDrawable.INSTANCE;
		assertEquals(0f, cd.getMinWidth());
		assertEquals(0f, cd.getMinHeight());
		assertEquals(0f, cd.getLeftWidth());
		assertEquals(0f, cd.getRightWidth());
		assertEquals(0f, cd.getTopHeight());
		assertEquals(0f, cd.getBottomHeight());
	}

	@Test
	void testImageLayout() {
		if (arc.Core.app == null) arc.Core.app = new arc.mock.MockApplication();
		if (arc.Core.graphics == null) arc.Core.graphics = new arc.mock.MockGraphics();
		arc.scene.ui.Image img = new arc.scene.ui.Image(CircleDrawable.INSTANCE);
		img.setSize(12f, 12f);
		img.layout();
		System.out.println("DEBUG_IMG_WIDTH: " + img.getImageWidth() + ", DEBUG_IMG_HEIGHT: " + img.getImageHeight());
		assertEquals(12f, img.getImageWidth());
		assertEquals(12f, img.getImageHeight());
	}
}
