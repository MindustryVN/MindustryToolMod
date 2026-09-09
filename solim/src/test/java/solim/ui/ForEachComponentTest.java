package solim.ui;

import static org.junit.jupiter.api.Assertions.*;

import arc.Core;
import arc.scene.Element;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import solim.core.BaseComponent;
import solim.signal.Signal;

class ForEachComponentTest {

	@BeforeAll
	static void initArc() {
		if (Core.app == null) {
			Core.app = new arc.mock.MockApplication();
		}
		if (Core.graphics == null) {
			Core.graphics = new arc.mock.MockGraphics();
		}
	}

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
	void forEachCreatesElement() {
		Signal<List<String>> items = Signal.of(Arrays.asList("A", "B"));
		ForEach<String, String> fe = new ForEach<>(items, id -> id, id -> new TestComponent(id));
		assertNotNull(fe.element());
		fe.dispose();
	}

	@Test
	void forEachRendersItems() {
		Signal<List<String>> items = Signal.of(Arrays.asList("A", "B", "C"));
		ForEach<String, String> fe = new ForEach<>(items, id -> id, id -> new TestComponent(id));
		assertNotNull(fe.element());
		assertEquals(3, fe.container().getChildren().size);
		fe.dispose();
	}

	@Test
	void forEachKeyedReuse() {
		Signal<List<String>> items = Signal.of(Arrays.asList("A", "B", "C"));
		Map<String, TestComponent> created = new HashMap<>();
		ForEach<String, String> fe = new ForEach<>(items, id -> id, id -> {
			TestComponent tc = new TestComponent(id);
			created.put(id, tc);
			return tc;
		});
		fe.element();

		TestComponent a = created.get("A");
		assertNotNull(a);

		// Update items to [B, C, D]
		items.set(Arrays.asList("B", "C", "D"));
		assertTrue(a.wasDisposed, "Removed item A must be disposed");
		assertFalse(created.get("B").wasDisposed, "Retained item B must not be disposed");

		fe.dispose();
	}

	@Test
	void forEachDisposal() {
		Signal<List<String>> items = Signal.of(Arrays.asList("A", "B"));
		Map<String, TestComponent> created = new HashMap<>();
		ForEach<String, String> fe = new ForEach<>(items, id -> id, id -> {
			TestComponent tc = new TestComponent(id);
			created.put(id, tc);
			return tc;
		});
		fe.element();
		fe.dispose();
		assertTrue(created.get("A").wasDisposed);
		assertTrue(created.get("B").wasDisposed);
	}

	@Test
	void forEachChildrenDoNotGrowByDefault() {
		Signal<List<String>> items = Signal.of(Arrays.asList("A"));
		ForEach<String, String> fe = new ForEach<>(items, id -> id, id -> new TestComponent(id));
		fe.element();
		arc.scene.ui.layout.Cell<?> cell = fe.container().getCells().first();
		assertEquals(0, arc.scene.ui.layout.CellAccess.expandX(cell));
		assertEquals(0, arc.scene.ui.layout.CellAccess.expandY(cell));
		fe.dispose();
	}
}
