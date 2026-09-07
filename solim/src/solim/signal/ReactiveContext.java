package solim.signal;

import java.util.ArrayDeque;
import java.util.Deque;

/** Stack-based dependency tracking for single-threaded UI. No ThreadLocal by design. */
public final class ReactiveContext {
	private static final Deque<ReactiveObserver> stack = new ArrayDeque<>();

	private ReactiveContext() {}

	public static void push(ReactiveObserver observer) {
		stack.push(observer);
	}

	public static void pop() {
		if (!stack.isEmpty()) stack.pop();
	}

	public static ReactiveObserver current() {
		return stack.peek();
	}

	/** Called from Signal.get() / Computed.get() to register dependency with current observer. */
	public static void track(Object observable) {
		ReactiveObserver cur = current();
		if (cur != null) {
			cur.addDependency(observable);
		}
	}

	/** For tests: clear stack */
	public static void clear() {
		stack.clear();
	}

	/** For tests: size */
	public static int size() {
		return stack.size();
	}
}
