package solim.modifier;

import static org.junit.jupiter.api.Assertions.*;

import arc.scene.Element;
import arc.scene.ui.layout.CellAccess;
import arc.scene.ui.layout.Table;
import org.junit.jupiter.api.Test;
import solim.input.Button;
import solim.layout.Card;
import solim.layout.Column;
import solim.layout.Grid;
import solim.layout.ReactiveGrid;
import solim.layout.Row;
import solim.layout.Wrap;
import solim.signal.Signal;

class ElementModifiersTest {

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
}
