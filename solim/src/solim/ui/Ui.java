package solim.ui;

import arc.scene.Element;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import solim.core.Component;

/**
 * Declarative UI facades for Solim.
 * Each method pushes a layout, runs the lambda, pops, and returns the layout element.
 */
public final class Ui {
    private Ui() {
    }

    public static Table column(Runnable r) {
        Table t = new Table();
        ParentStack.push(t);
        try {
            r.run();
        } finally {
            ParentStack.pop();
        }
        ParentStack.attachToParent(t);
        return t;
    }

    public static Table row(Runnable r) {
        Table t = new Table();
        ParentStack.push(t);
        try {
            r.run();
        } finally {
            ParentStack.pop();
        }
        ParentStack.attachToParent(t);
        return t;
    }

    public static Table stack(Runnable r) {
        // use a Table as simple container for declarative usage; actual Stack widget
        // can be introduced when Arc Stack is available
        return row(r);
    }

    public static Table grid(int columns, Runnable r) {
        Table t = new Table();
        ParentStack.push(t);
        try {
            r.run();
        } finally {
            ParentStack.pop();
        }
        ParentStack.attachToParent(t);
        return t;
    }

    public static Table wrap(Runnable r) {
        return row(r);
    }

    public static Table scroll(Runnable r) {
        return column(r);
    }

    public static Table container(Runnable r) {
        return column(r);
    }

    public static Element text(String s) {
        Label l = new Label(s);
        ParentStack.add(l);
        return l;
    }
}
