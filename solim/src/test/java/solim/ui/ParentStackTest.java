package solim.ui;

import arc.scene.Element;
import arc.scene.ui.layout.Table;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import solim.core.Component;

import static org.junit.jupiter.api.Assertions.*;

class ParentStackTest {

    @AfterEach
    void clear() {
        ParentStack.clear();
    }

    @Test
    void pushPopCurrent() {
        Table root = new Table();
        ParentStack.push(root);
        assertEquals(root, ParentStack.current());
        assertEquals(1, ParentStack.size());
        ParentStack.pop();
        assertNull(ParentStack.current());
        assertEquals(0, ParentStack.size());
    }

    @Test
    void autoAttachChildren() {
        Table root = new Table();
        ParentStack.push(root);
        Element e1 = new Element();
        ParentStack.add(e1);
        Element e2 = new Element();
        ParentStack.add(e2);
        assertEquals(2, root.getChildren().size);
        assertTrue(root.getChildren().contains(e1, true));
        assertTrue(root.getChildren().contains(e2, true));
    }

    @Test
    void tryFinallyCleanup() {
        assertEquals(0, ParentStack.size());
        ParentStack.push(new Table());
        assertEquals(1, ParentStack.size());
        assertThrows(RuntimeException.class, () -> {
            ParentStack.push(new Table());
            try {
                throw new RuntimeException("fail");
            } finally {
                ParentStack.pop();
            }
        });
        assertEquals(1, ParentStack.size(), "Stack restored after exception");
        ParentStack.pop();
    }

    @Test
    void columnDeclarative() {
        Table root = new Table();
        ParentStack.push(root);
        var col = Ui.column(() -> {
            Element e = new Element();
            ParentStack.add(e);
        });
        assertEquals(1, root.getChildren().size);
        assertTrue(root.getChildren().contains(col.table(), true));
    }

    @Test
    void nestedColumnRow() {
        Table root = new Table();
        ParentStack.push(root);
        var col = Ui.column(() -> {
            Element t = new Element();
            ParentStack.add(t);
            var row = Ui.row(() -> {
                Element b1 = new Element();
                ParentStack.add(b1);
                Element b2 = new Element();
                ParentStack.add(b2);
            });
        });
        assertEquals(1, root.getChildren().size);
        assertEquals(2, col.table().getChildren().size);
        Table row = (Table) col.table().getChildren().get(1);
        assertEquals(2, row.getChildren().size);
    }

    @Test
    void exceptionCleanupRestoresStack() {
        assertEquals(0, ParentStack.size());
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            Table g = new Table();
            ParentStack.push(g);
            try {
                throw new RuntimeException("boom");
            } finally {
                ParentStack.pop();
            }
        });
        assertNotNull(thrown);
        assertEquals(0, ParentStack.size());
    }

    @Test
    void addOutsideParent() {
        Element e = new Element();
        Element result = ParentStack.add(e);
        assertSame(e, result);
        assertEquals(0, ParentStack.size());
    }

    @Test
    void resolveElementPassthrough() {
        Element e = new Element();
        Element resolved = ElementResolver.resolve(e);
        assertSame(e, resolved);
    }

    @Test
    void resolveComponent() {
        Component comp = () -> new Element();
        Element resolved = ElementResolver.resolve(comp);
        assertNotNull(resolved);
        assertNotNull(comp.element());
    }

    @Test
    void mixedElementAndComponentChildren() {
        Table root = new Table();
        ParentStack.push(root);
        Element e = new Element();
        Component comp = () -> {
            Element ce = new Element();
            ce.name = "comp";
            return ce;
        };
        // skip Ui.text("Hello") because it needs arc.Core.scene; test add() instead
        ParentStack.add(comp);
        ParentStack.add(e);
        assertEquals(2, root.getChildren().size);
    }

    @Test
    void noStartEndApi() throws Exception {
        assertThrows(ClassNotFoundException.class, () -> {
            Class.forName("solim.ui.UiStart");
        });
    }
}
