package solim.input;

import static org.junit.jupiter.api.Assertions.*;

import arc.Core;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import solim.signal.Signal;

class CheckboxComponentTest {

	@BeforeAll
	static void checkArcContext() {
		Assumptions.assumeTrue(Core.scene != null, "Arc Core.scene is null; skipping skin-dependent tests");
	}

	@Test
	void checkboxCreatesElement() {
		Checkbox cb = new Checkbox("Test", Signal.of(false));
		assertNotNull(cb.element());
		assertNotNull(cb.checkBox());
		cb.dispose();
	}

	@Test
	void checkboxBinding() {
		Signal<Boolean> enabled = Signal.of(false);
		Checkbox cb = new Checkbox("Enable", enabled);
		assertFalse(cb.checkBox().isChecked());
		enabled.set(true);
		assertTrue(cb.checkBox().isChecked());
		cb.dispose();
	}

	@Test
	void checkboxCallback() {
		boolean[] toggled = {true};
		Checkbox cb = new Checkbox("Enable", true, val -> toggled[0] = val);
		assertTrue(cb.checkBox().isChecked());
		cb.checkBox().setChecked(false);
		assertFalse(toggled[0]);
		cb.dispose();
	}

	@Test
	void checkboxNameModifier() {
		Signal<Boolean> b = Signal.of(false);
		Checkbox cb = new Checkbox("Test", b).name("cb");
		assertEquals("cb", cb.element().name);
		cb.dispose();
	}

	@Test
	void checkboxGrowModifiers() {
		Signal<Boolean> b = Signal.of(false);
		Checkbox cb = new Checkbox("Test", b);
		cb.growX();
		cb.growY();
		cb.grow();
		assertNotNull(cb.element());
		cb.dispose();
	}

	@Test
	void checkboxImplementsComponent() {
		Checkbox cb = new Checkbox("Test", Signal.of(false));
		assertInstanceOf(solim.core.Component.class, cb);
		cb.dispose();
	}
}
