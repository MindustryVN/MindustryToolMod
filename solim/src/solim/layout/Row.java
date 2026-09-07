package solim.layout;

import arc.scene.Element;
import arc.scene.ui.layout.Table;
import arc.scene.ui.layout.Cell;
import solim.core.Component;

/**
 * Row layout - horizontal Table wrapper.
 */
public final class Row implements Component {
    private final Table table;

    public Row() {
        this.table = new Table();
        this.table.left();
    }

    public Table table() {
        return table;
    }

    @Override
    public Element element() {
        return table;
    }

    public Row gap(float g) {
        table.defaults().pad(g / 2f);
        return this;
    }

    public Row padding(float p) {
        table.margin(p);
        return this;
    }

    public Row grow() {
        table.setFillParent(true);
        return this;
    }

    public Row growX() {
        table.setFillParent(true);
        return this;
    }

    public Row growY() {
        table.setFillParent(true);
        return this;
    }

    public Row justify(Justify j) {
        switch (j) {
            case START: table.left(); break;
            case CENTER: table.center(); break;
            case END: table.right(); break;
            case BETWEEN: break;
            case AROUND: break;
            case EVENLY: break;
            default: break;
        }
        return this;
    }

    public Row align(Align a) {
        switch (a) {
            case START: table.top(); break;
            case CENTER: table.center(); break;
            case END: table.bottom(); break;
            case STRETCH: table.top(); break;
            default: break;
        }
        return this;
    }

    public Cell<?> add(arc.scene.Element e) {
        return table.add(e);
    }
}
