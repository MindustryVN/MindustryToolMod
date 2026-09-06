package solim.layout;

import arc.scene.ui.layout.Table;

/**
 * Wrap container: lays out children in a row that wraps.
 */
public final class Wrap {
    private final Table table = new Table();
    private float gap = 4f;

    public Wrap() {
    }

    public Table table() {
        return table;
    }

    public Wrap gap(float g) {
        this.gap = g;
        return this;
    }

    public Wrap add(arc.scene.Element child) {
        table.add(child).pad(gap / 2f);
        return this;
    }
}
