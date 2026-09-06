package solim.layout;

import arc.scene.ui.layout.Table;

/**
 * Simple grid with fixed column count.
 */
public final class Grid {
    private final Table table = new Table();
    private int columns = 1;
    private float gap = 4f;
    private int currentCell = 0;

    public Grid() {
    }

    public Grid(int columns) {
        this.columns = columns;
    }

    public Table table() {
        return table;
    }

    public Grid columns(int c) {
        this.columns = c;
        return this;
    }

    public Grid gap(float g) {
        this.gap = g;
        return this;
    }

    /**
     * Add a child element to the grid; wraps to next row when columns exceeded.
     */
    public Grid add(arc.scene.Element child) {
        table.add(child).pad(gap / 2f);
        currentCell++;
        if (currentCell >= columns) {
            table.row();
            currentCell = 0;
        }
        return this;
    }
}
