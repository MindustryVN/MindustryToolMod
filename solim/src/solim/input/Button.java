package solim.input;

import arc.scene.ui.TextButton;
import arc.scene.ui.Tooltip;
import solim.core.Disposable;
import solim.signal.Computed;
import solim.signal.Effect;
import solim.signal.Signal;
import solim.style.Style;
import solim.style.StyleBinding;
import java.util.ArrayList;
import java.util.List;

/**
 * Button widget with static and reactive text/style/enabled.
 */
public final class Button implements Disposable {
    private final TextButton textButton = new TextButton("");
    private final List<Effect> bindings = new ArrayList<>();
    private Runnable onClick;

    public Button() {}

    public static Button of(String text, Runnable onClick) {
        Button b = new Button();
        b.textButton.setText(text);
        b.onClick = onClick;
        if (onClick != null) b.textButton.changed(b.onClick);
        return b;
    }

    public static Button of(Signal<String> text, Runnable onClick) {
        Button b = new Button();
        b.onClick = onClick;
        if (onClick != null) b.textButton.changed(b.onClick);
        Effect e = Effect.of((java.util.function.Consumer<Effect.Cleanup>) cleanup -> {
            b.textButton.setText(text.get());
        });
        b.bindings.add(e);
        return b;
    }

    public static Button of(Computed<String> text, Runnable onClick) {
        Button b = new Button();
        b.onClick = onClick;
        if (onClick != null) b.textButton.changed(b.onClick);
        Effect e = Effect.of((java.util.function.Consumer<Effect.Cleanup>) cleanup -> {
            b.textButton.setText(text.get());
        });
        b.bindings.add(e);
        return b;
    }

    public Button tooltip(String tip) {
        if (tip != null && !tip.isEmpty()) {
            try {
                textButton.addListener(new Tooltip(t -> t.add(tip)));
            } catch (Throwable ignored) {
            }
        }
        return this;
    }

    public Button enabled(Signal<Boolean> signal) {
        Effect e = Effect.of((java.util.function.Consumer<Effect.Cleanup>) cleanup -> {
            textButton.setDisabled(!signal.get());
        });
        bindings.add(e);
        return this;
    }

    public Button visible(Signal<Boolean> signal) {
        Effect e = Effect.of((java.util.function.Consumer<Effect.Cleanup>) cleanup -> {
            textButton.visible = signal.get();
        });
        bindings.add(e);
        return this;
    }

    public Button style(Style style) {
        StyleBinding.apply(textButton, style, (tb, s) -> {});
        return this;
    }

    public Button style(Signal<Style> s) {
        Effect e = StyleBinding.bind(s, textButton, (tb, st) -> {});
        bindings.add(e);
        return this;
    }

    public Button style(Computed<Style> s) {
        Effect e = StyleBinding.bind(s, textButton, (tb, st) -> {});
        bindings.add(e);
        return this;
    }

    public TextButton textButton() {
        return textButton;
    }

    @Override
    public void dispose() {
        for (Effect e : bindings) e.dispose();
        bindings.clear();
    }
}
