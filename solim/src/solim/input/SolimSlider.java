package solim.input;

import arc.scene.ui.Slider;
import java.util.function.Consumer;
import solim.core.ComponentContext;
import solim.core.Disposable;
import solim.signal.Effect;
import solim.signal.Signal;

/** Slider widget bound to Signal&lt;Float&gt;. */
public final class SolimSlider implements Disposable {
	private final Slider slider = new Slider(0f, 1f, 0.1f, false);
	private Effect effect;
	private boolean updating = false;

	public SolimSlider(Signal<Float> signal, float min, float max, float step) {
		slider.setRange(min, max);
		slider.setStepSize(step);
		slider.setValue(signal.get());
		slider.changed(() -> {
			if (updating) return;
			if (Math.abs(slider.getValue() - signal.get()) > 0.0001f) {
				signal.set(slider.getValue());
			}
		});
		this.effect = Effect.of((Consumer<Effect.Cleanup>) cleanup -> {
			if (Math.abs(slider.getValue() - signal.get()) > 0.0001f) {
				updating = true;
				try {
					slider.setValue(signal.get());
				} finally {
					updating = false;
				}
			}
		});
		ComponentContext.register(this);
	}

	public SolimSlider(Signal<Integer> signal, int min, int max, int step) {
		slider.setRange(min, max);
		slider.setStepSize(step);
		slider.setValue(signal.get());
		slider.changed(() -> {
			if (updating) return;
			int intVal = Math.round(slider.getValue());
			if (intVal != signal.get()) {
				signal.set(intVal);
			}
		});
		this.effect = Effect.of((Consumer<Effect.Cleanup>) cleanup -> {
			int sigVal = signal.get();
			if (Math.round(slider.getValue()) != sigVal) {
				updating = true;
				try {
					slider.setValue(sigVal);
				} finally {
					updating = false;
				}
			}
		});
		ComponentContext.register(this);
	}

	public static SolimSlider of(Signal<Float> signal, float min, float max, float step) {
		return new SolimSlider(signal, min, max, step);
	}

	public static SolimSlider of(Signal<Integer> signal, int min, int max, int step) {
		return new SolimSlider(signal, min, max, step);
	}

	public Slider slider() {
		return slider;
	}

	@Override
	public void dispose() {
		if (effect != null) effect.dispose();
	}
}
