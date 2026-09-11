package solim.core;

import arc.Events;
import arc.func.Cons;
import arc.func.Func;
import arc.scene.Element;
import arc.util.Log;
import arc.util.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import solim.modifier.ElementModifiers;
import solim.signal.Signal;

/**
 * Base class with lazy single-build semantics, ambient lifecycle resource management, and automatic
 * child registration.
 *
 * <p>Lifecycle rules:
 * <ul>
 *   <li>Resources are disposed in reverse registration order (LIFO).</li>
 *   <li>Calling {@link #element()} on a disposed component throws {@link IllegalStateException}.</li>
 *   <li>If {@link #build()} throws, all resources registered up to that point are disposed.</li>
 *   <li>If {@link #build()} returns {@code null}, an {@link IllegalStateException} is thrown and
 *       resources are disposed.</li>
 * </ul>
 */
public abstract class BaseComponent implements Component {
	private Element cached;
	private @Nullable String componentName;
	private final List<Disposable> disposables = new ArrayList<>();
	private boolean disposed = false;

	public BaseComponent() {
		ComponentContext.registerChild(this);
	}

	protected abstract Element build();

	private void checkNotDisposed() {
		if (disposed) {
			throw new IllegalStateException(
				"Cannot use disposed component: " + getClass().getSimpleName()
			);
		}
	}

	private void applyName(Element element) {
		if (componentName != null) {
			ElementModifiers.name(element, componentName);
		} else if (element.name == null) {
			String compName = getClass().getSimpleName();
			if (compName.isEmpty()) {
				compName = "component";
			}
			String elemName = element.getClass().getSimpleName();
			if (elemName.isEmpty()) {
				elemName = "element";
			}
			ElementModifiers.name(element, "solim-" + compName.toLowerCase() + "-" + elemName.toLowerCase());
		}
	}

	@Override
	public BaseComponent name(String name) {
		this.componentName = name;
		if (cached != null) {
			ElementModifiers.name(cached, name);
		}
		return this;
	}

	@Override
	public final Element element() {
		checkNotDisposed();
		if (cached != null) {
			return cached;
		}
		ComponentContext.push(this);
		try {
			cached = build();
			if (cached == null) {
				throw new IllegalStateException(
					"build() returned null for " + getClass().getSimpleName()
				);
			}
			applyName(cached);
			return cached;
		} catch (Throwable throwable) {
			dispose();
			throw throwable;
		} finally {
			ComponentContext.pop();
		}
	}

	/**
	 * Owns a disposable resource: adds it to this component's disposal list and returns it. The
	 * resource will be disposed (in reverse registration order) when this component is disposed.
	 * Safe to call with {@code null}.
	 */
	public <T extends Disposable> T own(T disposable) {
		if (disposable != null) {
			disposables.add(disposable);
		}
		return disposable;
	}

	/**
	 * Registers an Arc event listener that automatically unregisters when this component is disposed.
	 */
	public <T> Disposable listen(Class<T> eventType, Cons<T> listener) {
		Events.on(eventType, listener);
		Disposable d = () -> Events.remove(eventType, listener);
		own(d);
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
	 * Creates a reactive signal initialized from the supplier. The {@code registrar} function
	 * installs a callback and returns a {@link Disposable} that unregisters it. That disposable is
	 * owned by this component, so the callback is automatically unregistered when the component is
	 * disposed.
	 *
	 * <p>If the underlying API cannot provide real unsubscription, the registrar should return a
	 * no-op {@code Disposable} and document this limitation.
	 */
	public <T> Signal<T> createSignal(Function<Runnable, Disposable> registrar, Supplier<T> supplier) {
		Signal<T> signal = Signal.of(supplier.get());
		if (registrar != null) {
			own(registrar.apply(() -> signal.set(supplier.get())));
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
		// Dispose in reverse registration order (LIFO) so later resources are freed first.
		for (int i = disposables.size() - 1; i >= 0; i--) {
			try {
				disposables.get(i).dispose();
			} catch (Throwable t) {
				Log.err("Error disposing resource in " + getClass().getSimpleName(), t);
			}
		}
		disposables.clear();
	}
}
