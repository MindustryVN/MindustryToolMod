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
	void nameModifierUpdatesTableName() {
		Divider d = new Divider();
		d.name("my-divider");
		assertEquals("my-divider", d.table().name);
	}

	@Test
	void tableIsSameAsElement() {
		Divider d = new Divider();
		assertSame(d.table(), d.element());
	}
}
