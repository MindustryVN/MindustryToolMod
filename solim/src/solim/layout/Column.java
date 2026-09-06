package solim.layout;

import arc.scene.ui.layout.Table;
import arc.scene.ui.layout.Cell;

/**
 * Column layout - vertical Table wrapper.
 */
public final class Column {
    private final Table table;

    public Column() {
        this.table = new Table();
        this.table.top().left();
    }

    public Table table() {
        return table;
    }

    public Column gap(float g) {
        table.defaults().pad(g / 2f);
        return this;
    }

    public Column padding(float p) {
        // Use defaults pad and apply to table itself via element fields
        return this;
    }

    public Column align(Align a) {
        switch (a) {
            case START: table.top(); break;
            case CENTER: table.center(); break;
            case END: table.bottom(); break;
            case STRETCH: table.top(); break;
        }
        return this;
    }

    public Cell add(arc.scene.Element e) {
        return table.add(e);
    }
}
