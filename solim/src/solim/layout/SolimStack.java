package solim.layout;

import arc.scene.Element;
import arc.scene.ui.layout.Stack;

/** Stack container: overlays children on top of each other. */
public final class SolimStack {
	private final Stack stack = new Stack();

	public Stack stack() {
		return stack;
	}

	public SolimStack add(Element child) {
		stack.add(child);
		return this;
	}
}
