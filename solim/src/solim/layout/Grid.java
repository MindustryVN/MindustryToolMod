package solim.layout;

import arc.scene.Element;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.util.Nullable;
import solim.core.Component;
import solim.ui.ParentStack;

/** Simple grid with fixed column count. */
public final class Grid implements Component {
	private final Table table = new Table();
	private int columns = 1;
	private float gap = 4f;
	private int currentCell = 0;

	public Grid() {}

	public Grid(int columns) {
		this.columns = Math.max(1, columns);
	}

	public Table table() {
		return table;
	}

	@Override
	public Element element() {
		return table;
	}

	public Grid columns(int c) {
		this.columns = Math.max(1, c);
		return this;
	}

	public Grid gap(float g) {
		this.gap = g;
		table.defaults().pad(g / 2f);
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
