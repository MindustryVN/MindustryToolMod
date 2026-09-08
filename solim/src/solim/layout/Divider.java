package solim.layout;

import arc.Core;
import arc.graphics.Color;
import arc.scene.Element;
import arc.scene.style.Drawable;
import arc.scene.ui.Image;
import solim.core.Component;
import solim.modifier.ElementModifiers;

/** Divider line supporting horizontal (X) and vertical (Y) directions. */
public final class Divider implements Component, LayoutModifiers<Divider> {
	private final SizedTable table = new SizedTable();
	private final Direction direction;

	public Divider() {
		this(Direction.X);
	}

	public Divider(Direction direction) {
		this.direction = direction != null ? direction : Direction.X;
		table.name = "solim-divider-table";
		Drawable white = (Core.atlas != null && Core.atlas.has("whiteui"))
				? Core.atlas.drawable("whiteui")
				: null;

		if (this.direction == Direction.Y) {
			table.getSizeConstraints().growY = true;
			if (white != null) {
				Image img = new Image(white);
				img.setColor(new Color(1f, 1f, 1f, 0.15f));
				table.add(img).width(1.5f).growY();
			} else {
				table.add().width(1.5f).growY();
			}
		} else {
			table.getSizeConstraints().growX = true;
			if (white != null) {
				Image img = new Image(white);
				img.setColor(new Color(1f, 1f, 1f, 0.15f));
				table.add(img).height(1.5f).growX().row();
			} else {
				table.add().height(1.5f).growX().row();
			}
		}
	}

	public Direction direction() {
		return direction;
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
