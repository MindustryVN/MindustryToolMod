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
	void wrapCreatesTable() {
		Wrap w = new Wrap();
		assertNotNull(w.element());
		assertNotNull(w.table());
	}

	@Test
	void wrapAddsChildren() {
		Wrap w = new Wrap().gap(4f);
		for (int i = 0; i < 3; i++) {
			w.add(new Element());
		}
		assertEquals(3, w.table().getChildren().size);
	}

	@Test
	void wrapGapModifier() {
		Wrap w = new Wrap();
		w.gap(8f);
		assertNotNull(w.table());
	}

	@Test
	void wrapNameModifier() {
		Wrap w = new Wrap();
		w.name("my-wrap");
		assertEquals("my-wrap", w.table().name);
	}

	@Test
	void wrapImplementsComponent() {
		Wrap w = new Wrap();
		assertInstanceOf(solim.core.Component.class, w);
	}
}
