package solim.layout;

import static org.junit.jupiter.api.Assertions.*;

import arc.Core;
import arc.graphics.Color;
import arc.mock.MockApplication;
import arc.mock.MockGraphics;
import arc.scene.Element;
import arc.scene.event.ClickListener;
import arc.scene.event.InputEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import solim.signal.Signal;
import solim.ui.ParentStack;

class CardTest {

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
	void cardCreatesElement() {
		Card c = new Card();
		assertNotNull(c.element());
		assertNotNull(c.cardButton());
		assertNotNull(c.container());
	}

	@Test
	void cardWithBackground() {
		Card c = new Card();
		assertNotNull(c.cardButton());
	}

	@Test
	void cardChildren() {
		Card c = new Card();
		c.children(() -> {
			ParentStack.add(new Element());
			ParentStack.add(new Element());
		});
		assertEquals(2, c.container().getChildren().size);
		c.dispose();
	}

	@Test
	void cardOnClick() {
		boolean[] clicked = {false};
		Card c = new Card().onClick(() -> clicked[0] = true);
		InputEvent event = new InputEvent();
		c.cardButton().getListeners().forEach(listener -> {
			if (listener instanceof ClickListener) {
				((ClickListener) listener).clicked(event, 0f, 0f);
			}
		});
		assertTrue(clicked[0]);
		c.dispose();
	}

	@Test
	void cardOnClickStoppedEvent() {
		boolean[] clicked = {false};
		Card c = new Card().onClick(() -> clicked[0] = true);
		InputEvent stoppedEvent = new InputEvent();
		stoppedEvent.stop();
		c.cardButton().getListeners().forEach(listener -> {
			if (listener instanceof ClickListener) {
				((ClickListener) listener).clicked(stoppedEvent, 0f, 0f);
			}
		});
		assertFalse(clicked[0], "Card onClick should not execute when event is stopped");
		c.dispose();
	}

	@Test
	void cardColorModifier() {
		Card c = new Card();
		c.color(Color.scarlet);
		assertEquals(Color.scarlet, c.cardButton().color);
		c.dispose();
	}

	@Test
	void cardReactiveColor() {
		Signal<Color> colorSig = Signal.of(Color.green);
		Card c = new Card().color(colorSig);
		assertEquals(Color.green, c.cardButton().color);
		colorSig.set(Color.blue);
		assertEquals(Color.blue, c.cardButton().color);
		c.dispose();
	}

	@Test
	void cardReactiveWidth() {
		Signal<Float> widthSig = Signal.of(200f);
		Card c = new Card();
		c.width(widthSig);
		assertEquals(200f, c.cardButton().getPrefWidth(), 0.01f);
		widthSig.set(300f);
		assertEquals(300f, c.cardButton().getPrefWidth(), 0.01f);
		c.dispose();
	}

	@Test
	void cardReactiveHeight() {
		Signal<Float> heightSig = Signal.of(150f);
		Card c = new Card();
		c.height(heightSig);
		assertEquals(150f, c.cardButton().getPrefHeight(), 0.01f);
		heightSig.set(200f);
		assertEquals(200f, c.cardButton().getPrefHeight(), 0.01f);
		c.dispose();
	}

	@Test
	void cardVisibleModifier() {
		Card c = new Card();
		c.visible(false);
		assertFalse(c.cardButton().visible);
		c.visible(true);
		assertTrue(c.cardButton().visible);
		c.dispose();
	}

	@Test
	void cardReactiveVisible() {
		Signal<Boolean> vis = Signal.of(true);
		Card c = new Card().visible(vis);
		assertTrue(c.cardButton().visible);
		vis.set(false);
		assertFalse(c.cardButton().visible);
		c.dispose();
	}

	@Test
	void cardPositionModifiers() {
		Card c = new Card();
		c.x(10f);
		assertEquals(10f, c.cardButton().x, 0.01f);
		c.y(20f);
		assertEquals(20f, c.cardButton().y, 0.01f);
		c.position(30f, 40f);
		assertEquals(30f, c.cardButton().x, 0.01f);
		assertEquals(40f, c.cardButton().y, 0.01f);
		c.dispose();
	}

	@Test
	void cardNameModifier() {
		Card c = new Card();
		c.name("my-card");
		assertEquals("my-card", c.cardButton().name);
		c.dispose();
	}

	@Test
	void cardGapModifier() {
		Card c = new Card();
		c.gap(8f);
		assertNotNull(c.container());
		c.dispose();
	}

	@Test
	void cardTopBottomLeftRightCenter() {
		Card c = new Card();
		c.top();
		c.bottom();
		c.left();
		c.right();
		c.center();
		assertNotNull(c.container());
		c.dispose();
	}

	@Test
	void cardImplementsComponent() {
		Card c = new Card();
		assertInstanceOf(solim.core.Component.class, c);
	}

	@Test
	void cardSizeConstraints() {
		Card c = new Card();
		assertNotNull(c.sizeConstraints());
		c.dispose();
	}
}
