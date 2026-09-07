package solim.display;

import arc.Core;
import arc.scene.style.Drawable;
import arc.scene.ui.layout.Table;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import solim.signal.Computed;
import solim.signal.Signal;
import solim.ui.ParentStack;
import static org.junit.jupiter.api.Assertions.*;

class DisplayTest {

    @BeforeAll
    static void checkArcContext() {
        Assumptions.assumeTrue(Core.scene != null, "Arc Core.scene is null; skipping skin/atlas-dependent tests");
    }

    @Test
    void staticText() {
        Table root = new Table();
        ParentStack.push(root);
        ParentStack.add(new Text("Hello").label());
        assertEquals(1, root.getChildren().size);
        ParentStack.pop();
    }

    @Test
    void reactiveTextViaComputed() {
        Signal<Integer> count = Signal.of(1);
        Computed<String> text = count.map(v -> "Count: " + v);
        Text t = Text.of(text);
        // initial compute
        text.get();
        assertEquals("Count: 1", t.label().getText().toString());
        count.set(2);
        text.get();
        assertEquals("Count: 2", t.label().getText().toString());
        t.dispose();
    }

    @Test
    void reactiveTextViaSignal() {
        Signal<String> s = Signal.of("a");
        Text t = Text.of(s);
        assertEquals("a", t.label().getText().toString());
        s.set("b");
        assertEquals("b", t.label().getText().toString());
        t.dispose();
    }

    @Test
    void imageDisplays() {
        SolimImage img = new SolimImage();
        assertNotNull(img.image());
    }

    @Test
    void iconDisplays() {
        Icon icon = new Icon();
        assertNotNull(icon.image());
    }

    @Test
    void reactiveIcon() {
        Signal<Drawable> s = Signal.of(null);
        Icon icon = Icon.of(s);
        assertNotNull(icon);
        icon.dispose();
    }

    @Test
    void textDisposeStopsBinding() {
        Signal<String> s = Signal.of("a");
        Text t = Text.of(s);
        assertEquals("a", t.label().getText().toString());
        t.dispose();
        s.set("b");
        assertEquals("a", t.label().getText().toString());
    }
}
