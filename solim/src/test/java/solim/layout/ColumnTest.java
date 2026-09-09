package solim.layout;

import static org.junit.jupiter.api.Assertions.*;

import arc.Core;
import arc.mock.MockApplication;
import arc.mock.MockGraphics;
import arc.scene.Element;
import arc.scene.ui.layout.CellAccess;
import arc.scene.ui.layout.Table;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import solim.signal.Signal;

class ColumnTest {

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
	void columnCreatesTable() {
		Column col = new Column();
		assertNotNull(col.element());
		assertNotNull(col.table());
	}

	@Test
	void columnAddsChildElements() {
		Column col = new Column();
		Element a = new Element();
		Element b = new Element();
		col.add(a).row();
		col.add(b);
		assertEquals(2, col.table().getChildren().size);
		assertSame(a, col.table().getChildren().get(0));
		assertSame(b, col.table().getChildren().get(1));
	}

	@Test
	void columnGapModifier() {
		Column col = new Column();
		col.gap(16f);
		Element a = new Element();
		Element b = new Element();
		col.add(a).row();
		col.add(b);
		assertEquals(2, col.table().getChildren().size);
	}

	@Test
	void columnPaddingModifier() {
		Column col = new Column();
		col.padding(24f);
		assertNotNull(col.table());
	}

	@Test
	void columnPaddingFourArgs() {
		Column col = new Column();
		col.padding(1f, 2f, 3f, 4f);
		assertNotNull(col.table());
	}

	@Test
	void columnAlignCenter() {
		Column col = new Column();
		col.align(Align.CENTER);
		assertNotNull(col.table());
	}

	@Test
	void columnAlignStart() {
		Column col = new Column();
		col.align(Align.START);
		assertNotNull(col.table());
	}

	@Test
	void columnAlignEnd() {
		Column col = new Column();
		col.align(Align.END);
		assertNotNull(col.table());
	}

	@Test
	void columnTopBottomLeftRightCenter() {
		Column col = new Column();
		col.top();
		col.bottom();
		col.left();
		col.right();
		col.center();
		assertNotNull(col.table());
	}

	@Test
	void columnVisibleModifier() {
		Column col = new Column();
		col.visible(false);
		assertFalse(col.table().visible);
		col.visible(true);
		assertTrue(col.table().visible);
	}

	@Test
	void columnReactiveVisible() {
		Signal<Boolean> vis = Signal.of(true);
		Column col = new Column();
		col.visible(vis);
		assertTrue(col.table().visible);
		vis.set(false);
		assertFalse(col.table().visible);
	}

	@Test
	void columnPositionModifiers() {
		Column col = new Column();
		col.x(10f);
		assertEquals(10f, col.table().x, 0.01f);
		col.y(20f);
		assertEquals(20f, col.table().y, 0.01f);
		col.position(30f, 40f);
		assertEquals(30f, col.table().x, 0.01f);
		assertEquals(40f, col.table().y, 0.01f);
	}

	@Test
	void columnNameModifier() {
		Column col = new Column();
		col.name("my-column");
		assertEquals("my-column", col.table().name);
	}

	@Test
	void columnChildrenRunnable() {
		Column col = new Column();
		col.children(() -> {
			Element e = new Element();
			col.add(e);
		});
		assertEquals(1, col.table().getChildren().size);
	}

	@Test
	void columnGrowModifiers() {
		Column col = new Column();
		Element child = new Element();
		col.add(child);
		// Column uses growY for expanding children
		assertNotNull(col.table());
	}

	@Test
	void columnImplementsComponent() {
		Column col = new Column();
		assertInstanceOf(solim.core.Component.class, col);
	}

	@Test
	void columnSizeConstraints() {
		Column col = new Column();
		assertNotNull(col.sizeConstraints());
	}
}
