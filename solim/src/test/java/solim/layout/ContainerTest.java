package solim.layout;

import static org.junit.jupiter.api.Assertions.*;

import arc.Core;
import arc.mock.MockApplication;
import arc.mock.MockGraphics;
import arc.scene.Element;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ContainerTest {

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
	void containerCreatesTable() {
		Container c = new Container();
		assertNotNull(c.element());
		assertNotNull(c.table());
	}

	@Test
	void containerAddsChild() {
		Container c = new Container().padding(12f);
		Element child = new Element();
		c.add(child);
		assertEquals(1, c.table().getChildren().size);
	}

	@Test
	void containerPaddingModifier() {
		Container c = new Container();
		c.padding(16f);
		assertNotNull(c.table());
	}

	@Test
	void containerNameModifier() {
		Container c = new Container();
		c.name("my-container");
		assertEquals("my-container", c.table().name);
	}

	@Test
	void containerImplementsComponent() {
		Container c = new Container();
		assertInstanceOf(solim.core.Component.class, c);
	}
}
