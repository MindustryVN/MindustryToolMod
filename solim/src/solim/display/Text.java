package solim.display;

import arc.scene.ui.Label;
import solim.core.Disposable;
import solim.signal.Computed;
import solim.signal.Effect;
import solim.signal.Signal;
import solim.ui.Binding;
import solim.ui.ParentStack;

/**
 * Display widget for text content.
 */
public final class Text {
    private final Label label = new Label("");
    private Effect binding;

    public Text() {
    }

    public Text(String text) {
        label.setText(text);
    }

    public static Text of(String text) {
        return new Text(text);
    }

    public static Text of(Signal<String> signal) {
        Text t = new Text();
        Effect e = Effect.of((java.util.function.Consumer<Effect.Cleanup>) cleanup -> {
            t.label.setText(signal.get());
        });
        t.binding = e;
        return t;
    }

    public static Text of(Computed<String> computed) {
        Text t = new Text();
        Effect e = Effect.of((java.util.function.Consumer<Effect.Cleanup>) cleanup -> {
            t.label.setText(computed.get());
        });
        t.binding = e;
        return t;
    }

    public Label label() {
        return label;
    }

    public void dispose() {
        if (binding != null) binding.dispose();
    }
}
