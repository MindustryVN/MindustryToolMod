package solim.layout;

import static org.junit.jupiter.api.Assertions.*;

import arc.Core;
import arc.mock.MockApplication;
import arc.mock.MockGraphics;
import arc.scene.Element;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import solim.signal.Signal;

class RowTest {

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
	void rowCreatesTable() {
		Row row = new Row();
		assertNotNull(row.element());
		assertNotNull(row.table());
	}

	@Test
	void rowAddsChildElements() {
		Row row = new Row();
		Element a = new Element();
		Element b = new Element();
		row.add(a);
		row.add(b);
		assertEquals(2, row.table().getChildren().size);
		assertSame(a, row.table().getChildren().get(0));
		assertSame(b, row.table().getChildren().get(1));
	}

	@Test
	void rowGapModifier() {
		Row row = new Row();
		row.gap(8f);
		Element a = new Element();
		Element b = new Element();
		row.add(a);
		row.add(b);
		assertEquals(2, row.table().getChildren().size);
	}

	@Test
	void rowPaddingModifier() {
		Row row = new Row();
		row.padding(12f);
		assertNotNull(row.table());
	}

	@Test
	void rowPaddingFourArgs() {
		Row row = new Row();
		row.padding(1f, 2f, 3f, 4f);
		assertNotNull(row.table());
	}

	@Test
	void rowJustifyAllVariants() {
		for (Justify j : Justify.values()) {
			Row row = new Row().justify(j);
			assertNotNull(row.table());
		}
	}

	@Test
	void rowAlignAllVariants() {
		for (Align a : Align.values()) {
			Row row = new Row().align(a);
			assertNotNull(row.table());
		}
	}

	@Test
	void rowTopBottomLeftRightCenter() {
		Row row = new Row();
		row.top();
		row.bottom();
		row.left();
		row.right();
		row.center();
		assertNotNull(row.table());
	}

	@Test
	void rowVisibleModifier() {
		Row row = new Row();
		row.visible(false);
		assertFalse(row.table().visible);
		row.visible(true);
		assertTrue(row.table().visible);
	}

	@Test
	void rowReactiveVisible() {
		Signal<Boolean> vis = Signal.of(true);
		Row row = new Row();
		row.visible(vis);
		assertTrue(row.table().visible);
		vis.set(false);
		assertFalse(row.table().visible);
	}

	@Test
	void rowPositionModifiers() {
		Row row = new Row();
		row.x(10f);
		assertEquals(10f, row.table().x, 0.01f);
		row.y(20f);
		assertEquals(20f, row.table().y, 0.01f);
		row.position(30f, 40f);
		assertEquals(30f, row.table().x, 0.01f);
		assertEquals(40f, row.table().y, 0.01f);
	}

	@Test
	void rowNameModifier() {
		Row row = new Row();
		row.name("my-row");
		assertEquals("my-row", row.table().name);
	}

	@Test
	void rowChildrenRunnable() {
		Row row = new Row();
		row.children(() -> {
			Element e = new Element();
			row.add(e);
		});
		assertEquals(1, row.table().getChildren().size);
	}

	@Test
	void rowDraggable() {
		Row row = new Row().draggable();
		assertNotNull(row.table());
	}

	@Test
	void rowImplementsComponent() {
		Row row = new Row();
		assertInstanceOf(solim.core.Component.class, row);
	}

	@Test
	void rowSizeConstraints() {
		Row row = new Row();
		assertNotNull(row.sizeConstraints());
	}
}
