package solim.layout;

import arc.scene.Element;
import arc.scene.ui.layout.Stack;
import solim.core.Component;
import solim.modifier.ElementModifiers;

/** Stack container: overlays children on top of each other. */
public final class SolimStack implements Component, LayoutModifiers<SolimStack> {
    private final Stack stack = new Stack();
    private final SizeConstraints constraints = new SizeConstraints();

    public SolimStack() {
        this.stack.name = "solim-stack-stack";
    }

    public Stack stack() {
        return stack;
    }

    @Override
    public Element element() {
        return stack;
    }

    public SolimStack name(String name) {
        ElementModifiers.name(stack, name);
        return this;
    }

    public SolimStack add(Element child) {
        if (child != null) {
            stack.add(child);
        }
        return this;
    }

    public SolimStack add(Component child) {
        if (child != null) {
            stack.add(child.element());
        }
        return this;
    }

    public SolimStack layer(Runnable r) {
        Row layerRow = new Row();
        layerRow.children(r);
        stack.add(layerRow.element());
        return this;
    }

    @Override
    public SizeConstraints sizeConstraints() {
        return constraints;
    }
}
