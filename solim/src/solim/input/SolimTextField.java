package solim.input;

import arc.scene.ui.TextField;
import solim.core.Disposable;
import solim.signal.Effect;
import solim.signal.Signal;

/**
 * TextField widget with two-way binding to a Signal&lt;String&gt;.
 * Equality guard prevents feedback loop.
 */
public final class SolimTextField implements Disposable {
    private final TextField field = new TextField();
    private final Signal<String> signal;
    private Effect effect;
    private boolean updating = false;

    public SolimTextField(Signal<String> signal) {
        this.signal = signal;
        field.setText(signal.get());
        // listener: type -> signal
        field.changed(() -> {
            if (updating) return;
            if (!field.getText().equals(signal.get())) {
                signal.set(field.getText());
            }
        });
        // effect: signal -> field
        this.effect = Effect.of((java.util.function.Consumer<Effect.Cleanup>) cleanup -> {
            if (!field.getText().equals(signal.get())) {
                updating = true;
                try {
                    field.setText(signal.get());
                } finally {
                    updating = false;
                }
            }
        });
    }

    public static SolimTextField of(Signal<String> signal) {
        return new SolimTextField(signal);
    }

    public TextField field() {
        return field;
    }

    @Override
    public void dispose() {
        if (effect != null) effect.dispose();
    }
}
