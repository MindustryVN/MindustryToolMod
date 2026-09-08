package solim.layout;

import arc.scene.Element;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.util.Nullable;
import solim.core.Component;
import solim.modifier.ElementModifiers;
import solim.ui.ParentStack;
import solim.ui.Ui;

/** Row layout - horizontal Table wrapper. */
public final class Row implements Component {
	public static final ParentStack.Attacher ATTACHER = (table, child) -> {
		Cell<?> cell = table.add(child);
		if (child instanceof TextField || Ui.isExpanding(child)) {
			cell.growX();
		}
		return cell;
	};

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

	public Row name(String name) {
		ElementModifiers.name(table, name);
		return this;
	}

	public Row gap(float g) {
		ElementModifiers.gap(table, g);
		return this;
	}

	public Row padding(float p) {
		ElementModifiers.padding(table, p);
		return this;
	}

	public Row padding(float top, float left, float bottom, float right) {
		ElementModifiers.padding(table, top, left, bottom, right);
		return this;
	}

	public Row paddingTop(float top) {
		ElementModifiers.paddingTop(table, top);
		return this;
	}

	public Row paddingBottom(float bottom) {
		ElementModifiers.paddingBottom(table, bottom);
		return this;
	}

	public Row paddingLeft(float left) {
		ElementModifiers.paddingLeft(table, left);
		return this;
	}

	public Row paddingRight(float right) {
		ElementModifiers.paddingRight(table, right);
		return this;
	}

	public Row margin(float m) {
		ElementModifiers.margin(table, m);
		return this;
	}

	public Row margin(float top, float left, float bottom, float right) {
		ElementModifiers.margin(table, top, left, bottom, right);
		return this;
	}

	public Row marginTop(float top) {
		ElementModifiers.marginTop(table, top);
		return this;
	}

	public Row marginBottom(float bottom) {
		ElementModifiers.marginBottom(table, bottom);
		return this;
	}

	public Row marginLeft(float left) {
		ElementModifiers.marginLeft(table, left);
		return this;
	}

	public Row marginRight(float right) {
		ElementModifiers.marginRight(table, right);
		return this;
	}

	public Row pad(float p) {
		ElementModifiers.pad(table, p);
		return this;
	}

	public Row pad(float top, float left, float bottom, float right) {
		ElementModifiers.pad(table, top, left, bottom, right);
		return this;
	}

	public Row padTop(float top) {
		ElementModifiers.padTop(table, top);
		return this;
	}

	public Row padBottom(float bottom) {
		ElementModifiers.padBottom(table, bottom);
		return this;
	}

	public Row padLeft(float left) {
		ElementModifiers.padLeft(table, left);
		return this;
	}

	public Row padRight(float right) {
		ElementModifiers.padRight(table, right);
		return this;
	}

	public Row width(float width) {
		ElementModifiers.width(table, width);
		return this;
	}

	public Row height(float height) {
		ElementModifiers.height(table, height);
		return this;
	}

	public Row size(float width, float height) {
		ElementModifiers.size(table, width, height);
		return this;
	}

	public Row size(float size) {
		ElementModifiers.size(table, size);
		return this;
	}

	public Row x(float x) {
		ElementModifiers.x(table, x);
		return this;
	}

	public Row y(float y) {
		ElementModifiers.y(table, y);
		return this;
	}

	public Row position(float x, float y) {
		ElementModifiers.position(table, x, y);
		return this;
	}

	public Row visible(boolean visible) {
		ElementModifiers.visible(table, visible);
		return this;
	}

	public Row top() {
		ElementModifiers.top(table);
		return this;
	}

	public Row bottom() {
		ElementModifiers.bottom(table);
		return this;
	}

	public Row left() {
		ElementModifiers.left(table);
		return this;
	}

	public Row right() {
		ElementModifiers.right(table);
		return this;
	}

	public Row center() {
		ElementModifiers.center(table);
		return this;
	}

	public Row grow() {
		table.userObject = "expanding";
		return this;
	}

	public Row growX() {
		table.userObject = "expanding";
		return this;
	}

	public Row growY() {
		table.userObject = "expanding";
		return this;
	}

	public Row justify(Justify j) {
		switch (j) {
			case START:
				table.left();
				break;
			case CENTER:
				table.center();
				break;
			case END:
				table.right();
				break;
			case BETWEEN:
				break;
			case AROUND:
				break;
			case EVENLY:
				break;
			default:
				break;
		}
		return this;
	}

	public Row align(Align a) {
		switch (a) {
			case START:
				table.top();
				break;
			case CENTER:
				table.center();
				break;
			case END:
				table.bottom();
				break;
			case STRETCH:
				table.top();
				break;
			default:
				break;
		}
		return this;
	}

	public Row children(@Nullable Runnable r) {
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
