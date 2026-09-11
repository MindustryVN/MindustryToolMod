package solim.ui;

import arc.scene.Element;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.util.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import solim.core.Component;
import solim.core.ComponentContext;
import solim.core.Disposable;
import solim.display.SolimImage;
import solim.display.Text;

/**
 * Implicit parent stack for declarative UI construction with guaranteed cleanup. Supports
 * customizable cell attachment strategies per container.
 */
public final class ParentStack {

	@FunctionalInterface
	public interface Attacher {
		Cell<?> attach(Table parent, Element child);
	}

	public static class Entry {
		public final Table table;
		public final Attacher attacher;

		public Entry(Table table, Attacher attacher) {
			this.table = table;
			this.attacher = attacher != null ? attacher : Table::add;
		}
	}

	private static final Deque<Entry> stack = new ArrayDeque<>();
	private static final Map<Table, List<Component>> pendingComponents = new HashMap<>();
	private static final Map<Table, Attacher> tableAttachers = new HashMap<>();

	private ParentStack() {}

	public static void push(Table parent) {
		push(parent, null);
	}

	public static void push(Table parent, Attacher attacher) {
		if (parent != null) {
			stack.push(new Entry(parent, attacher));
			if (attacher != null) {
				tableAttachers.put(parent, attacher);
			}
		}
	}

	public static Table pop() {
		if (!stack.isEmpty()) {
			Entry popped = stack.pop();
			attachPendingComponents(popped.table);
			tableAttachers.remove(popped.table);
			return popped.table;
		}
		return null;
	}

	public static Table current() {
		return stack.isEmpty() ? null : stack.peek().table;
	}

	public static @Nullable Table find(java.util.function.Predicate<Table> predicate) {
		if (predicate == null) return null;
		for (Entry entry : stack) {
			if (predicate.test(entry.table)) {
				return entry.table;
			}
		}
		return null;
	}

	public static void clear() {
		stack.clear();
		pendingComponents.clear();
		tableAttachers.clear();
	}

	public static int size() {
		return stack.size();
	}

	/**
	 * Executes the given supplier in an isolated ParentStack context where no parent is on the stack.
	 * Any components created within the supplier will not attach to parents on the outer stack,
	 * but internal hierarchies within the supplier remain fully functional.
	 */
	public static <T> T isolate(Supplier<T> supplier) {
		if (supplier == null) {
			return null;
		}
		Deque<Entry> saved = new ArrayDeque<>(stack);
		stack.clear();
		try {
			return supplier.get();
		} finally {
			stack.clear();
			stack.addAll(saved);
		}
	}

	/**
	 * Executes the given runnable in an isolated ParentStack context where no parent is on the stack.
	 */
	public static void isolate(Runnable runnable) {
		if (runnable == null) {
			return;
		}
		Deque<Entry> saved = new ArrayDeque<>(stack);
		stack.clear();
		try {
			runnable.run();
		} finally {
			stack.clear();
			stack.addAll(saved);
		}
	}

	/**
	 * Registers a component to be attached when its parent is popped, ensuring the component is fully
	 * constructed before element() is invoked.
	 */
	public static void registerPendingComponent(Component component, Table parent) {
		if (component != null && parent != null) {
			List<Component> list = pendingComponents.get(parent);
			if (list == null) {
				list = new ArrayList<>();
				pendingComponents.put(parent, list);
			}
			list.add(component);
		}
	}

	/** Attaches all pending components registered for the given table. */
	public static void attachPendingComponents(Table parent) {
		if (parent == null) {
			return;
		}
		List<Component> list = pendingComponents.remove(parent);
		Attacher attacher = tableAttachers.get(parent);
		if (list != null) {
			for (Component comp : list) {
				Element el = isolate(comp::element);
				if (el != null && el.parent == null) {
					doAttach(parent, el, attacher);
				}
				if (comp instanceof Text) {
					((Text) comp).applySpacing();
				} else if (comp instanceof SolimImage) {
					((SolimImage) comp).applySpacing();
				}
			}
		}
	}

	/** Attach child to current parent if one exists; otherwise no-op. */
	public static void attachToParent(Element child) {
		if (child == null || stack.isEmpty()) {
			return;
		}
		Entry entry = stack.peek();
		doAttach(entry.table, child, entry.attacher);
	}

	private static void doAttach(Table parent, Element child, Attacher attacher) {
		if (parent != null && child != null) {
			if (child.parent != parent && !parent.getChildren().contains(child, true)) {
				Cell<?> cell;
				if (attacher != null) {
					cell = attacher.attach(parent, child);
				} else {
					cell = parent.add(child);
				}
				if (cell != null) {
					solim.layout.SizeConstraints constraints = solim.layout.SizeConstraints.find(child);
					if (constraints != null) {
						List<Disposable> effects = constraints.applyToCell(cell);
						for (Disposable effect : effects) {
							ComponentContext.register(effect);
						}
					}
				}
			}
		}
	}

	public static Element add(Object child) {
		Element e = ElementResolver.resolve(child);
		attachToParent(e);
		return e;
	}
}
