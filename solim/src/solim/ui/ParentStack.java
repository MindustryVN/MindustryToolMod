package solim.ui;

import arc.scene.Element;
import arc.scene.ui.layout.Table;
import solim.core.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implicit parent stack for declarative UI construction with guaranteed cleanup.
 * Stack holds Table instances.
 */
public final class ParentStack {
    private static final Deque<Table> stack = new ArrayDeque<>();
    private static final Map<Table, List<Component>> pendingComponents = new HashMap<>();

    private ParentStack() {
    }

    public static void push(Table parent) {
        stack.push(parent);
    }

    public static Table pop() {
        if (!stack.isEmpty()) {
            Table popped = stack.pop();
            attachPendingComponents(popped);
            return popped;
        }
        return null;
    }

    public static Table current() {
        return stack.peek();
    }

    public static void clear() {
        stack.clear();
        pendingComponents.clear();
    }

    public static int size() {
        return stack.size();
    }

    /**
     * Registers a component to be attached when its parent is popped,
     * ensuring the component is fully constructed before element() is invoked.
     */
    public static void registerPendingComponent(Component component, Table parent) {
        if (component != null && parent != null) {
            List<Component> list = pendingComponents.get(parent);
            if (list == null) {
                list = new ArrayList<>();
                pendingComponents.put(parent, list);
            }
            list.add(component);
        }
    }

    /**
     * Attaches all pending components registered for the given table.
     */
    public static void attachPendingComponents(Table parent) {
        if (parent == null) {
            return;
        }
        List<Component> list = pendingComponents.remove(parent);
        if (list != null) {
            for (Component comp : list) {
                Element el = comp.element();
                if (el != null && el.parent == null) {
                    parent.addChild(el);
                }
            }
        }
    }

    /**
     * Attach child to current parent if one exists; otherwise no-op.
     */
    public static void attachToParent(Element child) {
        Table parent = stack.peek();
        if (parent != null && child != null) {
            if (child.parent != parent && !parent.getChildren().contains(child, true)) {
                parent.addChild(child);
            }
        }
    }

    public static Element add(Object child) {
        Element e = ElementResolver.resolve(child);
        attachToParent(e);
        return e;
    }
}
