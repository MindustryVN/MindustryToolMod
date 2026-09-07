package solim.style;

import arc.graphics.Color;
import org.junit.jupiter.api.Test;
import solim.signal.Effect;
import solim.signal.Signal;
import static org.junit.jupiter.api.Assertions.*;

class StyleTest {

    static class Target {
        Style current;
        public void setStyle(Style s) { this.current = s; }
    }

    @Test
    void immutability() {
        Style s1 = Style.builder().foreground(Color.white).background(Color.red).build();
        Style s2 = s1.withPad(16f);
        // s1 unchanged
        assertNotEquals(s1.padding(), s2.padding());
        assertNotSame(s1, s2);
    }

    @Test
    void builderAfterBuildDoesNotAffectInstance() {
        Style.Builder b = Style.builder().foreground(Color.white);
        Style s = b.build();
        b.foreground(Color.red);
        // s is still white
        assertEquals(Color.white, s.foreground());
    }

    @Test
    void constantsExist() {
        assertNotNull(Styles.PRIMARY);
        assertNotNull(Styles.GHOST);
        assertNotNull(Styles.BLACK6);
        assertTrue(Styles.PRIMARY.primary());
        assertTrue(Styles.GHOST.ghost());
    }

    @Test
    void staticStyleApply() {
        Target t = new Target();
        StyleBinding.apply(t, Styles.PRIMARY, (target, style) -> target.setStyle(style));
        assertSame(Styles.PRIMARY, t.current);
    }

    @Test
    void reactiveStyleToggle() {
        Signal<Boolean> dark = Signal.of(false);
        Target t = new Target();
        Effect b = StyleBinding.bind(dark.map(v -> v ? Styles.PRIMARY : Styles.GHOST), t, (target, style) -> target.setStyle(style));
        assertSame(Styles.GHOST, t.current);
        dark.set(true);
        assertSame(Styles.PRIMARY, t.current);
        dark.set(false);
        assertSame(Styles.GHOST, t.current);
        b.dispose();
        dark.set(true);
        assertSame(Styles.GHOST, t.current, "Disposed binding should not update");
    }

    @Test
    void reactiveDisposeStops() {
        Signal<Style> s = Signal.of(Styles.PRIMARY);
        Target t = new Target();
        Effect b = StyleBinding.bind(s, t, (target, style) -> target.setStyle(style));
        assertSame(Styles.PRIMARY, t.current);
        b.dispose();
        s.set(Styles.GHOST);
        assertSame(Styles.PRIMARY, t.current);
    }

    @Test
    void composeFromBase() {
        Style custom = Styles.PRIMARY.withPad(16f).withBackground(Color.green);
        assertNotSame(Styles.PRIMARY, custom);
        assertEquals(16f, custom.padding());
        assertEquals(Color.green, custom.background());
        // base unchanged
        assertNotEquals(16f, Styles.PRIMARY.padding());
    }

    @Test
    void noCssEngine() throws Exception {
        java.nio.file.Path styleDir = java.nio.file.Path.of("solim/src/solim/style");
        if (!java.nio.file.Files.exists(styleDir)) {
            styleDir = java.nio.file.Path.of("src/solim/style");
        }
        if (java.nio.file.Files.exists(styleDir)) {
            try (var stream = java.nio.file.Files.walk(styleDir)) {
                for (var p : (Iterable<java.nio.file.Path>) stream::iterator) {
                    if (p.toString().endsWith(".java")) {
                        String content = new String(java.nio.file.Files.readAllBytes(p), java.nio.charset.StandardCharsets.UTF_8);
                        assertFalse(content.contains("stylesheet"), "No CSS stylesheet");
                        assertFalse(content.contains("selector"), "No CSS selector");
                        assertFalse(content.contains("flex"), "No CSS flex");
                    }
                }
            }
        }
    }
}
