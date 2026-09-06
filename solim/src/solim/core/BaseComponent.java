package solim.core;

import arc.Events;
import arc.func.Cons;
import arc.scene.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class with lazy single-build semantics and lifecycle resource management.
 */
public abstract class BaseComponent implements Component {
    private Element cached;
    private final List<Disposable> disposables = new ArrayList<>();
    private boolean disposed = false;

    protected abstract Element build();

    @Override
    public final Element element() {
        if (cached == null) {
            cached = build();
        }
        return cached;
    }

    /**
     * Registers a disposable resource to be automatically disposed when this component is disposed.
     */
    protected <T extends Disposable> T own(T disposable) {
        if (disposable != null) {
            disposables.add(disposable);
        }
        return disposable;
    }

    /**
     * Registers a child component whose lifecycle should be tied to this parent component.
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
    protected <T> Disposable listen(Class<T> eventType, Cons<T> listener) {
        Events.on(eventType, listener);
        Disposable d = () -> Events.remove(eventType, listener);
        disposables.add(d);
        return d;
    }

    public boolean isDisposed() {
        return disposed;
    }

    /**
     * Subclass hook executed during disposal before owned resources are disposed.
     */
    protected void onDispose() {
    }

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
                arc.util.Log.err("Error disposing resource in " + getClass().getSimpleName(), t);
            }
        }
        disposables.clear();
    }
}
