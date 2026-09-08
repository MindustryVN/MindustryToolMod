package solim.modifier;

import static org.junit.jupiter.api.Assertions.*;

import arc.Core;
import arc.mock.MockApplication;
import arc.mock.MockGraphics;
import arc.scene.Element;
import arc.scene.ui.layout.CellAccess;
import arc.scene.ui.layout.Table;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import solim.input.Button;
import solim.layout.Card;
import solim.layout.Column;
import solim.layout.Container;
import solim.layout.Divider;
import solim.layout.Grid;
import solim.layout.ReactiveGrid;
import solim.layout.Row;
import solim.layout.Scroll;
import solim.layout.SolimStack;
import solim.layout.Spacer;
import solim.layout.Wrap;
import solim.signal.Signal;

class ElementModifiersTest {

	@BeforeAll
	static void checkArcContext() {
		if (Core.app == null) {
			Core.app = new MockApplication();
		}
		if (Core.graphics == null) {
			Core.graphics = new MockGraphics();
		}
	}

	@Test
	void elementModifiersGapOnTable() {
		Table table = new Table();
		ElementModifiers.gap(table, 16f);
		assertEquals(8f, CellAccess.padTop(table.defaults()), 0.01f);
		assertEquals(8f, CellAccess.padLeft(table.defaults()), 0.01f);
		assertEquals(8f, CellAccess.padBottom(table.defaults()), 0.01f);
		assertEquals(8f, CellAccess.padRight(table.defaults()), 0.01f);
	}

	@Test
	void elementModifiersGapOnElementOverload() {
		Table table = new Table();
		ElementModifiers.gap((Element) table, 20f);
		assertEquals(10f, CellAccess.padTop(table.defaults()), 0.01f);

		Element element = new Element();
		assertDoesNotThrow(() -> ElementModifiers.gap(element, 20f));
		assertDoesNotThrow(() -> ElementModifiers.gap((Table) null, 20f));
		assertDoesNotThrow(() -> ElementModifiers.gap((Element) null, 20f));
	}

	@Test
	void componentGapDelegation() {
		Row row = new Row().gap(14f);
		assertEquals(7f, CellAccess.padTop(row.table().defaults()), 0.01f);

		Column column = new Column().gap(18f);
		assertEquals(9f, CellAccess.padTop(column.table().defaults()), 0.01f);

		Grid grid = new Grid().gap(10f);
		assertEquals(5f, CellAccess.padTop(grid.table().defaults()), 0.01f);

		Wrap wrap = new Wrap().gap(12f);
		assertEquals(6f, CellAccess.padTop(wrap.table().defaults()), 0.01f);

		Card card = new Card().gap(16f);
		assertEquals(8f, CellAccess.padTop(card.container().defaults()), 0.01f);

		Button button = new Button().gap(8f);
		assertEquals(4f, CellAccess.padTop(button.sizedButton().defaults()), 0.01f);

		ReactiveGrid<String, String> rgrid = ReactiveGrid.of(
			Signal.of(2),
			Signal.of(java.util.Collections.singletonList("item")),
			s -> s,
			s -> new Row()
		).gap(24f);
		assertEquals(12f, CellAccess.padTop(rgrid.table().defaults()), 0.01f);
	}

	@Test
	void elementModifiersNameOnElement() {
		Element element = new Element();
		ElementModifiers.name(element, "test-element");
		assertEquals("test-element", element.name);

		assertDoesNotThrow(() -> ElementModifiers.name(null, "ignored"));
	}

	@Test
	void elementModifiersNullSafe() {
		assertDoesNotThrow(() -> ElementModifiers.width(null, 10f));
		assertDoesNotThrow(() -> ElementModifiers.height(null, 10f));
		assertDoesNotThrow(() -> ElementModifiers.size(null, 10f));
		assertDoesNotThrow(() -> ElementModifiers.size(null, 10f, 10f));
		assertDoesNotThrow(() -> ElementModifiers.x(null, 10f));
		assertDoesNotThrow(() -> ElementModifiers.y(null, 10f));
		assertDoesNotThrow(() -> ElementModifiers.position(null, 10f, 10f));
		assertDoesNotThrow(() -> ElementModifiers.visible(null, true));
		assertDoesNotThrow(() -> ElementModifiers.align(null, 0));
		assertDoesNotThrow(() -> ElementModifiers.top(null));
		assertDoesNotThrow(() -> ElementModifiers.bottom(null));
		assertDoesNotThrow(() -> ElementModifiers.left(null));
		assertDoesNotThrow(() -> ElementModifiers.right(null));
		assertDoesNotThrow(() -> ElementModifiers.center(null));
		assertDoesNotThrow(() -> ElementModifiers.margin(null, 10f));
		assertDoesNotThrow(() -> ElementModifiers.padding(null, 10f));
	}

	@Test
	void componentNameDelegationAndChaining() {
		Row row = new Row().name("my-row").gap(8f);
		assertEquals("my-row", row.element().name);

		Column column = new Column().name("my-column").gap(8f);
		assertEquals("my-column", column.element().name);

		Card card = new Card().name("my-card").gap(8f);
		assertEquals("my-card", card.element().name);

		Scroll scroll = new Scroll().name("my-scroll");
		assertEquals("my-scroll", scroll.element().name);

		Grid grid = new Grid().name("my-grid").gap(8f);
		assertEquals("my-grid", grid.element().name);

		Container container = new Container().name("my-container");
		assertEquals("my-container", container.element().name);

		Divider divider = new Divider().name("my-divider");
		assertEquals("my-divider", divider.element().name);

		Spacer spacer = new Spacer().name("my-spacer");
		assertEquals("my-spacer", spacer.element().name);

		SolimStack stack = new SolimStack().name("my-stack");
		assertEquals("my-stack", stack.element().name);

		Wrap wrap = new Wrap().name("my-wrap").gap(8f);
		assertEquals("my-wrap", wrap.element().name);

		Button button = new Button().name("my-button").gap(8f);
		assertEquals("my-button", button.element().name);
	}
}
