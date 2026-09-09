package solim.input;

import arc.input.KeyCode;
import arc.scene.Element;
import arc.scene.ui.TextField;
import java.util.function.Consumer;
import java.util.function.Predicate;
import solim.core.Component;
import solim.core.ComponentContext;
import solim.core.Disposable;
import solim.layout.ConstrainedElement;
import solim.layout.SizeConstraints;
import solim.modifier.ElementModifiers;
import solim.signal.Effect;
import solim.signal.Readable;
import solim.signal.Signal;
import solim.ui.Binding;

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
	private Effect disabledEffect;
	private boolean updating = false;
	private Predicate<String> validator;
	private final Signal<Boolean> valid = Signal.of(true);

	public SolimTextField(Signal<String> signal) {
		this(signal, (TextField.TextFieldStyle) null);
	}

	public SolimTextField(Signal<String> signal, TextField.TextFieldStyle style) {
		this.field = style != null ? new SizedTextField("", style) : new SizedTextField("");
		this.field.name = "solim-textfield-textField";
		this.signal = signal;
		field.setText(signal.get());
		// listener: type -> signal
		field.changed(() -> {
			if (updating) return;
			String text = field.getText();
			if (!text.equals(signal.get())) {
				signal.set(text);
			}
			if (validator != null) {
				valid.set(validator.test(text));
			}
		});
		// effect: signal -> field
		this.effect = Effect.of((Consumer<Effect.Cleanup>) cleanup -> {
			String val = signal.get();
			if (!field.getText().equals(val)) {
				updating = true;
				try {
					field.setText(val);
				} finally {
					updating = false;
				}
			}
			if (validator != null) {
				valid.set(validator.test(val));
			}
		});

		ComponentContext.register(this);
	}

	public static SolimTextField of(Signal<String> signal) {
		return new SolimTextField(signal);
	}

	public SolimTextField validator(Predicate<String> validator) {
		this.validator = validator;
		this.valid.set(validator == null || validator.test(field.getText()));
		return this;
	}

	public Readable<Boolean> valid() {
		return valid;
	}

	public boolean isValid() {
		return Boolean.TRUE.equals(valid.get());
	}

	public SolimTextField onEnter(Consumer<String> onSubmit) {
		field.keyDown(key -> {
			if (key == KeyCode.enter && !field.isDisabled()) {
				onSubmit.accept(field.getText());
			}
		});
		return this;
	}

	public SolimTextField onEnter(Runnable onSubmit) {
		return onEnter(text -> onSubmit.run());
	}

	public SolimTextField disabled(boolean disabled) {
		field.setDisabled(disabled);
		return this;
	}

	public SolimTextField disabled(Readable<Boolean> disabled) {
		if (disabledEffect != null) {
			disabledEffect.dispose();
		}
		disabledEffect = Binding.bind(disabled, d -> field.setDisabled(Boolean.TRUE.equals(d)));
		return this;
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
		if (disabledEffect != null) {
			disabledEffect.dispose();
			disabledEffect = null;
		}
	}
}
