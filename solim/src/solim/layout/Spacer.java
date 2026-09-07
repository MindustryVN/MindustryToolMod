package solim.layout;

import arc.scene.Element;
import arc.scene.ui.layout.Table;

/**
 * Spacer that consumes remaining space in a row/column.
 */
public final class Spacer {
    private final Table table = new Table();

    public Spacer() {
        table.name = "spacer";
        table.add().growX().growY();
    }

    public Element element() {
        return table;
    }
}
