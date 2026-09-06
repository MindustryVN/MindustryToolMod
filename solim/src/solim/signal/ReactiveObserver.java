package solim.signal;

/**
 * Internal observer abstraction implemented by Computed and Effect.
 * Used by ReactiveContext to collect dependencies.
 */
public interface ReactiveObserver {
    /**
     * Called when a Signal/Computed is read while this observer is active.
     * Implementation should record dependency and subscribe for future invalidation.
     */
    void addDependency(Object observable);

    /**
     * Called when a dependency changes and this observer should become dirty or re-run.
     */
    void invalidate();
}
