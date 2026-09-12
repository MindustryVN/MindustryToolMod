package solim.signal;

import arc.Events;
import arc.util.Log;
import arc.util.Nullable;
import java.util.ArrayDeque;
import java.util.Queue;
import mindustry.game.EventType.Trigger;

/**
 * Internal single-threaded dispatcher that batches and deduplicates reactive {@link Effect}
 * invalidations to run at most once per Mindustry frame.
 */
public final class SignalDispatcher {
	private static final int MAX_FLUSH_ITERATIONS = 100;
	private static final Queue<Effect> queue = new ArrayDeque<>();
	private static boolean registered = false;
	private static boolean flushing = false;

	static {
		register();
	}

	private SignalDispatcher() {}

	/**
	 * Registers the frame flush callback with Mindustry's {@link Trigger#update} event.
	 * This method is idempotent and safe to call repeatedly.
	 */
	public static void register() {
		if (registered) return;
		registered = true;
		Events.run(Trigger.update, SignalDispatcher::flush);
	}

	/**
	 * Ensures that the dispatcher is hooked into the frame lifecycle.
	 */
	public static void ensureRegistered() {
		if (!registered) {
			register();
		}
	}

	/**
	 * Enqueues an invalidated effect for batched execution on the next frame flush.
	 * If the effect is disposed, it will not be enqueued.
	 */
	public static void enqueue(@Nullable Effect effect) {
		if (effect == null || effect.isDisposed()) return;
		ensureRegistered();
		queue.add(effect);
	}

	/**
	 * Flushes and executes all pending effects in FIFO order on the caller's thread.
	 * Newly invalidated effects dirtied during execution are drained within the same flush
	 * up to a safety threshold to prevent infinite cycles.
	 */
	public static void flush() {
		if (flushing) return;
		flushing = true;
		try {
			int iterations = 0;
			while (!queue.isEmpty()) {
				if (++iterations > MAX_FLUSH_ITERATIONS) {
					Log.err("[Solim] Infinite reactive loop detected in SignalDispatcher (exceeded @ iterations). Clearing @ pending effects.",
							MAX_FLUSH_ITERATIONS, queue.size());
					for (Effect remaining : queue) {
						remaining.clearPending();
					}
					queue.clear();
					break;
				}

				Effect effect = queue.poll();
				if (effect == null) continue;

				if (effect.isDisposed()) {
					effect.clearPending();
					continue;
				}

				try {
					effect.runPending();
				} catch (Throwable t) {
					Log.err("[Solim] Error executing reactive effect", t);
				}
			}
		} finally {
			flushing = false;
		}
	}

	/** Returns the number of currently pending effects in the queue. */
	public static int size() {
		return queue.size();
	}

	/** Returns true if the dispatcher is currently executing a flush cycle. */
	public static boolean isFlushing() {
		return flushing;
	}

	/** Returns whether the frame callback has been registered. */
	public static boolean isRegistered() {
		return registered;
	}

	/**
	 * Resets dispatcher state for isolated test execution.
	 */
	static void resetForTests() {
		for (Effect remaining : queue) {
			remaining.clearPending();
		}
		queue.clear();
		flushing = false;
	}
}
