package solim.input;

import arc.scene.ui.CheckBox;
import arc.util.Nullable;
import java.util.function.Consumer;
import solim.core.ComponentContext;
import solim.core.Disposable;
import solim.signal.Effect;
import solim.signal.Signal;

/** Checkbox widget bound to Signal&lt;Boolean&gt;. */
public final class Checkbox implements Disposable {
	private final CheckBox checkBox = new CheckBox("");
	private final @Nullable Signal<Boolean> signal;
	private Effect effect;
	private boolean updating = false;

	public Checkbox(String label, Signal<Boolean> signal) {
		this.signal = signal;
		if (label != null) checkBox.setText(label);
		checkBox.setChecked(signal.get());
		checkBox.changed(() -> {
			if (updating) return;
			signal.set(checkBox.isChecked());
		});
		this.effect = Effect.of((Consumer<Effect.Cleanup>) cleanup -> {
			if (checkBox.isChecked() != signal.get()) {
				updating = true;
				try {
					checkBox.setChecked(signal.get());
				} finally {
					updating = false;
				}
			}
		});
		ComponentContext.register(this);
	}

	public Checkbox(String label, boolean initial, Consumer<Boolean> onChanged) {
		this.signal = null;
		if (label != null) checkBox.setText(label);
		checkBox.setChecked(initial);
		checkBox.changed(() -> {
			if (onChanged != null) {
				onChanged.accept(checkBox.isChecked());
			}
		});
		ComponentContext.register(this);
	}

	public static Checkbox of(String label, Signal<Boolean> signal) {
		return new Checkbox(label, signal);
	}

	public static Checkbox of(String label, boolean initial, Consumer<Boolean> onChanged) {
		return new Checkbox(label, initial, onChanged);
	}

	public CheckBox checkBox() {
		return checkBox;
	}

	@Override
	public void dispose() {
		if (effect != null) effect.dispose();
	}
}
