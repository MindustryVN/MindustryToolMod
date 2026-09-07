package solim.layout;

import arc.scene.Element;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.util.Nullable;
import solim.core.Component;
import solim.modifier.ElementModifiers;
import solim.ui.ParentStack;
import solim.ui.Ui;

/** Column layout - vertical Table wrapper. */
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
		ElementModifiers.gap(table, g);
		return this;
	}

	public Column padding(float p) {
		ElementModifiers.padding(table, p);
		return this;
	}

	public Column padding(float top, float left, float bottom, float right) {
		ElementModifiers.padding(table, top, left, bottom, right);
		return this;
	}

	public Column paddingTop(float top) {
		ElementModifiers.paddingTop(table, top);
		return this;
	}

	public Column paddingBottom(float bottom) {
		ElementModifiers.paddingBottom(table, bottom);
		return this;
	}

	public Column paddingLeft(float left) {
		ElementModifiers.paddingLeft(table, left);
		return this;
	}

	public Column paddingRight(float right) {
		ElementModifiers.paddingRight(table, right);
		return this;
	}

	public Column margin(float m) {
		ElementModifiers.margin(table, m);
		return this;
	}

	public Column margin(float top, float left, float bottom, float right) {
		ElementModifiers.margin(table, top, left, bottom, right);
		return this;
	}

	public Column marginTop(float top) {
		ElementModifiers.marginTop(table, top);
		return this;
	}

	public Column marginBottom(float bottom) {
		ElementModifiers.marginBottom(table, bottom);
		return this;
	}

	public Column marginLeft(float left) {
		ElementModifiers.marginLeft(table, left);
		return this;
	}

	public Column marginRight(float right) {
		ElementModifiers.marginRight(table, right);
		return this;
	}

	public Column pad(float p) {
		ElementModifiers.pad(table, p);
		return this;
	}

	public Column pad(float top, float left, float bottom, float right) {
		ElementModifiers.pad(table, top, left, bottom, right);
		return this;
	}

	public Column padTop(float top) {
		ElementModifiers.padTop(table, top);
		return this;
	}

	public Column padBottom(float bottom) {
		ElementModifiers.padBottom(table, bottom);
		return this;
	}

	public Column padLeft(float left) {
		ElementModifiers.padLeft(table, left);
		return this;
	}

	public Column padRight(float right) {
		ElementModifiers.padRight(table, right);
		return this;
	}

	public Column width(float width) {
		ElementModifiers.width(table, width);
		return this;
	}

	public Column height(float height) {
		ElementModifiers.height(table, height);
		return this;
	}

	public Column size(float width, float height) {
		ElementModifiers.size(table, width, height);
		return this;
	}

	public Column size(float size) {
		ElementModifiers.size(table, size);
		return this;
	}

	public Column x(float x) {
		ElementModifiers.x(table, x);
		return this;
	}

	public Column y(float y) {
		ElementModifiers.y(table, y);
		return this;
	}

	public Column position(float x, float y) {
		ElementModifiers.position(table, x, y);
		return this;
	}

	public Column visible(boolean visible) {
		ElementModifiers.visible(table, visible);
		return this;
	}

	public Column top() {
		ElementModifiers.top(table);
		return this;
	}

	public Column bottom() {
		ElementModifiers.bottom(table);
		return this;
	}

	public Column left() {
		ElementModifiers.left(table);
		return this;
	}

	public Column right() {
		ElementModifiers.right(table);
		return this;
	}

	public Column center() {
		ElementModifiers.center(table);
		return this;
	}

	public Column align(Align a) {
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
		}
		return this;
	}

	public Column grow() {
		table.userObject = "expanding";
		return this;
	}

	public Column growX() {
		table.userObject = "expanding";
		return this;
	}

	public Column growY() {
		table.userObject = "expanding";
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
