package solim.input;

import arc.Core;
import arc.scene.Element;
import arc.scene.ui.TextField;
import java.util.function.Consumer;
import solim.core.Component;
import solim.core.ComponentContext;
import solim.core.Disposable;
import solim.layout.ConstrainedElement;
import solim.layout.SizeConstraints;
import solim.modifier.ElementModifiers;
import solim.signal.Effect;
import solim.signal.Signal;

/**
 * TextField widget with two-way binding to a Signal&lt;String&gt;. Equality guard prevents feedback
 * loop. Automatically registers with the active ComponentContext if created during a component
 * build.
 */
public final class SolimTextField implements Component, Disposable {

	public static class SizedTextField extends TextField implements ConstrainedElement {
		private final SizeConstraints constraints = new SizeConstraints();

		public SizedTextField(String text) {
			super(text);
		}

		public SizedTextField(String text, TextFieldStyle style) {
			super(text, style);
		}

		@Override
		public SizeConstraints getSizeConstraints() {
			return constraints;
		}

		public SizedTextField growX() {
			constraints.growX = true;
			constraints.applyGrowToParentCell(this);
			return this;
		}

		public SizedTextField growY() {
			constraints.growY = true;
			constraints.applyGrowToParentCell(this);
			return this;
		}

		public SizedTextField grow() {
			return growX().growY();
		}
	}

	private final SizedTextField field;
	private final Signal<String> signal;
	private Effect effect;
	private boolean updating = false;

	public SolimTextField(Signal<String> signal) {
		this(signal, Core.scene == null ? new TextField.TextFieldStyle() : null);
	}

	public SolimTextField(Signal<String> signal, TextField.TextFieldStyle style) {
		this.field = style != null ? new SizedTextField("", style) : new SizedTextField("");
		this.field.name = "solim-textfield-textField";
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
		this.effect = Effect.of((Consumer<Effect.Cleanup>) cleanup -> {
			if (!field.getText().equals(signal.get())) {
				updating = true;
				try {
					field.setText(signal.get());
				} finally {
					updating = false;
				}
			}
		});

		ComponentContext.register(this);
	}

	public static SolimTextField of(Signal<String> signal) {
		return new SolimTextField(signal);
	}

	public SolimTextField placeholder(String placeholder) {
		field.setMessageText(placeholder);
		return this;
	}

	public SolimTextField width(float width) {
		ElementModifiers.width(field, width);
		return this;
	}

	public SolimTextField height(float height) {
		ElementModifiers.height(field, height);
		return this;
	}

	public SolimTextField size(float width, float height) {
		ElementModifiers.size(field, width, height);
		return this;
	}

	public SolimTextField size(float size) {
		ElementModifiers.size(field, size);
		return this;
	}

	public SolimTextField growX() {
		field.growX();
		return this;
	}

	public SolimTextField growY() {
		field.growY();
		return this;
	}

	public SolimTextField grow() {
		return growX().growY();
	}

	public SolimTextField x(float x) {
		ElementModifiers.x(field, x);
		return this;
	}

	public SolimTextField y(float y) {
		ElementModifiers.y(field, y);
		return this;
	}

	public SolimTextField position(float x, float y) {
		ElementModifiers.position(field, x, y);
		return this;
	}

	public SolimTextField visible(boolean visible) {
		ElementModifiers.visible(field, visible);
		return this;
	}

	public SizedTextField field() {
		return field;
	}

	@Override
	public Element element() {
		return field;
	}

	@Override
	public SolimTextField name(String name) {
		ElementModifiers.name(field, name);
		return this;
	}

	@Override
	public void dispose() {
		if (effect != null) {
			effect.dispose();
			effect = null;
		}
	}
}
