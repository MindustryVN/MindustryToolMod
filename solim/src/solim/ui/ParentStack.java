package solim.ui;

import arc.scene.Element;
import arc.scene.ui.layout.Table;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Implicit parent stack for declarative UI construction with guaranteed cleanup.
 * Stack holds Table instances.
 */
public final class ParentStack {
    private static final Deque<Table> stack = new ArrayDeque<>();

    private ParentStack() {
    }

    public static void push(Table parent) {
        stack.push(parent);
    }

    public static Table pop() {
        if (!stack.isEmpty()) return stack.pop();
        return null;
    }

    public static Table current() {
        return stack.peek();
    }

    public static void clear() {
        stack.clear();
    }

    public static int size() {
        return stack.size();
    }

    /**
     * Attach child to current parent if one exists; otherwise no-op.
     */
    public static void attachToParent(Element child) {
        Table parent = stack.peek();
        if (parent != null) {
            parent.addChild(child);
        }
    }

    public static Element add(Object child) {
        Element e = ElementResolver.resolve(child);
        attachToParent(e);
        return e;
    }
}
