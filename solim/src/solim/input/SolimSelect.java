package solim.input;

import arc.scene.ui.SelectBox;
import solim.core.Disposable;
import solim.signal.Effect;
import solim.signal.Signal;
import java.util.List;

/**
 * Select widget bound to Signal&lt;T&gt;.
 */
public final class SolimSelect<T> implements Disposable {
    private final SelectBox<T> selectBox;
    private final Signal<T> signal;
    private final List<T> options;
    private Effect effect;
    private boolean updating = false;

    public SolimSelect(Signal<T> signal, List<T> options) {
        this.signal = signal;
        this.options = options;
        this.selectBox = new SelectBox<>(options.toArray((T[]) new Object[0]));
        this.selectBox.setSelected(signal.get());
        selectBox.changed(() -> {
            if (updating) return;
            T sel = selectBox.getSelected();
            if (sel == null ? signal.get() != null : !sel.equals(signal.get())) {
                signal.set(sel);
            }
        });
        this.effect = Effect.of((java.util.function.Consumer<Effect.Cleanup>) cleanup -> {
            T sel = selectBox.getSelected();
            if (sel == null ? signal.get() != null : !sel.equals(signal.get())) {
                updating = true;
                try {
                    selectBox.setSelected(signal.get());
                } finally {
                    updating = false;
                }
            }
        });
    }

    public static <T> SolimSelect<T> of(Signal<T> signal, List<T> options) {
        return new SolimSelect<>(signal, options);
    }

    public SelectBox<T> selectBox() {
        return selectBox;
    }

    @Override
    public void dispose() {
        if (effect != null) effect.dispose();
    }
}
