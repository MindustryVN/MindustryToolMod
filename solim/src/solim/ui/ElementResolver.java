package solim.ui;

import arc.scene.Element;
import solim.core.Component;
import solim.core.ComponentContext;

/**
 * Resolves children that may be Element or Component.
 */
public final class ElementResolver {
    private ElementResolver() {
    }

    public static Element resolve(Object child) {
        if (child instanceof Element) {
            return (Element) child;
        }
        if (child instanceof Component) {
            Component c = (Component) child;
            ComponentContext.registerChild(c);
            return c.element();
        }
        throw new IllegalArgumentException("Cannot resolve child to Element: " + child);
    }
}
