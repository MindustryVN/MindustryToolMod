package solim.signal;

import solim.core.Disposable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Reactive effect with auto-tracking and cleanup.
 */
public final class Effect implements ReactiveObserver, Disposable {

    public interface Cleanup {
        void add(Runnable runnable);
    }

    private final Runnable runnable;
    private final Supplier<Runnable> supplierWithCleanup;
    private final Consumer<Cleanup> cleanupConsumer;

    private final Set<Object> dependencies = new LinkedHashSet<>();
    private Set<Object> collecting = null;
    private final List<Runnable> cleanups = new ArrayList<>();

    private boolean disposed = false;
    private boolean running = false;
    private boolean pending = false;

    private Effect(Runnable runnable, Supplier<Runnable> supplier, Consumer<Cleanup> cleanupConsumer) {
        this.runnable = runnable;
        this.supplierWithCleanup = supplier;
        this.cleanupConsumer = cleanupConsumer;
    }

    public static Effect of(Runnable runnable) {
        Effect e = new Effect(runnable, null, null);
        e.runEffect();
        return e;
    }

    public static Effect of(Supplier<Runnable> supplier) {
        Effect e = new Effect(null, supplier, null);
        e.runEffect();
        return e;
    }

    public static Effect ofSupplier(Supplier<Runnable> supplier) {
        return of(supplier);
    }

    /**
     * For API {@code Effect.of(() -> { Subscription s=...; return s::dispose; })} use ofSupplier.
     * This overload handles cleanup consumer: {@code Effect.of(cleanup -> { ... cleanup.add(...); })}.
     */
    public static Effect of(Consumer<Cleanup> consumer) {
        Effect e = new Effect(null, null, consumer);
        e.runEffect();
        return e;
    }

    /**
     * Convenience alias matching requirement example: effect(() -> {...})
     */
    public static Effect effect(Runnable r) {
        return of(r);
    }

    private void runEffect() {
        if (disposed || running) {
            if (running) pending = true;
            return;
        }
        running = true;
        // run previous cleanups before re-running
        runCleanups();

        Set<Object> newDeps = new LinkedHashSet<>();
        collecting = newDeps;
        ReactiveContext.push(this);
        Runnable returned = null;
        try {
            if (runnable != null) {
                runnable.run();
            } else if (supplierWithCleanup != null) {
                returned = supplierWithCleanup.get();
            } else if (cleanupConsumer != null) {
                CleanupImpl ci = new CleanupImpl();
                cleanupConsumer.accept(ci);
            }
        } catch (Throwable e) {
            System.err.println("[Effect] error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            ReactiveContext.pop();
            collecting = null;
            running = false;
        }
        if (returned != null) {
            cleanups.add(returned);
        }
        updateDependencies(newDeps);
        if (pending) {
            pending = false;
            runEffect();
        }
    }

    private void runCleanups() {
        List<Runnable> copy = new ArrayList<>(cleanups);
        cleanups.clear();
        for (Runnable r : copy) {
            try {
                r.run();
            } catch (Throwable e) {
                System.err.println("[Effect] cleanup error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void updateDependencies(Set<Object> newDeps) {
        Set<Object> toRemove = new LinkedHashSet<>(dependencies);
        toRemove.removeAll(newDeps);
        for (Object dep : toRemove) {
            if (dep instanceof Signal) {
                ((Signal<?>) dep).removeObserver(this);
            } else if (dep instanceof Computed) {
                ((Computed<?>) dep).removeObserver(this);
            }
        }
        Set<Object> toAdd = new LinkedHashSet<>(newDeps);
        toAdd.removeAll(dependencies);
        for (Object dep : toAdd) {
            if (dep instanceof Signal) {
                ((Signal<?>) dep).addObserver(this);
            } else if (dep instanceof Computed) {
                ((Computed<?>) dep).addObserver(this);
            }
        }
        dependencies.clear();
        dependencies.addAll(newDeps);
    }

    @Override
    public void addDependency(Object observable) {
        if (disposed) return;
        if (collecting != null) {
            collecting.add(observable);
        }
    }

    @Override
    public void invalidate() {
        if (disposed) return;
        if (running) {
            pending = true;
            return;
        }
        runEffect();
    }

    public void dispose() {
        if (disposed) return;
        disposed = true;
        // unsubscribe from dependencies
        for (Object dep : new ArrayList<>(dependencies)) {
            if (dep instanceof Signal) {
                ((Signal<?>) dep).removeObserver(this);
            } else if (dep instanceof Computed) {
                ((Computed<?>) dep).removeObserver(this);
            }
        }
        dependencies.clear();
        runCleanups();
    }

    public boolean isDisposed() {
        return disposed;
    }

    // For tests
    int dependencyCount() {
        return dependencies.size();
    }

    private class CleanupImpl implements Cleanup {
        @Override
        public void add(Runnable runnable) {
            if (runnable != null) cleanups.add(runnable);
        }
    }
}
