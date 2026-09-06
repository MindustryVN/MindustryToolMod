package solim.layout;

import arc.Core;
import arc.scene.Element;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Table;
import solim.core.Component;

/**
 * Scroll container wrapping a Table in a ScrollPane.
 */
public final class Scroll implements Component {
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
        outer.setFillParent(true);
        return this;
    }

    public Scroll growX() {
        outer.setFillParent(true);
        return this;
    }

    public Scroll growY() {
        outer.setFillParent(true);
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
