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
        Assumptions.assumeTrue(Core.app != null, "Arc Core.app is null; skipping headless-dependent tests");
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
