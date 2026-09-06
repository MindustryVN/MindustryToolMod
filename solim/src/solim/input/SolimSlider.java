package solim.input;

import arc.scene.ui.Slider;
import solim.core.Disposable;
import solim.signal.Effect;
import solim.signal.Signal;

/**
 * Slider widget bound to Signal&lt;Float&gt;.
 */
public final class SolimSlider implements Disposable {
    private final Slider slider = new Slider(0f, 1f, 0.1f, false);
    private final Signal<Float> signal;
    private Effect effect;
    private boolean updating = false;

    public SolimSlider(Signal<Float> signal, float min, float max, float step) {
        this.signal = signal;
        slider.setRange(min, max);
        slider.setStepSize(step);
        slider.setValue(signal.get());
        slider.changed(() -> {
            if (updating) return;
            if (Math.abs(slider.getValue() - signal.get()) > 0.0001f) {
                signal.set(slider.getValue());
            }
        });
        this.effect = Effect.of((java.util.function.Consumer<Effect.Cleanup>) cleanup -> {
            if (Math.abs(slider.getValue() - signal.get()) > 0.0001f) {
                updating = true;
                try {
                    slider.setValue(signal.get());
                } finally {
                    updating = false;
                }
            }
        });
    }

    public static SolimSlider of(Signal<Float> signal, float min, float max, float step) {
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
