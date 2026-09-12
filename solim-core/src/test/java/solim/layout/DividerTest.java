package solim.layout;

import static org.junit.jupiter.api.Assertions.*;

import arc.Core;
import arc.mock.MockApplication;
import arc.mock.MockGraphics;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DividerTest {

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
	void defaultDirectionIsX() {
		Divider d = new Divider();
		assertEquals(Direction.X, d.direction());
	}

	@Test
	void horizontalDividerSetsGrowX() {
		Divider d = new Divider(Direction.X);
		assertTrue(d.sizeConstraints().growX);
		assertFalse(d.sizeConstraints().growY);
	}

	@Test
	void verticalDividerSetsGrowY() {
		Divider d = new Divider(Direction.Y);
		assertTrue(d.sizeConstraints().growY);
		assertFalse(d.sizeConstraints().growX);
	}

	@Test
	void nullDirectionDefaultsToX() {
		Divider d = new Divider(null);
		assertEquals(Direction.X, d.direction());
	}

	@Test
	void nameModifierUpdatesName() {
		Divider d = new Divider();
		d.name("my-divider");
		assertEquals("my-divider", d.element().name);
	}

	@Test
	void elementIsImage() {
		Divider d = new Divider();
		assertTrue(d.element() instanceof arc.scene.ui.Image);
	}

	@Test
	void colorAndHeightModifiers() {
		Divider d = new Divider();
		d.color(arc.graphics.Color.green).height(2f);
		assertEquals(arc.graphics.Color.green, d.image().color);
		assertEquals(2f, d.sizeConstraints().prefHeight.get());
	}

	@Test
	void dividerStretchesDrawableToFillCell() {
		// Scaling.fit would shrink the square source pixel into a centered dot
		// instead of a line, rendering the divider effectively invisible.
		assertEquals(arc.util.Scaling.stretch, new Divider(Direction.X).solimImage().getScaling());
		assertEquals(arc.util.Scaling.stretch, new Divider(Direction.Y).solimImage().getScaling());
	}
}
