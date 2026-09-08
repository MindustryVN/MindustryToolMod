package solim.overlay;

import static org.junit.jupiter.api.Assertions.*;

import arc.Core;
import arc.Events;
import arc.input.KeyCode;
import arc.mock.MockApplication;
import arc.mock.MockGraphics;
import arc.scene.Element;
import arc.scene.event.EventListener;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.event.Touchable;
import mindustry.game.EventType.ResizeEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import solim.modifier.ElementModifiers;
import solim.signal.Signal;
import solim.ui.Ui;

class HudTest {

	static class ResizableMockGraphics extends MockGraphics {
		int width = 1024;
		int height = 768;

		@Override
		public int getWidth() {
			return width;
		}

		@Override
		public int getHeight() {
			return height;
		}
	}

	private static ResizableMockGraphics mockGraphics;

	@BeforeAll
	static void initCore() {
		if (Core.app == null) {
			Core.app = new MockApplication();
		}
		mockGraphics = new ResizableMockGraphics();
		Core.graphics = mockGraphics;
	}

	@BeforeEach
	void resetScreen() {
		mockGraphics.width = 1024;
		mockGraphics.height = 768;
	}

	@Test
	void hudDefaultsAndTouchable() {
		Hud hud = Ui.hud();
		assertNotNull(hud.element());
		assertEquals(Touchable.childrenOnly, hud.element().touchable, "HUD root should allow touch pass-through");
		assertEquals(Touchable.enabled, hud.container().touchable, "HUD container should be enabled for touches");
		hud.dispose();
	}

	@Test
	void keepInScreenClamping() {
		Hud hud = new Hud();
		hud.element().setSize(200f, 100f);

		// Position far outside bottom-right
		hud.position(1200f, 900f);
		hud.keepInScreen();

		assertEquals(824f, hud.element().x, 0.01f, "X should clamp to 1024 - 200 = 824");
		assertEquals(668f, hud.element().y, 0.01f, "Y should clamp to 768 - 100 = 668");

		// Position outside top-left
		hud.position(-50f, -30f);
		hud.keepInScreen();

		assertEquals(0f, hud.element().x, 0.01f, "X should clamp to 0");
		assertEquals(0f, hud.element().y, 0.01f, "Y should clamp to 0");

		hud.dispose();
	}

	@Test
	void resizeEventClamping() {
		Hud hud = new Hud();
		hud.element().setSize(200f, 100f);
		hud.position(800f, 600f);
		hud.keepInScreen();

		assertEquals(800f, hud.element().x, 0.01f);
		assertEquals(600f, hud.element().y, 0.01f);

		// Shrink screen viewport
		mockGraphics.width = 800;
		mockGraphics.height = 600;

		Events.fire(new ResizeEvent());

		assertEquals(600f, hud.element().x, 0.01f, "X should clamp to 800 - 200 = 600 on resize");
		assertEquals(500f, hud.element().y, 0.01f, "Y should clamp to 600 - 100 = 500 on resize");

		hud.dispose();
	}

	@Test
	void draggableMovementAndClamping() {
		Hud hud = new Hud();
		hud.element().setSize(200f, 100f);
		hud.position(100f, 100f);

		Signal<Float> xSig = Signal.of(100f);
		Signal<Float> ySig = Signal.of(100f);

		Element handle = new Element();
		hud.draggable(handle, xSig, ySig);

		InputListener dragListener = null;
		for (EventListener l : handle.getListeners()) {
			if (l instanceof InputListener) {
				dragListener = (InputListener) l;
				break;
			}
		}
		assertNotNull(dragListener, "Handle should have InputListener registered");

		InputEvent event = new InputEvent();
		dragListener.touchDown(event, 10f, 10f, 0, KeyCode.mouseLeft);

		// Drag by +50 in X and +70 in Y
		dragListener.touchDragged(event, 60f, 80f, 0);

		assertEquals(150f, hud.element().x, 0.01f, "HUD X should move by +50");
		assertEquals(170f, hud.element().y, 0.01f, "HUD Y should move by +70");
		assertEquals(150f, xSig.get(), 0.01f, "X signal should sync with HUD X");
		assertEquals(170f, ySig.get(), 0.01f, "Y signal should sync with HUD Y");

		// Drag past screen edge
		dragListener.touchDragged(event, 2000f, 2000f, 0);

		assertEquals(824f, hud.element().x, 0.01f, "HUD X should clamp to 824");
		assertEquals(668f, hud.element().y, 0.01f, "HUD Y should clamp to 668");
		assertEquals(824f, xSig.get(), 0.01f, "X signal should clamp to 824");
		assertEquals(668f, ySig.get(), 0.01f, "Y signal should clamp to 668");

		hud.dispose();
	}

	@Test
	void reactiveSignalsBinding() {
		Hud hud = new Hud();
		Signal<Float> x = Signal.of(40f);
		Signal<Float> y = Signal.of(60f);
		Signal<Float> opacity = Signal.of(0.7f);

		hud.x(x).y(y).opacity(opacity);

		assertEquals(40f, hud.element().x, 0.01f);
		assertEquals(60f, hud.element().y, 0.01f);
		assertEquals(0.7f, hud.container().color.a, 0.01f);

		x.set(150f);
		y.set(250f);
		opacity.set(0.3f);

		assertEquals(150f, hud.element().x, 0.01f);
		assertEquals(250f, hud.element().y, 0.01f);
		assertEquals(0.3f, hud.container().color.a, 0.01f);

		hud.dispose();
	}
}
