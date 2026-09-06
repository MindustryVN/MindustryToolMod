package solim.layout;

import arc.scene.ui.layout.Table;

/**
 * Scroll container wrapping a Table.
 * Uses Table as a placeholder for ScrollPane; consumers can
 * wrap in actual scrollable container externally.
 */
public final class Scroll {
    private final Table content = new Table();

    public Table content() {
        return content;
    }

    public Scroll add(arc.scene.Element child) {
        content.add(child).growX().row();
        return this;
    }
}
