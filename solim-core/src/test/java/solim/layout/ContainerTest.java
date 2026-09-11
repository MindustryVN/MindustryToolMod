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
	void createsTableWithDefaultName() {
		Container c = new Container();
		assertEquals("solim-container-table", c.table().name);
	}

	@Test
	void addAddsChildToTable() {
		Container c = new Container();
		Element child = new Element();
		c.add(child);

		assertEquals(1, c.table().getChildren().size);
		assertSame(child, c.table().getChildren().get(0));
	}

	@Test
	void paddingPreservesChildrenAndReturnsSelf() {
		Container c = new Container();
		Element child = new Element();
		c.add(child);

		assertSame(c, c.padding(16f));
		assertEquals(1, c.table().getChildren().size);
		assertSame(child, c.table().getChildren().get(0));
	}

	@Test
	void nameModifierUpdatesTableName() {
		Container c = new Container();
		c.name("my-container");
		assertEquals("my-container", c.table().name);
	}

	@Test
	void tableIsSameAsElement() {
		Container c = new Container();
		assertSame(c.table(), c.element());
	}

	@Test
	void fluentApiReturnsSameContainer() {
		Container c = new Container();
		assertSame(c, c.padding(8f));
		assertSame(c, c.name("test"));
	}
}
