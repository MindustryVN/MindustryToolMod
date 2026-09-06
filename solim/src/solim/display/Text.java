package solim.display;

import arc.graphics.Color;
import arc.scene.Element;
import arc.scene.ui.Label;
import solim.core.Component;
import solim.core.Disposable;
import solim.signal.Computed;
import solim.signal.Effect;
import solim.signal.Readable;
import solim.signal.Signal;
import solim.ui.Binding;

/**
 * Display widget for text content.
 */
public final class Text implements Component, Disposable {
    private final Label label = new Label("");
    private Disposable binding;

    public Text() {
    }

    public Text(String text) {
        label.setText(text != null ? text : "");
    }

    public static Text of(String text) {
        return new Text(text);
    }

    public static Text of(Signal<String> signal) {
        Text t = new Text();
        t.binding = Binding.bindText(t.label, signal);
        return t;
    }

    public static Text of(Computed<String> computed) {
        Text t = new Text();
        t.binding = Binding.bindText(t.label, computed);
        return t;
    }

    public static Text of(Readable<String> readable) {
        Text t = new Text();
        t.binding = Binding.bindText(t.label, readable);
        return t;
    }

    public Text color(Color color) {
        label.setColor(color);
        return this;
    }

    public Text color(Readable<Color> color) {
        Binding.bindColor(label, color);
        return this;
    }

    public Text padding(float pad) {
        // Label margin/padding
        return this;
    }

    public Text fontScale(float scale) {
        label.setFontScale(scale);
        return this;
    }

    public Label label() {
        return label;
    }

    @Override
    public Element element() {
        return label;
    }

    @Override
    public void dispose() {
        if (binding != null) {
            binding.dispose();
            binding = null;
        }
    }
}
