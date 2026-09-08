package solim.layout;

import static org.junit.jupiter.api.Assertions.*;

import arc.Core;
import arc.graphics.Color;
import arc.mock.MockApplication;
import arc.mock.MockGraphics;
import arc.scene.Element;
import arc.scene.event.ClickListener;
import arc.scene.event.InputEvent;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.CellAccess;
import arc.scene.ui.layout.Table;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import solim.signal.Signal;
import solim.ui.ParentStack;
import solim.ui.Ui;

class LayoutTest {

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
	void rowJustifyAndGap() {
		Table root = new Table();
		ParentStack.push(root);
		Row r = new Row();
		r.gap(8f);
		r.justify(Justify.BETWEEN);
		r.align(Align.CENTER);
		Element a = new Element();
		a.name = "a";
		Element b = new Element();
		b.name = "b";
		r.add(a);
		r.add(b);
		assertEquals(2, r.table().getChildren().size);
		assertSame(a, r.table().getChildren().get(0));
		assertSame(b, r.table().getChildren().get(1));
		ParentStack.pop();
	}

	@Test
	void columnGapAndPadding() {
		Column c = new Column();
		c.gap(16f);
		c.padding(24f);
		Element e1 = new Element();
		Element e2 = new Element();
		c.add(e1).row();
		c.add(e2);
		assertEquals(2, c.table().getChildren().size);
	}

	@Test
	void spacerConsumesSpace() {
		Table root = new Table();
		ParentStack.push(root);
		Table row = new Table();
		Element back = new Element();
		back.name = "back";
		Element save = new Element();
		save.name = "save";
		row.add(back);
		row.add(new Spacer().element());
		row.add(save);
		assertEquals(3, row.getChildren().size);
		ParentStack.pop();
	}

	@Test
	void gridColumnsWrapping() {
		Grid g = new Grid(3).gap(8f);
		for (int i = 0; i < 4; i++) {
			g.add(new Element());
		}
		assertEquals(4, g.table().getChildren().size);
	}

	@Test
	void stackOverlay() {
		SolimStack s = new SolimStack();
		Element bg = new Element();
		Element fg = new Element();
		s.add(bg);
		s.add(fg);
		assertEquals(2, s.stack().getChildren().size);
	}

	@Test
	void dividerRenders() {
		Divider d = new Divider();
		assertNotNull(d.table());
	}

	@Test
	void containerAddsChild() {
		Container c = new Container().padding(12f);
		Element child = new Element();
		c.add(child);
		assertEquals(1, c.table().getChildren().size);
	}

	@Test
	void scrollWrapsContent() {
		Scroll s = new Scroll();
		Element e = new Element();
		s.add(e);
		assertNotNull(s.content());
		assertEquals(1, s.content().getChildren().size);
	}

	@Test
	void justifyAndAlignEnums() {
		assertEquals(6, Justify.values().length);
		assertTrue(Arrays.asList(Justify.values()).contains(Justify.BETWEEN));
		assertEquals(4, Align.values().length);
		assertTrue(Arrays.asList(Align.values()).contains(Align.STRETCH));
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
	void columnAligns() {
		Column c = new Column();
		c.align(Align.CENTER);
		assertNotNull(c.table());
	}

	@Test
	void rowJustifyVariants() {
		for (Justify j : Justify.values()) {
			Row r = new Row().justify(j);
			assertNotNull(r.table());
		}
	}

	@Test
	void declarativeColumnAndRowCellLayout() {
		Column col = Ui.column().children(() -> {
			Ui.row().children(() -> {
				Element e1 = new Element() {
					@Override
					public float getPrefWidth() {
						return 100f;
					}

					@Override
					public float getPrefHeight() {
						return 40f;
					}
				};
				ParentStack.add(e1);
			});
			Ui.scroll().grow().children(() -> {
				Element e2 = new Element() {
					@Override
					public float getPrefWidth() {
						return 200f;
					}

					@Override
					public float getPrefHeight() {
						return 200f;
					}
				};
				ParentStack.add(e2);
			});
		});

		Table t = col.table();
		assertEquals(2, t.getCells().size, "Column must have 2 cells for its 2 children");
		t.setSize(600f, 800f);
		t.layout();

		Element toolbar = t.getChildren().get(0);
		Element scroll = t.getChildren().get(1);

		assertTrue(toolbar.getWidth() > 0f, "Toolbar must have non-zero width");
		assertTrue(toolbar.getHeight() > 0f, "Toolbar must have non-zero height");
		assertTrue(scroll.getWidth() > 0f, "Scroll must have non-zero width");
		assertTrue(scroll.getHeight() > 0f, "Scroll must have non-zero height");
		assertTrue(scroll.y + scroll.getHeight() <= toolbar.y, "Scroll and Toolbar must not overlap vertically");
	}

	@Test
	void cardDeclarativeAndChainedBindings() {
		Signal<Float> widthSignal = Signal.of(250f);
		Signal<Color> colorSignal = Signal.of(Color.scarlet);

		Card c = Ui.card()
				.height(180f)
				.width(widthSignal)
				.color(colorSignal)
				.padding(12f)
				.children(() -> {
					ParentStack.add(new Element());
					ParentStack.add(new Element());
				});

		assertEquals(2, c.container().getChildren().size, "Card container must have 2 children");
		assertEquals(180f, c.cardButton().getPrefHeight(), 0.01f);
		assertEquals(250f, c.cardButton().getPrefWidth(), 0.01f);
		assertEquals(Color.scarlet, c.cardButton().color);

		widthSignal.set(300f);
		assertEquals(300f, c.cardButton().getPrefWidth(), 0.01f);

		colorSignal.set(Color.green);
		assertEquals(Color.green, c.cardButton().color);

		c.dispose();
	}

	@Test
	void cardClickAndChildEventIsolation() {
		boolean[] cardClicked = {false};
		Card c = Ui.card().onClick(() -> cardClicked[0] = true);

		InputEvent stoppedEvent = new InputEvent();
		stoppedEvent.stop();
		c.cardButton().getListeners().forEach(listener -> {
			if (listener instanceof ClickListener) {
				((ClickListener) listener).clicked(stoppedEvent, 0f, 0f);
			}
		});
		assertFalse(cardClicked[0], "Card onClick should not execute when event is stopped");

		InputEvent normalEvent = new InputEvent();
		c.cardButton().getListeners().forEach(listener -> {
			if (listener instanceof ClickListener) {
				((ClickListener) listener).clicked(normalEvent, 0f, 0f);
			}
		});
		assertTrue(cardClicked[0], "Card onClick should execute for normal events");

		c.dispose();
	}

	@Test
	void spacerExpandsInRowAndColumn() {
		Row row = Ui.row(() -> {
			Ui.spacer();
		});
		Cell<?> rowCell = row.table().getCells().first();
		assertTrue(CellAccess.expandX(rowCell) > 0, "Spacer in Row must grow horizontally");

		Column col = Ui.column(() -> {
			Ui.spacer();
		});
		Cell<?> colCell = col.table().getCells().first();
		assertTrue(CellAccess.expandY(colCell) > 0, "Spacer in Column must grow vertically");
	}
}
