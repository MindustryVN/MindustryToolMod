package solim.layout;

import arc.scene.Element;
import arc.scene.ui.layout.Stack;
import solim.core.Component;
import solim.modifier.ElementModifiers;

/** Stack container: overlays children on top of each other. */
public final class SolimStack implements Component {
	private final Stack stack = new Stack();
 
	public SolimStack() {
		this.stack.name = "solim-stack-stack";
	}

	public Stack stack() {
		return stack;
	}

	@Override
	public Element element() {
		return stack;
	}

	public SolimStack name(String name) {
		ElementModifiers.name(stack, name);
		return this;
	}

	public SolimStack add(Element child) {
		stack.add(child);
		return this;
	}
}
