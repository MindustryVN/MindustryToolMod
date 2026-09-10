package solim.layout;

import static org.junit.jupiter.api.Assertions.*;

import arc.Core;
import arc.mock.MockApplication;
import arc.mock.MockGraphics;
import arc.scene.Element;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class WrapTest {

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
	void createsTableWithDefaultName() {
		Wrap w = new Wrap();
		assertEquals("solim-wrap-table", w.table().name);
	}

	@Test
	void addAddsChildToTable() {
		Wrap w = new Wrap();
		Element first = new Element();
		Element second = new Element();

		w.add(first);
		w.add(second);

		assertEquals(2, w.table().getChildren().size);
		assertSame(first, w.table().getChildren().get(0));
		assertSame(second, w.table().getChildren().get(1));
	}

	@Test
	void nameModifierUpdatesTableName() {
		Wrap w = new Wrap();
		w.name("my-wrap");
		assertEquals("my-wrap", w.table().name);
	}

	@Test
	void tableIsSameAsElement() {
		Wrap w = new Wrap();
		assertSame(w.table(), w.element());
	}
}
