package solim.layout;

import arc.scene.Element;
import arc.scene.ui.layout.Table;
import arc.scene.ui.layout.Cell;
import arc.util.Nullable;
import solim.core.Component;
import solim.ui.ParentStack;
import solim.ui.Ui;

/**
 * Column layout - vertical Table wrapper.
 */
public final class Column implements Component {
    public static final ParentStack.Attacher ATTACHER = (table, child) -> {
        Cell<?> cell = table.add(child);
        cell.growX();
        if (Ui.isExpanding(child)) {
            cell.growY();
        }
        cell.row();
        return cell;
    };

    private final Table table;

    public Column() {
        this.table = new Table();
        this.table.top().left();
    }

    public Table table() {
        return table;
    }

    @Override
    public Element element() {
        return table;
    }

    public Column gap(float g) {
        table.defaults().pad(g / 2f);
        return this;
    }

    public Column padding(float p) {
        table.margin(p);
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

    public Column grow() {
        table.setFillParent(true);
        return this;
    }

    public Column growX() {
        table.setFillParent(true);
        return this;
    }

    public Column growY() {
        table.setFillParent(true);
        return this;
    }

    public Column children(@Nullable Runnable r) {
        ParentStack.push(table, ATTACHER);
        try {
            if (r != null) {
                r.run();
            }
        } finally {
            ParentStack.pop();
        }
        ParentStack.attachToParent(table);
        return this;
    }

    public Cell<?> add(Element e) {
        return table.add(e);
    }
}
