package solim.style;

import java.util.function.Consumer;
import solim.signal.Computed;
import solim.signal.Effect;
import solim.signal.Signal;

/** Static and reactive style application for widgets. No CSS, full re-apply on change. */
public final class StyleBinding {
	private StyleBinding() {}

	/** Static style apply. */
	public static <T> void apply(T target, Style style, StyleApplier<T> applier) {
		applier.apply(target, style);
	}

	/** Reactive style binding via Signal. */
	public static <T> Effect bind(Signal<Style> style, T target, StyleApplier<T> applier) {
		return Effect.of((Consumer<Effect.Cleanup>) cleanup -> {
			Style current = style.get();
			applier.apply(target, current);
		});
	}

	/** Reactive style binding via Computed. */
	public static <T> Effect bind(Computed<Style> style, T target, StyleApplier<T> applier) {
		return Effect.of((Consumer<Effect.Cleanup>) cleanup -> {
			Style current = style.get();
			applier.apply(target, current);
		});
	}

	@FunctionalInterface
	public interface StyleApplier<T> {
		void apply(T target, Style style);
	}
}
