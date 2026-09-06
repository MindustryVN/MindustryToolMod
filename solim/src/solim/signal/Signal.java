package solim.signal;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Mutable reactive value.
 */
public final class Signal<T> implements Readable<T> {
    private T value;
    private final List<Consumer<T>> listeners = new ArrayList<>();
    private final Set<ReactiveObserver> observers = new LinkedHashSet<>();

    private Signal(T initial) {
        this.value = initial;
    }

    public static <T> Signal<T> of(T initial) {
        return new Signal<>(initial);
    }

    public static <T> Computed<T> computed(Supplier<T> supplier) {
        return new Computed<>(supplier);
    }

    @Override
    public T get() {
        ReactiveContext.track(this);
        return value;
    }

    public void set(T newValue) {
        if (Objects.equals(value, newValue)) return;
        this.value = newValue;
        // notify listeners
        List<Consumer<T>> copy = new ArrayList<>(listeners);
        for (Consumer<T> c : copy) {
            try {
                c.accept(value);
            } catch (Throwable e) {
                // log but continue
                System.err.println("[Signal] listener error: " + e.getMessage());
                e.printStackTrace();
            }
        }
        // notify observers (Computeds/Effects)
        Set<ReactiveObserver> obsCopy = new LinkedHashSet<>(observers);
        for (ReactiveObserver o : obsCopy) {
            try {
                o.invalidate();
            } catch (Throwable e) {
                System.err.println("[Signal] observer invalidate error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void update(Function<T, T> updater) {
        set(updater.apply(value));
    }

    public Subscription subscribe(Consumer<T> listener) {
        listeners.add(listener);
        AtomicBoolean disposed = new AtomicBoolean(false);
        return new Subscription() {
            @Override
            public void dispose() {
                if (disposed.compareAndSet(false, true)) {
                    listeners.remove(listener);
                }
            }

            @Override
            public boolean isDisposed() {
                return disposed.get();
            }
        };
    }

    public <R> Computed<R> map(Function<T, R> mapper) {
        return Signal.computed(() -> mapper.apply(get()));
    }

    // Observable support
    void addObserver(ReactiveObserver observer) {
        observers.add(observer);
    }

    void removeObserver(ReactiveObserver observer) {
        observers.remove(observer);
    }

    // For testing: listener/observer counts
    int listenerCount() {
        return listeners.size();
    }

    int observerCount() {
        return observers.size();
    }
}
