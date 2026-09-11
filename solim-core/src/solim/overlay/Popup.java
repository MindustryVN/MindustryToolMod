package solim.overlay;

import arc.graphics.Color;
import arc.scene.ui.layout.Table;
import arc.util.Nullable;
import solim.modifier.ElementModifiers;
import solim.signal.Readable;

/** Popup (lightweight tooltip) placeholder. */
public final class Popup {
	private final Table table = new Table();

	public Popup() {}

	public Table table() {
		return table;
	}

	public Popup rounded(int radius) {
		ElementModifiers.rounded(table, radius);
		return this;
	}

	public Popup rounded(int radius, @Nullable Color color) {
		ElementModifiers.rounded(table, radius, color);
		return this;
	}

	public Popup rounded(int radius, @Nullable Readable<Color> color) {
		ElementModifiers.rounded(table, radius, color);
		return this;
	}

	public Popup border(float stroke, @Nullable Color color) {
		ElementModifiers.border(table, stroke, color);
		return this;
	}

	public Popup border(float stroke, @Nullable Readable<Color> color) {
		ElementModifiers.border(table, stroke, color);
		return this;
	}
}
