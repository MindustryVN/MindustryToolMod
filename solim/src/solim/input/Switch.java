package solim.input;

import arc.scene.ui.TextButton;
import java.util.function.Consumer;
import solim.core.Component;
import solim.core.Disposable;
import solim.modifier.ElementModifiers;
import solim.signal.Effect;
import solim.signal.Signal;

/**
 * Switch widget bound to Signal&lt;Boolean&gt;. Uses TextButton as a visual toggle; state held in
 * signal.
 */
public final class Switch implements Component, Disposable {
	private final TextButton button = new TextButton("");
	private final Signal<Boolean> signal;
	private boolean state;
	private Disposable binding;

	{
		button.name = "solim-switch-switchBox";
	}

	public Switch(Signal<Boolean> signal) {
		this.signal = signal;
		this.state = Boolean.TRUE.equals(signal.peek());
		button.setText(state ? "ON" : "OFF");
		this.binding = new TwoWayBinding<>(
			signal,
			() -> state,
			val -> {
				state = Boolean.TRUE.equals(val);
				button.setText(state ? "ON" : "OFF");
			},
			onChange -> {
				button.changed(() -> {
					state = !state;
					onChange.run();
				});
				return () -> {};
			}
		);
		solim.core.ComponentContext.register(this);
	}

	public static Switch of(Signal<Boolean> signal) {
		return new Switch(signal);
	}

	public TextButton button() {
		return button;
	}

	@Override
	public TextButton element() {
		return button;
	}

	@Override
	public Switch name(String name) {
		ElementModifiers.name(button, name);
		return this;
	}

	@Override
	public void dispose() {
		if (binding != null) {
			binding.dispose();
			binding = null;
		}
	}
}
