package solim.layout;

import arc.scene.Element;
import solim.core.Component;
import solim.modifier.ElementModifiers;

/** Divider line. */
public final class Divider implements Component, LayoutModifiers<Divider> {
    private final SizedTable table = new SizedTable();

    public Divider() {
        table.name = "solim-divider-table";
        table.getSizeConstraints().growX = true;
        table.add().height(2f).growX().row();
    }

    public SizedTable table() {
        return table;
    }

    @Override
    public Element element() {
        return table;
    }

    @Override
    public SizeConstraints sizeConstraints() {
        return table.getSizeConstraints();
    }

    public Divider name(String name) {
        ElementModifiers.name(table, name);
        return this;
    }
}
