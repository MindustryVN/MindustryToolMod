package solim.layout;

import arc.scene.Element;
import arc.scene.ui.layout.Table;
import solim.core.Component;
import solim.modifier.ElementModifiers;

/** Divider line. */
public final class Divider implements Component {
    private final Table table = new Table();

    public Divider() {
        table.add().height(2f).growX().row();
    }

    public Table table() {
        return table;
    }

    @Override
    public Element element() {
        return table;
    }

    public Divider name(String name) {
        ElementModifiers.name(table, name);
        return this;
    }
}
