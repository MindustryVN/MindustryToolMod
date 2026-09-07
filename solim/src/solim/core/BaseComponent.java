package solim.core;

import arc.Events;
import arc.func.Cons;
import arc.func.Func;
import arc.scene.Element;
import arc.scene.ui.layout.Table;
import arc.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import solim.signal.Signal;
import solim.ui.ParentStack;

/**
 * Base class with lazy single-build semantics, ambient lifecycle resource management, and automatic
 * child registration.
 */
public abstract class BaseComponent implements Component {
	private Element cached;
	private final List<Disposable> disposables = new ArrayList<>();
	private boolean disposed = false;

	public BaseComponent() {
		ComponentContext.registerChild(this);
		Table parent = ParentStack.current();
		if (parent != null) {
			ParentStack.registerPendingComponent(this, parent);
		}
	}

	protected abstract Element build();

	@Override
	public final Element element() {
		if (cached == null) {
			ComponentContext.push(this);
			try {
				cached = build();
			} finally {
				ComponentContext.pop();
			}
		}
		return cached;
	}

	/** Registers a disposable resource with this component's lifecycle. */
	public <T extends Disposable> T registerDisposable(T disposable) {
		if (disposable != null) {
			disposables.add(disposable);
		}
		return disposable;
	}

	/**
	 * Internal/legacy helper for registering a disposable. Application components should rely on
	 * ambient automatic registration.
	 */
	protected <T extends Disposable> T own(T disposable) {
		return registerDisposable(disposable);
	}

	/**
	 * Internal/legacy helper for registering a child component. Application components should rely on
	 * ambient automatic registration.
	 */
	protected <T extends Component> T ownChild(T child) {
		if (child != null) {
			disposables.add(child::dispose);
		}
		return child;
	}

	/**
	 * Registers an Arc event listener that automatically unregisters when this component is disposed.
	 */
	public <T> Disposable listen(Class<T> eventType, Cons<T> listener) {
		Events.on(eventType, listener);
		Disposable d = () -> Events.remove(eventType, listener);
		disposables.add(d);
		return d;
	}

	/**
	 * Creates a reactive signal initialized from the supplier that recalculates whenever the
	 * specified Arc event fires. Automatically unregisters when this component is disposed.
	 */
	public <E, T> Signal<T> createSignal(Class<E> eventType, Supplier<T> supplier) {
		Signal<T> signal = Signal.of(supplier.get());
		listen(eventType, e -> signal.set(supplier.get()));
		return signal;
	}

	/**
	 * Creates a reactive signal that updates with mapped event data whenever the specified Arc event
	 * fires. Automatically unregisters when this component is disposed.
	 */
	public <E, T> Signal<T> createSignal(Class<E> eventType, Func<E, T> mapper, T initial) {
		Signal<T> signal = Signal.of(initial);
		listen(eventType, e -> signal.set(mapper.get(e)));
		return signal;
	}

	/**
	 * Creates a reactive signal initialized from the supplier that recalculates whenever the callback
	 * registrar invokes the given callback (e.g. {@code element::resized}).
	 */
	public <T> Signal<T> createSignal(Consumer<Runnable> callbackRegistrar, Supplier<T> supplier) {
		Signal<T> signal = Signal.of(supplier.get());
		if (callbackRegistrar != null) {
			callbackRegistrar.accept(() -> signal.set(supplier.get()));
		}
		return signal;
	}

	public boolean isDisposed() {
		return disposed;
	}

	/** Subclass hook executed during disposal before owned resources are disposed. */
	protected void onDispose() {}

	@Override
	public void dispose() {
		if (disposed) {
			return;
		}
		disposed = true;
		onDispose();
		for (Disposable d : disposables) {
			try {
				d.dispose();
			} catch (Throwable t) {
				Log.err("Error disposing resource in " + getClass().getSimpleName(), t);
			}
		}
		disposables.clear();
	}
}
