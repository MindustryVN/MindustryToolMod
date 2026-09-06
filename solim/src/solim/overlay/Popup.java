package solim.overlay;

import arc.scene.ui.layout.Table;

/**
 * Popup (lightweight tooltip) placeholder.
 */
public final class Popup {
    private final Table table = new Table();

    public Popup() {}

    public Table table() {
        return table;
    }
}
