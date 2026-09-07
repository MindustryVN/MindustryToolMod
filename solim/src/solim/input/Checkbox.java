package solim.input;

import arc.scene.ui.CheckBox;
import java.util.function.Consumer;
import solim.core.Disposable;
import solim.signal.Effect;
import solim.signal.Signal;

/** Checkbox widget bound to Signal&lt;Boolean&gt;. */
public final class Checkbox implements Disposable {
	private final CheckBox checkBox = new CheckBox("");
	private final Signal<Boolean> signal;
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
	}

	public static Checkbox of(String label, Signal<Boolean> signal) {
		return new Checkbox(label, signal);
	}

	public CheckBox checkBox() {
		return checkBox;
	}

	@Override
	public void dispose() {
		if (effect != null) effect.dispose();
	}
}
