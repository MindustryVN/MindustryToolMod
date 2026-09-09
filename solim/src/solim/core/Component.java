package solim.core;

import arc.scene.Element;
import solim.modifier.ElementModifiers;

/** Minimal component abstraction. A component is a lifecycle resource that owns an Arc Element. */
public interface Component extends Disposable {
	Element element();

	@Override
	default void dispose() {}

	default Component name(String name) {
		ElementModifiers.name(element(), name);
		return this;
	}
}
