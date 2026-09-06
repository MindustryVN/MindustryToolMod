package solim.ui;

import solim.signal.Effect;
import solim.signal.Computed;
import solim.signal.Signal;
import java.util.function.Consumer;

/**
 * Reactive property binding that applies a value to a target on subscription and
 * updates on every change. Returns an Effect for disposal.
 */
public final class Binding {
    private Binding() {
    }

    public static <T> Effect of(Consumer<T> target, Signal<T> source) {
        return Effect.of(() -> target.accept(source.get()));
    }

    public static <T> Effect of(Consumer<T> target, Computed<T> source) {
        return Effect.of(() -> target.accept(source.get()));
    }

    public static <T> Effect bind(Signal<T> source, Consumer<T> target) {
        return of(target, source);
    }
}
