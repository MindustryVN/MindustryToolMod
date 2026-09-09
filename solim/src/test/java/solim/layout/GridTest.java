package solim.layout;

import static org.junit.jupiter.api.Assertions.*;

import arc.Core;
import arc.mock.MockApplication;
import arc.mock.MockGraphics;
import arc.scene.Element;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import solim.signal.Signal;
import solim.ui.ParentStack;

class GridTest {

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
	void gridCreatesTable() {
		Grid g = new Grid();
		assertNotNull(g.element());
		assertNotNull(g.table());
	}

	@Test
	void gridWithColumnCount() {
		Grid g = new Grid(3);
		assertNotNull(g.table());
	}

	@Test
	void gridAddsChildElements() {
		Grid g = new Grid(3);
		for (int i = 0; i < 4; i++) {
			g.add(new Element());
		}
		assertEquals(4, g.table().getChildren().size);
	}

	@Test
	void gridGapModifier() {
		Grid g = new Grid(2).gap(16f);
		assertNotNull(g.table());
	}

	@Test
	void gridColumnsModifier() {
		Grid g = new Grid(2);
		g.columns(4);
		assertNotNull(g.table());
	}

	@Test
	void gridReactiveColumns() {
		Signal<Integer> cols = Signal.of(2);
		Grid g = new Grid();
		g.columns(cols);
		assertNotNull(g.table());
		cols.set(3);
		assertNotNull(g.table());
	}

	@Test
	void gridReactiveGap() {
		Signal<Float> gap = Signal.of(8f);
		Grid g = new Grid(2);
		g.gap(gap);
		assertNotNull(g.table());
		gap.set(16f);
		assertNotNull(g.table());
	}

	@Test
	void gridChildrenRunnable() {
		Grid g = new Grid(2);
		g.children(() -> {
			ParentStack.add(new Element());
			ParentStack.add(new Element());
		});
		assertEquals(2, g.table().getChildren().size);
	}

	@Test
	void gridImplementsComponent() {
		Grid g = new Grid();
		assertInstanceOf(solim.core.Component.class, g);
	}

	@Test
	void gridSizeConstraints() {
		Grid g = new Grid();
		assertNotNull(g.sizeConstraints());
	}

	@Test
	void gridZeroArgConstructor() {
		Grid g = new Grid();
		g.gap(12f);
		g.children(() -> {
			ParentStack.add(new Element());
		});
		assertEquals(1, g.table().getCells().size);
	}
}
