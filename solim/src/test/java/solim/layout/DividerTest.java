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
	void dividerDefaultDirection() {
		Divider d = new Divider();
		assertNotNull(d.element());
		assertNotNull(d.table());
		assertEquals(Direction.X, d.direction());
	}

	@Test
	void dividerHorizontalDirection() {
		Divider d = new Divider(Direction.X);
		assertEquals(Direction.X, d.direction());
		assertTrue(d.sizeConstraints().growX);
		assertFalse(d.sizeConstraints().growY);
	}

	@Test
	void dividerVerticalDirection() {
		Divider d = new Divider(Direction.Y);
		assertEquals(Direction.Y, d.direction());
		assertTrue(d.sizeConstraints().growY);
		assertFalse(d.sizeConstraints().growX);
	}

	@Test
	void dividerNullDirectionDefaultsToX() {
		Divider d = new Divider(null);
		assertEquals(Direction.X, d.direction());
	}

	@Test
	void dividerNameModifier() {
		Divider d = new Divider();
		d.name("my-divider");
		assertEquals("my-divider", d.table().name);
	}

	@Test
	void dividerImplementsComponent() {
		Divider d = new Divider();
		assertInstanceOf(solim.core.Component.class, d);
	}

	@Test
	void dividerSizeConstraints() {
		Divider d = new Divider();
		assertNotNull(d.sizeConstraints());
	}
}
