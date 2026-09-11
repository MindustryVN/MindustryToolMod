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
}
