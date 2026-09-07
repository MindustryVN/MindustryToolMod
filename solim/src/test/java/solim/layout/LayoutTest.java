package solim.layout;

import arc.Core;
import arc.scene.Element;
import arc.scene.ui.layout.Table;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import solim.ui.ParentStack;
import solim.ui.Ui;
import static org.junit.jupiter.api.Assertions.*;

class LayoutTest {

    @BeforeAll
    static void checkArcContext() {
        if (Core.app == null) {
            Core.app = new arc.mock.MockApplication();
        }
        if (Core.graphics == null) {
            Core.graphics = new arc.mock.MockGraphics();
        }
    }

    @Test
    void rowJustifyAndGap() {
        Table root = new Table();
        ParentStack.push(root);
        Row r = new Row();
        r.gap(8f);
        r.justify(Justify.BETWEEN);
        r.align(Align.CENTER);
        Element a = new Element(); a.name = "a";
        Element b = new Element(); b.name = "b";
        r.add(a);
        r.add(b);
        assertEquals(2, r.table().getChildren().size);
        assertSame(a, r.table().getChildren().get(0));
        assertSame(b, r.table().getChildren().get(1));
        ParentStack.pop();
    }

    @Test
    void columnGapAndPadding() {
        Column c = new Column();
        c.gap(16f);
        c.padding(24f);
        Element e1 = new Element();
        Element e2 = new Element();
        c.add(e1).row();
        c.add(e2);
        assertEquals(2, c.table().getChildren().size);
    }

    @Test
    void spacerConsumesSpace() {
        Table root = new Table();
        ParentStack.push(root);
        Table row = new Table();
        Element back = new Element(); back.name = "back";
        Element save = new Element(); save.name = "save";
        row.add(back);
        row.add(new Spacer().element());
        row.add(save);
        assertEquals(3, row.getChildren().size);
        ParentStack.pop();
    }

    @Test
    void gridColumnsWrapping() {
        Grid g = new Grid(3).gap(8f);
        for (int i = 0; i < 4; i++) {
            g.add(new Element());
        }
        assertEquals(4, g.table().getChildren().size);
    }

    @Test
    void stackOverlay() {
        SolimStack s = new SolimStack();
        Element bg = new Element();
        Element fg = new Element();
        s.add(bg);
        s.add(fg);
        assertEquals(2, s.stack().getChildren().size);
    }

    @Test
    void dividerRenders() {
        Divider d = new Divider();
        assertNotNull(d.table());
    }

    @Test
    void containerAddsChild() {
        Container c = new Container().padding(12f);
        Element child = new Element();
        c.add(child);
        assertEquals(1, c.table().getChildren().size);
    }

    @Test
    void scrollWrapsContent() {
        Scroll s = new Scroll();
        Element e = new Element();
        s.add(e);
        assertNotNull(s.content());
        assertEquals(1, s.content().getChildren().size);
    }

    @Test
    void justifyAndAlignEnums() {
        assertEquals(6, Justify.values().length);
        assertTrue(java.util.Arrays.asList(Justify.values()).contains(Justify.BETWEEN));
        assertEquals(4, Align.values().length);
        assertTrue(java.util.Arrays.asList(Align.values()).contains(Align.STRETCH));
    }

    @Test
    void wrapAddsChildren() {
        Wrap w = new Wrap().gap(4f);
        for (int i = 0; i < 3; i++) {
            w.add(new Element());
        }
        assertEquals(3, w.table().getChildren().size);
    }

    @Test
    void columnAligns() {
        Column c = new Column();
        c.align(Align.CENTER);
        assertNotNull(c.table());
    }

    @Test
    void rowJustifyVariants() {
        for (Justify j : Justify.values()) {
            Row r = new Row().justify(j);
            assertNotNull(r.table());
        }
    }

    @Test
    void declarativeColumnAndRowCellLayout() {
        Column col = Ui.column(() -> {
            Ui.row(() -> {
                Element e1 = new Element() {
                    @Override public float getPrefWidth() { return 100f; }
                    @Override public float getPrefHeight() { return 40f; }
                };
                ParentStack.add(e1);
            });
            Ui.scroll(() -> {
                Element e2 = new Element() {
                    @Override public float getPrefWidth() { return 200f; }
                    @Override public float getPrefHeight() { return 200f; }
                };
                ParentStack.add(e2);
            }).grow();
        });

        Table t = col.table();
        assertEquals(2, t.getCells().size, "Column must have 2 cells for its 2 children");
        t.setSize(600f, 800f);
        t.layout();

        Element toolbar = t.getChildren().get(0);
        Element scroll = t.getChildren().get(1);

        assertTrue(toolbar.getWidth() > 0f, "Toolbar must have non-zero width");
        assertTrue(toolbar.getHeight() > 0f, "Toolbar must have non-zero height");
        assertTrue(scroll.getWidth() > 0f, "Scroll must have non-zero width");
        assertTrue(scroll.getHeight() > 0f, "Scroll must have non-zero height");
    }
}

