package solim.core;

import arc.scene.Element;
import solim.modifier.ElementModifiers;

/** Minimal component abstraction. */
public interface Component {
	Element element();

	default void dispose() {}

	default Component name(String name) {
		ElementModifiers.name(element(), name);
		return this;
	}
}
