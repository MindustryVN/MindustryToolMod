package solim.input;

import static org.junit.jupiter.api.Assertions.*;

import arc.Core;
import arc.input.KeyCode;
import arc.scene.event.InputEvent;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import solim.signal.Signal;

class SolimTextFieldTest {

	@BeforeAll
	static void checkArcContext() {
		Assumptions.assumeTrue(Core.scene != null, "Arc Core.scene is null; skipping skin-dependent tests");
	}

	private void simulateKey(SolimTextField tf, KeyCode key) {
		InputEvent event = new InputEvent();
		event.type = InputEvent.InputEventType.keyDown;
		event.keyCode = key;
		tf.field().getListeners().each(l -> l.handle(event));
	}

	@Test
	void enterKeySubmission() {
		Signal<String> text = Signal.of("hello world");
		SolimTextField tf = new SolimTextField(text);

		String[] submitted = {null};
		tf.onEnter(submittedText -> submitted[0] = submittedText);

		// Non-enter key should do nothing
		simulateKey(tf, KeyCode.space);
		assertNull(submitted[0]);

		// Enter key should submit text
		simulateKey(tf, KeyCode.enter);
		assertEquals("hello world", submitted[0]);

		// Update text and submit again
		text.set("updated text");
		simulateKey(tf, KeyCode.enter);
		assertEquals("updated text", submitted[0]);

		tf.dispose();
	}

	@Test
	void enterKeyDisabledIgnored() {
		Signal<String> text = Signal.of("blocked");
		SolimTextField tf = new SolimTextField(text);

		boolean[] submitted = {false};
		tf.onEnter(() -> submitted[0] = true);

		tf.disabled(true);
		assertTrue(tf.field().isDisabled());

		simulateKey(tf, KeyCode.enter);
		assertFalse(submitted[0], "Disabled text field must not submit on enter");

		tf.disabled(false);
		assertFalse(tf.field().isDisabled());

		simulateKey(tf, KeyCode.enter);
		assertTrue(submitted[0], "Enabled text field should submit on enter");

		tf.dispose();
	}

	@Test
	void validationFeedback() {
		Signal<String> text = Signal.of("abc");
		SolimTextField tf = new SolimTextField(text);

		// Validates length >= 3
		tf.validator(s -> s != null && s.length() >= 3);
		assertTrue(tf.isValid());
		assertTrue(tf.valid().get());

		// Signal update that fails validation
		text.set("ab");
		assertFalse(tf.isValid());
		assertFalse(tf.valid().get());

		// Signal update that passes validation
		text.set("abcd");
		assertTrue(tf.isValid());
		assertTrue(tf.valid().get());

		tf.dispose();
	}

	@Test
	void reactiveDisabledBinding() {
		Signal<Boolean> disabled = Signal.of(false);
		SolimTextField tf = new SolimTextField(Signal.of("test")).disabled(disabled);

		assertFalse(tf.field().isDisabled());

		disabled.set(true);
		assertTrue(tf.field().isDisabled());

		disabled.set(false);
		assertFalse(tf.field().isDisabled());

		tf.dispose();
	}
}