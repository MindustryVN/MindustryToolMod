package solim.overlay;

import arc.Core;
import arc.scene.ui.layout.Table;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import solim.feedback.Alert;
import solim.feedback.Avatar;
import solim.feedback.Badge;
import solim.feedback.ProgressBar;
import solim.feedback.Spinner;
import solim.signal.Computed;
import solim.signal.Signal;
import solim.ui.ParentStack;
import static org.junit.jupiter.api.Assertions.*;

class OverlayFeedbackTest {

    @BeforeAll
    static void checkArcContext() {
        Assumptions.assumeTrue(Core.scene != null, "Arc Core.scene is null; skipping skin-dependent tests");
    }

    @Test
    void dialogShowHide() {
        SolimDialog d = SolimDialog.of("Confirm", () -> {
            ParentStack.add(new Table());
        });
        d.show();
        assertTrue(d.isShown());
        d.hide();
        assertFalse(d.isShown());
        d.dispose();
    }

    public static class TestDialogEvent {
        public final int code;
        public TestDialogEvent(int code) { this.code = code; }
    }

    @Test
    void dialogSignalCreationAndEventRecalculation() {
        SolimDialog d = new SolimDialog("Test");
        int[] counter = new int[]{10};
        java.util.List<Runnable> resizeCallbacks = new java.util.ArrayList<>();

        // Test callback-based signal creation (e.g. this.resized(callback))
        Signal<Integer> resizeSignal = d.createSignal(resizeCallbacks::add, () -> counter[0]);
        assertEquals(10, resizeSignal.get());

        counter[0] = 25;
        for (Runnable r : resizeCallbacks) r.run();
        assertEquals(25, resizeSignal.get());

        // Test event-based signal creation (Events.on)
        Signal<Integer> eventSignal = d.createSignal(TestDialogEvent.class, () -> counter[0] * 2);
        assertEquals(50, eventSignal.get());

        counter[0] = 30;
        arc.Events.fire(new TestDialogEvent(1));
        assertEquals(60, eventSignal.get());

        // Test event-mapping signal creation
        Signal<Integer> mappedSignal = d.createSignal(TestDialogEvent.class, e -> e.code * 100, 0);
        assertEquals(0, mappedSignal.get());
        arc.Events.fire(new TestDialogEvent(5));
        assertEquals(500, mappedSignal.get());

        // Test disposal cleans up listeners
        d.dispose();
        assertTrue(d.isDisposed());

        counter[0] = 999;
        arc.Events.fire(new TestDialogEvent(99));
        // After disposal, event listener was unregistered so eventSignal should not recalculate
        assertEquals(60, eventSignal.get());
        assertEquals(500, mappedSignal.get());
    }

    @Test
    void progressBarReactive() {
        Signal<Float> progress = Signal.of(0.3f);
        ProgressBar pb = new ProgressBar(progress);
        progress.set(0.7f);
        assertNotNull(pb.bar());
        pb.dispose();
    }

    @Test
    void alertDismiss() {
        Alert a = new Alert("Saved!", Alert.Type.SUCCESS);
        assertTrue(a.isShown());
        a.dismiss();
        assertFalse(a.isShown());
    }

    @Test
    void badgeReactiveCount() {
        Signal<Integer> count = Signal.of(5);
        Computed<String> text = count.map(v -> v > 99 ? "99+" : String.valueOf(v));
        Badge b = Badge.of(text);
        assertNotNull(b.table());
        count.set(50);
        text.get();
        b.dispose();
    }

    @Test
    void spinnerExists() {
        Spinner s = new Spinner();
        assertNotNull(s.label());
    }

    @Test
    void avatarExists() {
        Avatar a = new Avatar();
        assertNotNull(a.table());
    }

    @Test
    void popupExists() {
        Popup p = new Popup();
        assertNotNull(p.table());
    }
}
