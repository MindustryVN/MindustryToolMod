package solim.core;

import arc.scene.Element;

/** Minimal component abstraction. A component is a lifecycle resource that owns an Arc Element. */
public interface Component extends Disposable {
	Element element();

	@Override
	default void dispose() {}

	default Component name(String name) {
		Element el = element();
		if (el != null) {
			el.name = name;
		}
		return this;
	}
}
