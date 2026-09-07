package solim.input;

import arc.Core;
import arc.scene.event.ClickListener;
import arc.scene.event.InputEvent;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import solim.signal.Signal;

import static org.junit.jupiter.api.Assertions.*;

class IconButtonTest {

    @BeforeAll
    static void checkArcContext() {
        Assumptions.assumeTrue(Core.scene != null, "Arc Core.scene is null; skipping skin-dependent tests");
    }

    @Test
    void iconButtonSizeAndEventPropagation() {
        boolean[] clicked = {false};
        IconButton btn = new IconButton()
                .size(32f)
                .onClick(() -> clicked[0] = true);

        assertEquals(32f, btn.imageButton().getPrefWidth(), 0.01f);
        assertEquals(32f, btn.imageButton().getPrefHeight(), 0.01f);

        InputEvent event = new InputEvent();
        btn.imageButton().getListeners().forEach(listener -> {
            if (listener instanceof ClickListener) {
                ((ClickListener) listener).clicked(event, 0f, 0f);
            }
        });

        assertTrue(clicked[0], "IconButton onClick should have executed");
        assertTrue(event.stopped, "IconButton should stop event propagation by default");

        btn.dispose();
    }

    @Test
    void iconButtonReactivity() {
        Signal<Boolean> enabled = Signal.of(true);
        Signal<Boolean> visible = Signal.of(true);

        IconButton btn = new IconButton()
                .enabled(enabled)
                .visible(visible);

        assertFalse(btn.imageButton().isDisabled());
        assertTrue(btn.imageButton().visible);

        enabled.set(false);
        assertTrue(btn.imageButton().isDisabled());

        visible.set(false);
        assertFalse(btn.imageButton().visible);

        btn.dispose();
    }
}
