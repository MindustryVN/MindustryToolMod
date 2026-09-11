package solim;

import static org.junit.jupiter.api.Assertions.*;

import arc.scene.Element;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import solim.ui.ParentStack;

class ArcFacadeTest {

	@AfterEach
	void clear() {
		ParentStack.clear();
	}

	@Test
	void arcAttachesRawElement() {
		Table root = new Table();
		ParentStack.push(root);
		Label label = UI.arc(new Label("hi"));
		ParentStack.pop();
		assertEquals("hi", label.getText().toString());
		assertTrue(root.getChildren().contains(label, true));
	}

	@Test
	void arcReturnsSameInstance() {
		Element el = new Element();
		assertSame(el, UI.arc(el));
	}

	@Test
	void arcOutsideScopeIsNoOp() {
		Element el = UI.arc(new Element());
		assertNull(el.parent);
	}
}
