package solim.display;

import arc.Core;
import arc.graphics.Color;
import arc.scene.Element;
import arc.scene.ui.Label;
import arc.util.Align;
import java.util.ArrayList;
import java.util.List;
import solim.core.Component;
import solim.core.ComponentContext;
import solim.core.Disposable;
import solim.signal.Computed;
import solim.signal.Effect;
import solim.signal.Readable;
import solim.signal.Signal;

/** Display widget for text content. */
public final class Text implements Component, Disposable {
	private final Label label;
	private final List<Disposable> bindings = new ArrayList<>();

	public Text() {
		this("");
	}

	public Text(String text) {
		this(text, Core.scene != null ? null : new Label.LabelStyle());
	}

	public Text(String text, Label.LabelStyle style) {
		this.label = (style != null || Core.scene == null)
				? new Label(text != null ? text : "", style != null ? style : new Label.LabelStyle())
				: new Label(text != null ? text : "");
	}

	public static Text of(String text) {
		return new Text(text);
	}

	public static Text of(Signal<String> signal) {
		return of((Readable<String>) signal);
	}

	public static Text of(Computed<String> computed) {
		return of((Readable<String>) computed);
	}

	public static Text of(Readable<String> readable) {
		Text t = new Text();
		t.text(readable);
		return t;
	}

	public Text text(String text) {
		label.setText(text != null ? text : "");
		return this;
	}

	public Text text(Readable<String> text) {
		if (text != null) {
			Effect e = Effect.of(() -> label.setText(text.get() != null ? text.get() : ""));
			bindings.add(e);
			ComponentContext.register(e);
		}
		return this;
	}

	public Text color(Color color) {
		label.setColor(color);
		return this;
	}

	public Text color(Readable<Color> color) {
		if (color != null) {
			Effect e = Effect.of(() -> {
				Color c = color.get();
				if (c != null) {
					label.setColor(c);
				}
			});
			bindings.add(e);
			ComponentContext.register(e);
		}
		return this;
	}

	public Text style(Label.LabelStyle style) {
		if (style != null) {
			label.setStyle(style);
		}
		return this;
	}

	public Text wrap(boolean wrap) {
		label.setWrap(wrap);
		return this;
	}

	public Text wrap() {
		return wrap(true);
	}

	public Text ellipsis(boolean ellipsis) {
		label.setEllipsis(ellipsis);
		return this;
	}

	public Text ellipsis() {
		return ellipsis(true);
	}

	public Text align(int align) {
		label.setAlignment(align);
		return this;
	}

	public Text left() {
		return align(Align.left);
	}

	public Text center() {
		return align(Align.center);
	}

	public Text right() {
		return align(Align.right);
	}

	public Text padding(float pad) {
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
		for (Disposable d : bindings) {
			d.dispose();
		}
		bindings.clear();
	}
}
