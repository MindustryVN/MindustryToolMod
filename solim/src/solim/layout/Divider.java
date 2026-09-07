package solim.layout;

import arc.scene.ui.layout.Table;

/** Divider line. */
public final class Divider {
	private final Table table = new Table();

	public Divider() {
		table.add().height(2f).growX().row();
	}

	public Table table() {
		return table;
	}
}
