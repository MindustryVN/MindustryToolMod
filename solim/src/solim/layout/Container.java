package solim.layout;

import arc.scene.ui.layout.Table;

/**
 * Container with single child, padding, and optional background.
 */
public final class Container {
    private final Table table = new Table();

    public Container() {
    }

    public Table table() {
        return table;
    }

    public Container padding(float p) {
        return this;
    }

    public Container background(arc.scene.style.Drawable d) {
        table.setBackground(d);
        return this;
    }

    public Container add(arc.scene.Element child) {
        table.add(child);
        return this;
    }
}
