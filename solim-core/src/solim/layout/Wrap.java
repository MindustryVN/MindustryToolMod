package solim.layout;

import arc.scene.Element;
import arc.scene.ui.layout.Table;
import solim.core.Component;
import solim.modifier.ElementModifiers;

/** Wrap container: lays out children in a row that wraps. */
public final class Wrap implements Component {
	private final Table table = new Table();
	private float gap = 4f;

	public Wrap() {
		this.table.name = "solim-wrap-table";
	}

	public Table table() {
		return table;
	}

	@Override
	public Element element() {
		return table;
	}

	public Wrap name(String name) {
		ElementModifiers.name(table, name);
		return this;
	}

	public Wrap gap(float g) {
		this.gap = g;
		ElementModifiers.gap(table, g);
		return this;
	}

	public Wrap add(Element child) {
		table.add(child).pad(gap / 2f);
		return this;
	}
}
