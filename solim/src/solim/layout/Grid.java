package solim.layout;

import arc.scene.Element;
import arc.scene.style.Drawable;
import arc.scene.ui.layout.Cell;
import arc.struct.Seq;
import arc.util.Nullable;
import solim.core.Component;
import solim.core.ComponentContext;
import solim.modifier.ElementModifiers;
import solim.signal.Effect;
import solim.signal.Readable;
import solim.ui.ParentStack;
import solim.ui.Ui;

/** Simple grid with fixed or reactive column count and customizable gap. */
public final class Grid implements Component, LayoutModifiers<Grid> {

	private final SizedTable table;
	private int columns = 1;
	private float gap = 4f;
	private int currentCell = 0;

	public Grid() {
		this.table = new SizedTable();
		this.table.name = "solim-grid-table";
		ElementModifiers.gap(table, gap);
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
		reflow();
		return this;
	}

	public Grid columns(@Nullable Readable<Integer> c) {
		if (c != null) {
			Effect e = Effect.of(() -> {
				Integer cols = c.get();
				if (cols != null) {
					this.columns = Math.max(1, cols);
					reflow();
				}
			});
			ComponentContext.register(e);
		}
		return this;
	}

	public Grid gap(float g) {
		this.gap = g;
		ElementModifiers.gap(table, g);
		return this;
	}

	public Grid gap(@Nullable Readable<Float> gapSignal) {
		if (gapSignal != null) {
			Effect e = Effect.of(() -> {
				Float g = gapSignal.get();
				if (g != null) {
					gap(g);
				}
			});
			ComponentContext.register(e);
		}
		return this;
	}

	public Grid background(@Nullable Drawable bg) {
		table.background(bg);
		return this;
	}

	public Grid children(@Nullable Runnable r) {
		int[] count = new int[] {0};
		ElementModifiers.gap(table, gap);
		ParentStack.push(table, (tbl, child) -> {
			Cell<?> cell = tbl.add(child).pad(gap / 2f);
			if (Ui.isExpanding(child)) {
				cell.growX().fillX();
			}
			cell.uniformX();
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

	private void reflow() {
		if (table.getChildren().size == 0) return;
		Seq<Element> children = new Seq<>(table.getChildren());
		table.clear();
		ElementModifiers.gap(table, gap);
		int col = 0;
		for (Element child : children) {
			Cell<?> cell = table.add(child).pad(gap / 2f);
			if (Ui.isExpanding(child)) {
				cell.growX().fillX();
			}
			cell.uniformX();
			if (++col % Math.max(1, columns) == 0) {
				table.row();
			}
		}
		table.invalidateHierarchy();
	}

	/** Add a child element to the grid; wraps to next row when columns exceeded. */
	public Grid add(Element child) {
		Cell<?> cell = table.add(child).pad(gap / 2f);
		if (Ui.isExpanding(child)) {
			cell.growX().fillX();
		}
		cell.uniformX();
		currentCell++;
		if (currentCell >= columns) {
			table.row();
			currentCell = 0;
		}
		return this;
	}
}
