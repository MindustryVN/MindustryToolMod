package solim.input;

import arc.scene.ui.Button;
import solim.core.Disposable;
import solim.signal.Effect;
import solim.signal.Signal;

/**
 * Switch widget bound to Signal&lt;Boolean&gt;.
 * Uses Button as a visual toggle; state held in signal.
 */
public final class Switch implements Disposable {
    private final Button button = new Button();
    private final Signal<Boolean> signal;
    private Effect effect;
    private boolean updating = false;

    public Switch(Signal<Boolean> signal) {
        this.signal = signal;
        updateVisual();
        button.changed(() -> {
            if (updating) return;
            signal.set(!signal.get());
        });
        this.effect = Effect.of((java.util.function.Consumer<Effect.Cleanup>) cleanup -> {
            updateVisual();
        });
    }

    public static Switch of(Signal<Boolean> signal) {
        return new Switch(signal);
    }

    private void updateVisual() {
        updating = true;
        try {
            button.setText(signal.get() ? "ON" : "OFF");
        } finally {
            updating = false;
        }
    }

    public Button button() {
        return button;
    }

    @Override
    public void dispose() {
        if (effect != null) effect.dispose();
    }
}
