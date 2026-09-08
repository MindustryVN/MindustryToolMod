package solim.layout;

import arc.scene.Element;
import arc.scene.style.Drawable;
import arc.scene.ui.layout.Cell;
import arc.util.Nullable;
import solim.core.Component;
import solim.modifier.ElementModifiers;
import solim.ui.ParentStack;

/** Simple grid with fixed column count. */
public final class Grid implements Component, LayoutModifiers<Grid> {

	private final SizedTable table;
	private int columns = 1;
	private float gap = 4f;
	private int currentCell = 0;

	public Grid() {
		this.table = new SizedTable();
		this.table.name = "solim-grid-table";
	}

	public Grid(int columns) {
		this();
		this.columns = Math.max(1, columns);
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

	public Grid name(String name) {
		ElementModifiers.name(table, name);
		return this;
	}

	public Grid columns(int c) {
		this.columns = Math.max(1, c);
		return this;
	}

	public Grid gap(float g) {
		this.gap = g;
		ElementModifiers.gap(table, g);
		return this;
	}

	public Grid background(@Nullable Drawable bg) {
		table.background(bg);
		return this;
	}

	public Grid children(@Nullable Runnable r) {
		int[] count = new int[] {0};
		ParentStack.push(table, (tbl, child) -> {
			Cell<?> cell = tbl.add(child);
			if (++count[0] % Math.max(1, columns) == 0) {
				tbl.row();
			}
			return cell;
		});
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

	/** Add a child element to the grid; wraps to next row when columns exceeded. */
	public Grid add(Element child) {
		table.add(child).pad(gap / 2f);
		currentCell++;
		if (currentCell >= columns) {
			table.row();
			currentCell = 0;
		}
		return this;
	}
}
