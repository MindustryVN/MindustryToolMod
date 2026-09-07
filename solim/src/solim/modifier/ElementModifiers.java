package solim.modifier;

import arc.scene.Element;
import arc.scene.ui.layout.Table;
import arc.util.Nullable;
import solim.input.Button.SizedButton;
import solim.layout.Card.CardButton;

/**
 * Utility class providing static helper methods for modifying Arc elements and tables.
 */
public final class ElementModifiers {

	private ElementModifiers() {
	}

	public static void width(@Nullable Element element, float width) {
		if (element == null) return;
		float val = Math.max(0f, width);
		element.setWidth(val);
		if (element instanceof SizedButton) {
			((SizedButton) element).setCustomPrefWidth(val);
		} else if (element instanceof CardButton) {
			((CardButton) element).setCustomPrefWidth(val);
		}
		element.invalidateHierarchy();
	}

	public static void height(@Nullable Element element, float height) {
		if (element == null) return;
		float val = Math.max(0f, height);
		element.setHeight(val);
		if (element instanceof SizedButton) {
			((SizedButton) element).setCustomPrefHeight(val);
		} else if (element instanceof CardButton) {
			((CardButton) element).setCustomPrefHeight(val);
		}
		element.invalidateHierarchy();
	}

	public static void size(@Nullable Element element, float width, float height) {
		width(element, width);
		height(element, height);
	}

	public static void size(@Nullable Element element, float size) {
		size(element, size, size);
	}

	public static void x(@Nullable Element element, float x) {
		if (element == null) return;
		element.x = x;
	}

	public static void y(@Nullable Element element, float y) {
		if (element == null) return;
		element.y = y;
	}

	public static void position(@Nullable Element element, float x, float y) {
		if (element == null) return;
		element.setPosition(x, y);
	}

	public static void visible(@Nullable Element element, boolean visible) {
		if (element == null) return;
		element.visible = visible;
	}

	public static void align(@Nullable Table table, int align) {
		if (table == null) return;
		table.align(align);
	}

	public static void top(@Nullable Table table) {
		if (table == null) return;
		table.top();
	}

	public static void bottom(@Nullable Table table) {
		if (table == null) return;
		table.bottom();
	}

	public static void left(@Nullable Table table) {
		if (table == null) return;
		table.left();
	}

	public static void right(@Nullable Table table) {
		if (table == null) return;
		table.right();
	}

	public static void center(@Nullable Table table) {
		if (table == null) return;
		table.center();
	}

	public static void margin(@Nullable Table table, float margin) {
		if (table == null) return;
		table.margin(margin);
	}

	public static void margin(@Nullable Table table, float top, float left, float bottom, float right) {
		if (table == null) return;
		table.margin(top, left, bottom, right);
	}

	public static void marginTop(@Nullable Table table, float top) {
		if (table == null) return;
		table.marginTop(top);
	}

	public static void marginBottom(@Nullable Table table, float bottom) {
		if (table == null) return;
		table.marginBottom(bottom);
	}

	public static void marginLeft(@Nullable Table table, float left) {
		if (table == null) return;
		table.marginLeft(left);
	}

	public static void marginRight(@Nullable Table table, float right) {
		if (table == null) return;
		table.marginRight(right);
	}

	public static void pad(@Nullable Table table, float pad) {
		margin(table, pad);
	}

	public static void pad(@Nullable Table table, float top, float left, float bottom, float right) {
		margin(table, top, left, bottom, right);
	}

	public static void padTop(@Nullable Table table, float top) {
		marginTop(table, top);
	}

	public static void padBottom(@Nullable Table table, float bottom) {
		marginBottom(table, bottom);
	}

	public static void padLeft(@Nullable Table table, float left) {
		marginLeft(table, left);
	}

	public static void padRight(@Nullable Table table, float right) {
		marginRight(table, right);
	}

	public static void padding(@Nullable Table table, float padding) {
		margin(table, padding);
	}

	public static void padding(@Nullable Table table, float top, float left, float bottom, float right) {
		margin(table, top, left, bottom, right);
	}

	public static void paddingTop(@Nullable Table table, float top) {
		marginTop(table, top);
	}

	public static void paddingBottom(@Nullable Table table, float bottom) {
		marginBottom(table, bottom);
	}

	public static void paddingLeft(@Nullable Table table, float left) {
		marginLeft(table, left);
	}

	public static void paddingRight(@Nullable Table table, float right) {
		marginRight(table, right);
	}

	public static void gap(@Nullable Table table, float gap) {
		if (table == null) return;
		table.defaults().pad(gap / 2f);
	}

	public static void gap(@Nullable Element element, float gap) {
		if (element instanceof Table) {
			gap((Table) element, gap);
		}
	}
}
