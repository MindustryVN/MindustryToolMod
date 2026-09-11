package solim.core;

import static org.junit.jupiter.api.Assertions.*;

import arc.scene.Element;
import arc.scene.ui.layout.Table;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import solim.ui.ParentStack;
import solim.ui.Ui;

class BaseComponentAutoAttachTest {

	static class Probe extends BaseComponent {
		@Override
		protected Element build() {
			return new Element();
		}
	}

	@AfterEach
	void clear() {
		ParentStack.clear();
		ComponentContext.clear();
	}

	@Test
	void constructsInScopeAutoAttaches() {
		Table root = new Table();
		ParentStack.push(root);
		Probe probe = new Probe();
		ParentStack.pop();
		assertEquals(1, root.getChildren().size);
		assertTrue(root.getChildren().contains(probe.element(), true));
	}

	@Test
	void nestedScopeAttachesToInnerParent() {
		Table root = new Table();
		ParentStack.push(root);
		final Probe[] held = new Probe[1];
		var col = Ui.column().children(() -> {
			held[0] = new Probe();
		});
		ParentStack.pop();
		assertEquals(1, root.getChildren().size);
		assertEquals(1, col.table().getChildren().size);
		assertSame(held[0].element(), col.table().getChildren().get(0));
	}

	@Test
	void constructsOutsideScopeAttachesNothing() {
		assertNull(ParentStack.current());
		Probe probe = new Probe();
		assertNull(probe.element().parent);
		assertEquals(0, ParentStack.size());
	}

	@Test
	void explicitComponentCallDoesNotDuplicate() {
		Table root = new Table();
		ParentStack.push(root);
		Probe probe = new Probe();
		ParentStack.attachToParent(ParentStack.isolate(probe::element));
		ParentStack.pop();
		assertEquals(1, root.getChildren().size);
		assertTrue(root.getChildren().contains(probe.element(), true));
	}
}
