package solim.ui;

import arc.scene.Element;
import solim.core.Component;

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
            return ((Component) child).element();
        }
        throw new IllegalArgumentException("Cannot resolve child to Element: " + child);
    }
}
