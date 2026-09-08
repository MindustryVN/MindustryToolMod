package solim.layout;

import java.util.function.Consumer;

import arc.Core;
import arc.scene.Element;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.util.Nullable;
import solim.core.Component;
import solim.modifier.ElementModifiers;
import solim.ui.ParentStack;
import solim.ui.Ui;

/** Scroll container wrapping a Table in a ScrollPane. */
public final class Scroll implements Component, LayoutModifiers<Scroll> {

	public static final ParentStack.Attacher ATTACHER = (table, child) -> {
		Cell<?> cell = table.add(child);
		cell.top().left();
		cell.growX();
		if (Ui.isExpanding(child)) {
			cell.growY();
		}
		cell.row();
		return cell;
	};

	private final SizedTable outer;
	private final Table content;
	private final ScrollPane pane;
	private boolean centered = false;

	public Scroll() {
		this.outer = new SizedTable();
		this.outer.name = "solim-scroll-pane-outer";
		this.outer.top().left();
		this.content = new SizedTable();
		this.content.top().left();
		this.content.name = "solim-scroll-pane-content";
		if (Core.scene != null) {
			this.pane = outer.pane(content).grow().scrollX(false).scrollY(true).get();
			this.pane.name = "solim-scroll-pane";
			this.pane.setScrollingDisabled(true, false);
		} else {
			this.pane = null;
			outer.add(content).grow();
		}
	}

	public SizedTable outer() {
		return outer;
	}

	public Scroll outer(Consumer<SizedTable> consumer) {
		consumer.accept(outer);
		return this;
	}

	public Table content() {
		return content;
	}

	public Scroll content(Consumer<Table> consumer) {
		consumer.accept(content);
		return this;
	}

	public ScrollPane pane() {
		return pane;
	}

	public Scroll pane(Consumer<ScrollPane> consumer) {
		consumer.accept(pane);
		return this;
	}

	@Override
	public Scroll center() {
		this.centered = true;
		outer.center();
		content.center();
		for (Cell<?> cell : content.getCells()) {
			cell.center().top();
		}
		return LayoutModifiers.super.center();
	}

	@Override
	public Scroll left() {
		this.centered = false;
		outer.left();
		content.left();
		for (Cell<?> cell : content.getCells()) {
			cell.left().top();
		}
		return LayoutModifiers.super.left();
	}

	@Override
	public Scroll right() {
		this.centered = false;
		outer.right();
		content.right();
		for (Cell<?> cell : content.getCells()) {
			cell.right().top();
		}
		return LayoutModifiers.super.right();
	}

	@Override
	public Element element() {
		return outer;
	}

	@Override
	public SizeConstraints sizeConstraints() {
		return outer.getSizeConstraints();
	}

	public Scroll x(float x) {
		ElementModifiers.x(outer, x);
		return this;
	}

	public Scroll y(float y) {
		ElementModifiers.y(outer, y);
		return this;
	}

	public Scroll position(float x, float y) {
		ElementModifiers.position(outer, x, y);
		return this;
	}

	public Scroll visible(boolean visible) {
		ElementModifiers.visible(outer, visible);
		return this;
	}

	public Scroll children(@Nullable Runnable r) {
		ParentStack.Attacher attacher = (table, child) -> {
			Cell<?> cell = table.add(child);
			if (centered) {
				cell.center().top();
			} else {
				cell.top().left();
			}
			if (Ui.isExpanding(child)) {
				cell.growY();
			}
			cell.row();
			return cell;
		};
		ParentStack.push(content, attacher);
		try {
			if (r != null) {
				r.run();
			}
		} finally {
			ParentStack.pop();
		}
		ParentStack.attachToParent(outer);
		return this;
	}

	public Scroll add(Element child) {
		Cell<?> cell = content.add(child);
		if (centered) {
			cell.center().top();
		} else {
			cell.top().left();
		}
		cell.row();
		return this;
	}

	public Scroll name(String name) {
		ElementModifiers.name(outer, name);
		return this;
	}
}
