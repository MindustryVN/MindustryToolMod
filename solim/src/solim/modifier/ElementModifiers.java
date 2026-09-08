package solim.modifier;

import arc.scene.Element;
import arc.scene.ui.layout.Table;
import arc.util.Nullable;
import java.util.Objects;
import solim.input.Button.SizedButton;
import solim.layout.Card.CardButton;

/**
 * Utility class providing static helper methods for modifying Arc elements and tables.
 */
public final class ElementModifiers {

	private ElementModifiers() {
	}

	public static void width(Element element, float width) {
		Objects.requireNonNull(element, "element cannot be null");
		float val = Math.max(0f, width);
		element.setWidth(val);
		if (element instanceof SizedButton) {
			((SizedButton) element).setCustomPrefWidth(val);
		} else if (element instanceof CardButton) {
			((CardButton) element).setCustomPrefWidth(val);
		}
		element.invalidateHierarchy();
	}

	public static void height(Element element, float height) {
		Objects.requireNonNull(element, "element cannot be null");
		float val = Math.max(0f, height);
		element.setHeight(val);
		if (element instanceof SizedButton) {
			((SizedButton) element).setCustomPrefHeight(val);
		} else if (element instanceof CardButton) {
			((CardButton) element).setCustomPrefHeight(val);
		}
		element.invalidateHierarchy();
	}

	public static void size(Element element, float width, float height) {
		width(element, width);
		height(element, height);
	}

	public static void size(Element element, float size) {
		size(element, size, size);
	}

	public static void x(Element element, float x) {
		Objects.requireNonNull(element, "element cannot be null");
		element.x = x;
	}

	public static void y(Element element, float y) {
		Objects.requireNonNull(element, "element cannot be null");
		element.y = y;
	}

	public static void position(Element element, float x, float y) {
		Objects.requireNonNull(element, "element cannot be null");
		element.setPosition(x, y);
	}

	public static void visible(Element element, boolean visible) {
		Objects.requireNonNull(element, "element cannot be null");
		element.visible = visible;
	}

	public static void name(Element element, @Nullable String name) {
		Objects.requireNonNull(element, "element cannot be null");
		element.name = name;
	}

	public static void align(Table table, int align) {
		Objects.requireNonNull(table, "table cannot be null");
		table.align(align);
	}

	public static void top(Table table) {
		Objects.requireNonNull(table, "table cannot be null");
		table.top();
	}

	public static void bottom(Table table) {
		Objects.requireNonNull(table, "table cannot be null");
		table.bottom();
	}

	public static void left(Table table) {
		Objects.requireNonNull(table, "table cannot be null");
		table.left();
	}

	public static void right(Table table) {
		Objects.requireNonNull(table, "table cannot be null");
		table.right();
	}

	public static void center(Table table) {
		Objects.requireNonNull(table, "table cannot be null");
		table.center();
	}

	public static void margin(Table table, float margin) {
		Objects.requireNonNull(table, "table cannot be null");
		table.margin(margin);
	}

	public static void margin(Table table, float top, float left, float bottom, float right) {
		Objects.requireNonNull(table, "table cannot be null");
		table.margin(top, left, bottom, right);
	}

	public static void marginTop(Table table, float top) {
		Objects.requireNonNull(table, "table cannot be null");
		table.marginTop(top);
	}

	public static void marginBottom(Table table, float bottom) {
		Objects.requireNonNull(table, "table cannot be null");
		table.marginBottom(bottom);
	}

	public static void marginLeft(Table table, float left) {
		Objects.requireNonNull(table, "table cannot be null");
		table.marginLeft(left);
	}

	public static void marginRight(Table table, float right) {
		Objects.requireNonNull(table, "table cannot be null");
		table.marginRight(right);
	}

	public static void pad(Table table, float pad) {
		margin(table, pad);
	}

	public static void pad(Table table, float top, float left, float bottom, float right) {
		margin(table, top, left, bottom, right);
	}

	public static void padTop(Table table, float top) {
		marginTop(table, top);
	}

	public static void padBottom(Table table, float bottom) {
		marginBottom(table, bottom);
	}

	public static void padLeft(Table table, float left) {
		marginLeft(table, left);
	}

	public static void padRight(Table table, float right) {
		marginRight(table, right);
	}

	public static void padding(Table table, float padding) {
		margin(table, padding);
	}

	public static void padding(Table table, float top, float left, float bottom, float right) {
		margin(table, top, left, bottom, right);
	}

	public static void paddingTop(Table table, float top) {
		marginTop(table, top);
	}

	public static void paddingBottom(Table table, float bottom) {
		marginBottom(table, bottom);
	}

	public static void paddingLeft(Table table, float left) {
		marginLeft(table, left);
	}

	public static void paddingRight(Table table, float right) {
		marginRight(table, right);
	}

	public static void gap(Table table, float gap) {
		Objects.requireNonNull(table, "table cannot be null");
		table.defaults().pad(gap / 2f);
	}

	public static void gap(Element element, float gap) {
		Objects.requireNonNull(element, "element cannot be null");
		if (!(element instanceof Table)) {
			throw new IllegalArgumentException("Element must be an instance of Table to apply gap spacing: " + element.getClass().getName());
		}
		gap((Table) element, gap);
	}
}
