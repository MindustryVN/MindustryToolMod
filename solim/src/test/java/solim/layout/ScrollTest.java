package solim.layout;

import static org.junit.jupiter.api.Assertions.*;

import arc.Core;
import arc.mock.MockApplication;
import arc.mock.MockGraphics;
import arc.scene.Element;
import arc.scene.event.EventListener;
import arc.scene.event.InputListener;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import solim.overlay.Hud;
import solim.ui.ParentStack;

class ScrollTest {

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
	void scrollCreatesElement() {
		Scroll s = new Scroll();
		assertNotNull(s.element());
		assertNotNull(s.outer());
		assertNotNull(s.content());
	}

	@Test
	void scrollAddsChild() {
		Scroll s = new Scroll();
		Element e = new Element();
		s.add(e);
		assertEquals(1, s.content().getChildren().size);
	}

	@Test
	void scrollMultipleChildren() {
		Scroll s = new Scroll();
		for (int i = 0; i < 5; i++) {
			s.add(new Element());
		}
		assertEquals(5, s.content().getChildren().size);
	}

	@Test
	void scrollChildrenRunnable() {
		Scroll s = new Scroll();
		s.children(() -> {
			ParentStack.add(new Element());
			ParentStack.add(new Element());
		});
		assertEquals(2, s.content().getChildren().size);
	}

	@Test
	void scrollVisibleModifier() {
		Scroll s = new Scroll();
		s.visible(false);
		assertFalse(s.outer().visible);
		s.visible(true);
		assertTrue(s.outer().visible);
	}

	@Test
	void scrollPositionModifiers() {
		Scroll s = new Scroll();
		s.x(10f);
		assertEquals(10f, s.outer().x, 0.01f);
		s.y(20f);
		assertEquals(20f, s.outer().y, 0.01f);
		s.position(30f, 40f);
		assertEquals(30f, s.outer().x, 0.01f);
		assertEquals(40f, s.outer().y, 0.01f);
	}

	@Test
	void scrollNameModifier() {
		Scroll s = new Scroll();
		s.name("my-scroll");
		assertEquals("my-scroll", s.outer().name);
	}

	@Test
	void scrollCenterLeftRight() {
		Scroll s = new Scroll();
		s.center();
		s.left();
		s.right();
		assertNotNull(s.outer());
	}

	@Test
	void scrollImplementsComponent() {
		Scroll s = new Scroll();
		assertInstanceOf(solim.core.Component.class, s);
	}

	@Test
	void scrollSizeConstraints() {
		Scroll s = new Scroll();
		assertNotNull(s.sizeConstraints());
	}

	@Test
	void scrollScrollToTopAndBottom() {
		Scroll s = new Scroll();
		assertDoesNotThrow(s::scrollToTop);
		assertDoesNotThrow(s::scrollToBottom);
	}

	@Test
	void scrollDisabledConfiguration() {
		Scroll scroll = new Scroll();
		scroll.scrollingDisabled(false, true);
		assertFalse(scroll.isScrollingDisabledX());
		assertTrue(scroll.isScrollingDisabledY());

		scroll.scrollX(true);
		assertFalse(scroll.isScrollingDisabledX());

		scroll.scrollX(false);
		assertTrue(scroll.isScrollingDisabledX());

		scroll.scrollY(true);
		assertFalse(scroll.isScrollingDisabledY());

		scroll.scrollY(false);
		assertTrue(scroll.isScrollingDisabledY());
	}

	@Test
	void scrollPercentagesAndDirections() {
		Scroll scroll = new Scroll();
		assertDoesNotThrow(() -> {
			scroll.scrollToBottom();
			scroll.scrollToTop();
			scroll.scrollPercentX(0.5f);
			scroll.scrollPercentY(0.75f);
		});
	}

	@Test
	void hudToFrontOnTouchAndMount() {
		Hud hud = new Hud();
		hud.toFrontOnTouch();

		InputListener listener = null;
		for (EventListener l : hud.root().getListeners()) {
			if (l instanceof InputListener) {
				listener = (InputListener) l;
				break;
			}
		}
		assertNotNull(listener, "toFrontOnTouch should register an InputListener on hud.root()");

		arc.scene.ui.layout.Table parent = new arc.scene.ui.layout.Table();
		hud.mount(parent);
		assertTrue(parent.getChildren().contains(hud.root()), "HUD root should be added to parent group");

		hud.dispose();
	}
}
