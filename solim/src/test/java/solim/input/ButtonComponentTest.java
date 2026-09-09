package solim.input;

import static org.junit.jupiter.api.Assertions.*;

import arc.Core;
import arc.scene.Element;
import arc.scene.event.ClickListener;
import arc.scene.event.InputEvent;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import solim.signal.Signal;

class ButtonComponentTest {

	@BeforeAll
	static void checkArcContext() {
		Assumptions.assumeTrue(Core.scene != null, "Arc Core.scene is null; skipping skin-dependent tests");
	}

	@Test
	void buttonCreatesElement() {
		Button btn = new Button();
		assertNotNull(btn.element());
		assertNotNull(btn.sizedButton());
	}

	@Test
	void buttonWidthAndHeight() {
		Button btn = new Button().width(200f).height(80f);
		assertEquals(200f, btn.sizedButton().getPrefWidth(), 0.01f);
		assertEquals(80f, btn.sizedButton().getPrefHeight(), 0.01f);
		btn.dispose();
	}

	@Test
	void buttonSizeModifier() {
		Button btn = new Button().size(100f);
		assertEquals(100f, btn.sizedButton().getPrefWidth(), 0.01f);
		assertEquals(100f, btn.sizedButton().getPrefHeight(), 0.01f);
		btn.dispose();
	}

	@Test
	void buttonOnClickCallback() {
		boolean[] clicked = {false};
		Button btn = new Button(() -> clicked[0] = true);
		InputEvent event = new InputEvent();
		btn.sizedButton().getListeners().forEach(listener -> {
			if (listener instanceof ClickListener) {
				((ClickListener) listener).clicked(event, 0f, 0f);
			}
		});
		assertTrue(clicked[0]);
		btn.dispose();
	}

	@Test
	void buttonClickStopsPropagation() {
		Button btn = new Button(() -> {});
		InputEvent event = new InputEvent();
		btn.sizedButton().getListeners().forEach(listener -> {
			if (listener instanceof ClickListener) {
				((ClickListener) listener).clicked(event, 0f, 0f);
			}
		});
		assertTrue(event.stopped, "Button should stop event propagation");
		btn.dispose();
	}

	@Test
	void buttonDisabledState() {
		Button btn = new Button();
		assertFalse(btn.sizedButton().isDisabled());
		btn.sizedButton().setDisabled(true);
		assertTrue(btn.sizedButton().isDisabled());
		btn.dispose();
	}

	@Test
	void buttonReactiveEnabled() {
		Signal<Boolean> enabled = Signal.of(true);
		Button btn = new Button().enabled(enabled);
		assertFalse(btn.sizedButton().isDisabled());
		enabled.set(false);
		assertTrue(btn.sizedButton().isDisabled());
		btn.dispose();
	}

	@Test
	void buttonReactiveVisible() {
		Signal<Boolean> visible = Signal.of(true);
		Button btn = new Button().visible(visible);
		assertTrue(btn.sizedButton().visible);
		visible.set(false);
		assertFalse(btn.sizedButton().visible);
		btn.dispose();
	}

	@Test
	void buttonReactiveChecked() {
		Signal<Boolean> checked = Signal.of(false);
		Button btn = new Button().checked(checked);
		assertFalse(btn.sizedButton().isChecked());
		checked.set(true);
		assertTrue(btn.sizedButton().isChecked());
		btn.dispose();
	}

	@Test
	void buttonChildren() {
		Button btn = new Button().children(() -> {
			solim.ui.ParentStack.add(new Element());
		});
		assertEquals(1, btn.sizedButton().getChildren().size);
		btn.dispose();
	}

	@Test
	void buttonGrowModifiers() {
		Button btn = new Button();
		btn.growX();
		btn.growY();
		btn.grow();
		assertNotNull(btn.sizedButton());
		btn.dispose();
	}

	@Test
	void buttonPositionModifiers() {
		Button btn = new Button();
		btn.x(10f);
		assertEquals(10f, btn.sizedButton().x, 0.01f);
		btn.y(20f);
		assertEquals(20f, btn.sizedButton().y, 0.01f);
		btn.position(30f, 40f);
		assertEquals(30f, btn.sizedButton().x, 0.01f);
		assertEquals(40f, btn.sizedButton().y, 0.01f);
		btn.dispose();
	}

	@Test
	void buttonNameModifier() {
		Button btn = new Button();
		btn.name("my-button");
		assertEquals("my-button", btn.sizedButton().name);
		btn.dispose();
	}

	@Test
	void buttonImplementsComponent() {
		Button btn = new Button();
		assertInstanceOf(solim.core.Component.class, btn);
		btn.dispose();
	}

	@Test
	void buttonGapAndMargin() {
		Button btn = new Button();
		btn.gap(8f);
		btn.margin(4f);
		btn.margin(1f, 2f, 3f, 4f);
		assertNotNull(btn.sizedButton());
		btn.dispose();
	}

	@Test
	void buttonAlignment() {
		Button btn = new Button();
		btn.left();
		btn.right();
		btn.center();
		btn.top();
		btn.bottom();
		assertNotNull(btn.sizedButton());
		btn.dispose();
	}
}
