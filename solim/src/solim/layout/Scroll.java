package solim.layout;

import arc.Core;
import arc.scene.Element;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.util.Nullable;
import solim.core.Component;
import solim.ui.ParentStack;

/** Scroll container wrapping a Table in a ScrollPane. */
public final class Scroll implements Component {
	public static final ParentStack.Attacher ATTACHER = (table, child) -> {
		Cell<?> cell = table.add(child).growX();
		cell.row();
		return cell;
	};

	private final Table outer = new Table();
	private final Table content = new Table();
	private final ScrollPane pane;

	public Scroll() {
		outer.top().left();
		content.top().left();
		if (Core.scene != null) {
			this.pane = outer.pane(content).scrollX(false).scrollY(true).grow().get();
			this.pane.setScrollingDisabled(true, false);
		} else {
			this.pane = null;
			outer.add(content);
		}
	}

	public Table outer() {
		return outer;
	}

	public Table content() {
		return content;
	}

	public ScrollPane pane() {
		return pane;
	}

	public Scroll grow() {
		outer.userObject = "expanding";
		return this;
	}

	public Scroll growX() {
		outer.userObject = "expanding";
		return this;
	}

	public Scroll growY() {
		outer.userObject = "expanding";
		return this;
	}

	public Scroll children(@Nullable Runnable r) {
		ParentStack.push(content, ATTACHER);
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
		content.add(child).growX().row();
		return this;
	}

	@Override
	public Element element() {
		return outer;
	}
}
