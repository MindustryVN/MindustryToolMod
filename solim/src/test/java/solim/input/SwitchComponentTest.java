package solim.input;

import static org.junit.jupiter.api.Assertions.*;

import arc.Core;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import solim.signal.Signal;

class SwitchComponentTest {

	@BeforeAll
	static void checkArcContext() {
		Assumptions.assumeTrue(Core.scene != null, "Arc Core.scene is null; skipping skin-dependent tests");
	}

	@Test
	void switchCreatesElement() {
		Switch sw = new Switch(Signal.of(false));
		assertNotNull(sw.element());
		assertNotNull(sw.button());
		sw.dispose();
	}

	@Test
	void switchBinding() {
		Signal<Boolean> on = Signal.of(false);
		Switch sw = new Switch(on);
		assertEquals("OFF", sw.button().getText());
		on.set(true);
		assertEquals("ON", sw.button().getText());
		sw.dispose();
	}

	@Test
	void switchNameModifier() {
		Switch sw = new Switch(Signal.of(false)).name("sw");
		assertEquals("sw", sw.element().name);
		sw.dispose();
	}

	@Test
	void switchImplementsComponent() {
		Switch sw = new Switch(Signal.of(false));
		assertInstanceOf(solim.core.Component.class, sw);
		sw.dispose();
	}

	@Test
	void switchStaticFactory() {
		Switch sw = Switch.of(Signal.of(true));
		assertEquals("ON", sw.button().getText());
		sw.dispose();
	}
}
