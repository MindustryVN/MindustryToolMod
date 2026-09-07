package solim.core;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Ambient component build context tracking the active building component. Allows child components,
 * disposables, and reactive bindings instantiated during build() to be automatically registered and
 * owned without manual own() calls.
 */
public final class ComponentContext {
	private static final Deque<BaseComponent> stack = new ArrayDeque<>();
	private static int paused = 0;

	private ComponentContext() {}

	public static void push(BaseComponent component) {
		stack.push(component);
	}

	public static BaseComponent pop() {
		if (!stack.isEmpty()) {
			return stack.pop();
		}
		return null;
	}

	public static BaseComponent current() {
		if (paused > 0) {
			return null;
		}
		return stack.peek();
	}

	public static void pause() {
		paused++;
	}

	public static void resume() {
		if (paused > 0) {
			paused--;
		}
	}

	public static void clear() {
		stack.clear();
		paused = 0;
	}

	public static int size() {
		return stack.size();
	}

	/** Registers a disposable with the currently active building component, if any. */
	public static <T extends Disposable> T register(T disposable) {
		if (paused > 0 || disposable == null) {
			return disposable;
		}
		BaseComponent current = stack.peek();
		if (current != null) {
			current.registerDisposable(disposable);
		}
		return disposable;
	}

	/** Registers a child component with the currently active building component, if any. */
	public static <T extends Component> T registerChild(T child) {
		if (paused > 0 || child == null) {
			return child;
		}
		BaseComponent current = stack.peek();
		if (current != null && current != child) {
			current.registerDisposable(child::dispose);
		}
		return child;
	}
}
