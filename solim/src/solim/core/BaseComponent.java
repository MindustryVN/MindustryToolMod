package solim.core;

import arc.scene.Element;

/**
 * Base class with lazy single-build semantics.
 */
public abstract class BaseComponent implements Component {
    private Element cached;

    protected abstract Element build();

    @Override
    public final Element element() {
        if (cached == null) {
            cached = build();
        }
        return cached;
    }

    @Override
    public void dispose() {
    }
}
