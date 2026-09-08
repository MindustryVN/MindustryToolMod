package solim.layout;

import arc.scene.Element;
import arc.scene.style.Drawable;
import arc.scene.ui.layout.Table;
import solim.core.Component;
import solim.modifier.ElementModifiers;

/** Container with single child, padding, and optional background. */
public final class Container implements Component {
	private final Table table = new Table();

	public Container() {}

	public Table table() {
		return table;
	}

	@Override
	public Element element() {
		return table;
	}

	public Container name(String name) {
		ElementModifiers.name(table, name);
		return this;
	}

	public Container padding(float p) {
		ElementModifiers.padding(table, p);
		return this;
	}

	public Container background(Drawable d) {
		table.setBackground(d);
		return this;
	}

	public Container add(Element child) {
		table.add(child);
		return this;
	}
}
