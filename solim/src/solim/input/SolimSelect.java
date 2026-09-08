package solim.input;

import arc.scene.ui.TextButton;
import java.util.List;
import java.util.function.Consumer;
import solim.core.Component;
import solim.core.Disposable;
import solim.modifier.ElementModifiers;
import solim.signal.Effect;
import solim.signal.Signal;

/**
 * Select widget bound to Signal&lt;T&gt;. Uses TextButton as placeholder (Arc SelectBox unavailable
 * in this version). State held in signal; visual updates on change.
 */
public final class SolimSelect<T> implements Component, Disposable {
	private final TextButton selectBox = new TextButton("");
	private final Signal<T> signal;
	private final List<T> options;
	private int selectedIndex = 0;
	private Effect effect;

	public SolimSelect(Signal<T> signal, List<T> options) {
		this.signal = signal;
		this.options = options;
		if (signal.get() != null) {
			int idx = options.indexOf(signal.get());
			if (idx >= 0) selectedIndex = idx;
		}
		selectBox.setText(String.valueOf(signal.get()));
		selectBox.changed(() -> {
			// cycle through options
			selectedIndex = (selectedIndex + 1) % options.size();
			signal.set(options.get(selectedIndex));
		});
		this.effect = Effect.of((Consumer<Effect.Cleanup>) cleanup -> {
			T cur = signal.get();
			int idx = options.indexOf(cur);
			if (idx >= 0) selectedIndex = idx;
			selectBox.setText(String.valueOf(cur));
		});
	}

	public static <T> SolimSelect<T> of(Signal<T> signal, List<T> options) {
		return new SolimSelect<>(signal, options);
	}

	public TextButton selectBox() {
		return selectBox;
	}

	@Override
	public TextButton element() {
		return selectBox;
	}

	@Override
	public SolimSelect<T> name(String name) {
		ElementModifiers.name(selectBox, name);
		return this;
	}

	@Override
	public void dispose() {
		if (effect != null) effect.dispose();
	}
}
