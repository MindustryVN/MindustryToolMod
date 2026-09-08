package solim.ui;

import static org.junit.jupiter.api.Assertions.*;
import static solim.ui.Ui.card;

import arc.scene.Element;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.CellAccess;
import java.util.*;
import org.junit.jupiter.api.Test;
import solim.core.BaseComponent;
import solim.layout.ReactiveGrid;
import solim.signal.Signal;

class StructuralReactivityTest {

	static class TestComponent extends BaseComponent {
		final String id;
		boolean wasDisposed = false;

		TestComponent(String id) {
			this.id = id;
		}

		@Override
		protected Element build() {
			return new Element();
		}

		@Override
		protected void onDispose() {
			wasDisposed = true;
		}
	}

	@Test
	void testDynamicSubtreeReplacementAndDisposal() {
		Signal<Boolean> toggle = Signal.of(true);
		Map<String, TestComponent> instances = new HashMap<>();

		Dynamic<Boolean> dyn = new Dynamic<>(toggle, val -> {
			TestComponent tc = new TestComponent(val ? "A" : "B");
			instances.put(tc.id, tc);
			return tc;
		});

		Element el = dyn.element();
		assertNotNull(el);
		assertEquals(1, dyn.container().getChildren().size);
		TestComponent compA = instances.get("A");
		assertNotNull(compA);
		assertFalse(compA.wasDisposed);

		// Switch dynamic subtree
		toggle.set(false);
		assertTrue(compA.wasDisposed, "Previous component must be disposed on change");
		TestComponent compB = instances.get("B");
		assertNotNull(compB);
		assertFalse(compB.wasDisposed);
		assertEquals(1, dyn.container().getChildren().size);

		// Dispose dynamic component
		dyn.dispose();
		assertTrue(compB.wasDisposed, "Active component must be disposed when Dynamic is disposed");
	}

	@Test
	void testForEachKeyedReuseAndDisposal() {
		Signal<List<String>> items = Signal.of(Arrays.asList("A", "B", "C"));
		Map<String, TestComponent> created = new HashMap<>();

		ForEach<String, String> fe = new ForEach<>(items, id -> id, id -> {
			TestComponent tc = new TestComponent(id);
			created.put(id, tc);
			return tc;
		});

		assertNotNull(fe.element());
		assertEquals(3, fe.container().getChildren().size);

		TestComponent a = created.get("A");
		TestComponent b = created.get("B");
		TestComponent c = created.get("C");
		assertNotNull(a);
		assertNotNull(b);
		assertNotNull(c);

		// Update items to [B, C, D]
		items.set(Arrays.asList("B", "C", "D"));

		assertTrue(a.wasDisposed, "Removed item A must be disposed");
		assertFalse(b.wasDisposed, "Retained item B must not be disposed");
		assertFalse(c.wasDisposed, "Retained item C must not be disposed");

		TestComponent d = created.get("D");
		assertNotNull(d, "New item D must be created");
		assertFalse(d.wasDisposed);

		// Total children should now be 3
		assertEquals(3, fe.container().getChildren().size);

		// Dispose ForEach
		fe.dispose();
		assertTrue(b.wasDisposed);
		assertTrue(c.wasDisposed);
		assertTrue(d.wasDisposed);
	}

	@Test
	void testReactiveGridReflowsWithoutRecreatingComponents() {
		Signal<Integer> cols = Signal.of(3);
		Signal<List<String>> items = Signal.of(Arrays.asList("A", "B", "C", "D", "E"));
		Map<String, Integer> factoryCallCount = new HashMap<>();

		ReactiveGrid<String, String> grid = new ReactiveGrid<>(cols, items, id -> id, id -> {
			factoryCallCount.put(id, factoryCallCount.getOrDefault(id, 0) + 1);
			return new TestComponent(id);
		});

		assertNotNull(grid.element());
		assertEquals(5, grid.table().getChildren().size);
		assertEquals(1, factoryCallCount.get("A"));
		assertEquals(1, factoryCallCount.get("B"));
		assertEquals(1, factoryCallCount.get("C"));
		assertEquals(1, factoryCallCount.get("D"));
		assertEquals(1, factoryCallCount.get("E"));

		// Change column count: 3 -> 2
		cols.set(2);

		// Columns changed, but factory must NOT have been called again!
		assertEquals(1, factoryCallCount.get("A"), "Component A must be reused without factory re-invocation");
		assertEquals(1, factoryCallCount.get("B"), "Component B must be reused without factory re-invocation");
		assertEquals(1, factoryCallCount.get("C"), "Component C must be reused without factory re-invocation");
		assertEquals(1, factoryCallCount.get("D"), "Component D must be reused without factory re-invocation");
		assertEquals(1, factoryCallCount.get("E"), "Component E must be reused without factory re-invocation");

		// Table still has 5 children reflowed
		assertEquals(5, grid.table().getChildren().size);

		grid.dispose();
		assertTrue(grid.isDisposed());
	}

	@Test
	void testForEachChildrenDoNotGrowByDefault() {
		Signal<List<String>> items = Signal.of(Arrays.asList("A"));
		ForEach<String, String> fe = new ForEach<>(items, id -> id, id -> new TestComponent(id));
		fe.element();
		Cell<?> cell = fe.container().getCells().first();
		assertEquals(0, CellAccess.expandX(cell), "ForEach item must not growX by default");
		assertEquals(0, CellAccess.expandY(cell), "ForEach item must not growY by default");
		fe.dispose();
	}

	@Test
	void testDynamicChildDoesNotGrowByDefault() {
		Signal<String> source = Signal.of("A");
		Dynamic<String> dyn = new Dynamic<>(source, val -> new TestComponent(val));
		dyn.element();
		Cell<?> cell = dyn.container().getCells().first();
		assertEquals(0, CellAccess.expandX(cell), "Dynamic item must not grow by default");
		assertEquals(0, CellAccess.expandY(cell), "Dynamic item must not grow by default");
		dyn.dispose();
	}

	@Test
	void testConstrainedChildGrowsInForEachAndDynamic() {
		class ConstrainedComp extends BaseComponent {
			@Override
			protected Element build() {
				return card().growX().element();
			}
		}

		Signal<List<String>> items = Signal.of(Arrays.asList("A"));
		ForEach<String, String> fe = new ForEach<>(items, id -> id, id -> new ConstrainedComp());
		fe.element();
		Cell<?> cell = fe.container().getCells().first();
		assertEquals(1, CellAccess.expandX(cell), "Constrained child in ForEach must growX");
		fe.dispose();

		Signal<String> source = Signal.of("A");
		Dynamic<String> dyn = new Dynamic<>(source, val -> new ConstrainedComp());
		dyn.element();
		Cell<?> dynCell = dyn.container().getCells().first();
		assertEquals(1, CellAccess.expandX(dynCell), "Constrained child in Dynamic must growX");
		dyn.dispose();
	}
}
