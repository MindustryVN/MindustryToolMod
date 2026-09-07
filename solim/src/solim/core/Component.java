package solim.core;

import arc.scene.Element;

/** Minimal component abstraction. */
public interface Component {
	Element element();

	default void dispose() {}
}
