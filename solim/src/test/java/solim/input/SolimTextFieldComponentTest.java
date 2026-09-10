package solim.input;

import static org.junit.jupiter.api.Assertions.*;

import arc.Core;
import arc.input.KeyCode;
import arc.scene.event.InputEvent;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import solim.signal.Signal;

class SolimTextFieldComponentTest {

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
void signalUpdatesFieldText() {
    Signal<String> input = Signal.of("");
    SolimTextField tf = new SolimTextField(input);

    assertEquals("", tf.field().getText());

    input.set("hello");
    assertEquals("hello", tf.field().getText());
    tf.dispose();
}

@Test
void fieldTextUpdatesSignal() {
    Signal<String> input = Signal.of("a");
    SolimTextField tf = new SolimTextField(input);

    tf.field().setText("b");
    assertEquals("b", input.get());
    tf.dispose();
}

@Test
void enterKeyTriggersSubmission() {
    Signal<String> text = Signal.of("hello world");
    SolimTextField tf = new SolimTextField(text);
    String[] submitted = {null};
    tf.onEnter(submittedText -> submitted[0] = submittedText);

    simulateKey(tf, KeyCode.enter);
    assertEquals("hello world", submitted[0]);
    tf.dispose();
}

@Test
void disabledStateTogglesFieldDisabled() {
    Signal<String> text = Signal.of("blocked");
    SolimTextField tf = new SolimTextField(text);

    tf.disabled(true);
    assertTrue(tf.field().isDisabled());

    tf.disabled(false);
    assertFalse(tf.field().isDisabled());
    tf.dispose();
}

@Test
void reactiveDisabledUpdatesFieldDisabled() {
    Signal<Boolean> disabled = Signal.of(false);
    SolimTextField tf = new SolimTextField(Signal.of("test")).disabled(disabled);

    assertFalse(tf.field().isDisabled());

    disabled.set(true);
    assertTrue(tf.field().isDisabled());

    disabled.set(false);
    assertFalse(tf.field().isDisabled());
    tf.dispose();
}

@Test
void validatorUpdatesIsValid() {
    Signal<String> text = Signal.of("abc");
    SolimTextField tf = new SolimTextField(text);
    tf.validator(s -> s != null && s.length() >= 3);

    assertTrue(tf.isValid());

    text.set("ab");
    assertFalse(tf.isValid());

    text.set("abcd");
    assertTrue(tf.isValid());
    tf.dispose();
}

@Test
void nameModifierUpdatesElementName() {
    SolimTextField tf = new SolimTextField(Signal.of("")).name("tf");
    assertEquals("tf", tf.element().name);
    tf.dispose();
}

@Test
void elementIsSameAsField() {
    SolimTextField tf = new SolimTextField(Signal.of(""));
    assertSame(tf.field(), tf.element());
    tf.dispose();
}
}
