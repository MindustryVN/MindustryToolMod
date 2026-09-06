package solim.core;

import arc.Events;
import arc.func.Cons;

/**
 * Utility for lifecycle-safe Arc event listening.
 */
public final class EventsUtil {
    private EventsUtil() {
    }

    public static <T> Disposable listen(Class<T> type, Cons<T> listener) {
        Events.on(type, listener);
        return () -> Events.remove(type, listener);
    }
}
