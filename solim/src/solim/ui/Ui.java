package solim.ui;

import arc.scene.Element;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import solim.display.Text;
import solim.input.Button;
import solim.layout.Column;
import solim.layout.Divider;
import solim.layout.Row;
import solim.layout.Spacer;
import solim.signal.Computed;
import solim.signal.Signal;

/**
 * Declarative UI facades for Solim.
 * Each method pushes a layout, runs the lambda, pops, and returns the layout element.
 */
public final class Ui {
    private Ui() {
    }

    public static Column column(Runnable r) {
        Column col = new Column();
        ParentStack.push(col.table());
        try {
            r.run();
        } finally {
            ParentStack.pop();
        }
        ParentStack.attachToParent(col.table());
        return col;
    }

    public static Row row(Runnable r) {
        Row row = new Row();
        ParentStack.push(row.table());
        try {
            r.run();
        } finally {
            ParentStack.pop();
        }
        ParentStack.attachToParent(row.table());
        return row;
    }

    public static Table stack(Runnable r) {
        return row(r).table();
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
        return row(r).table();
    }

    public static Table scroll(Runnable r) {
        return column(r).table();
    }

    public static Table container(Runnable r) {
        return column(r).table();
    }

    public static Element divider() {
        Divider d = new Divider();
        ParentStack.attachToParent(d.table());
        return d.table();
    }

    public static Element spacer() {
        Spacer s = new Spacer();
        ParentStack.attachToParent(s.element());
        return s.element();
    }

    public static Button button(String text, Runnable onClick) {
        Button b = Button.of(text, onClick);
        ParentStack.attachToParent(b.textButton());
        return b;
    }

    public static Button button(Signal<String> text, Runnable onClick) {
        Button b = Button.of(text, onClick);
        ParentStack.attachToParent(b.textButton());
        return b;
    }

    public static Button button(Computed<String> text, Runnable onClick) {
        Button b = Button.of(text, onClick);
        ParentStack.attachToParent(b.textButton());
        return b;
    }

    public static Element text(String s) {
        Label l = new Label(s);
        ParentStack.add(l);
        return l;
    }

    public static Text text(Signal<String> s) {
        Text t = Text.of(s);
        ParentStack.attachToParent(t.label());
        return t;
    }

    public static Text text(Computed<String> s) {
        Text t = Text.of(s);
        ParentStack.attachToParent(t.label());
        return t;
    }
}
