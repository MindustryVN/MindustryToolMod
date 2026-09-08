package solim.input;

import arc.Core;
import arc.scene.ui.CheckBox;
import arc.scene.ui.CheckBox.CheckBoxStyle;
import arc.util.Nullable;
import java.util.function.Consumer;
import solim.core.Component;
import solim.core.ComponentContext;
import solim.core.Disposable;
import solim.layout.ConstrainedElement;
import solim.layout.SizeConstraints;
import solim.modifier.ElementModifiers;
import solim.signal.Effect;
import solim.signal.Signal;

/** Checkbox widget bound to Signal&lt;Boolean&gt;. */
public final class Checkbox implements Component, Disposable {

	public static class SizedCheckBox extends CheckBox implements ConstrainedElement {
		private final SizeConstraints constraints = new SizeConstraints();

        {
            top().left();
        }

		public SizedCheckBox(String text) {
			super(text);
		}

		public SizedCheckBox(String text, CheckBoxStyle style) {
			super(text, style);
		}

		@Override
		public SizeConstraints getSizeConstraints() {
			return constraints;
		}

		public SizedCheckBox growX() {
			constraints.growX = true;
			constraints.applyGrowToParentCell(this);
			return this;
		}

		public SizedCheckBox growY() {
			constraints.growY = true;
			constraints.applyGrowToParentCell(this);
			return this;
		}

		public SizedCheckBox grow() {
			return growX().growY();
		}
	}

	private final SizedCheckBox checkBox;
	private final @Nullable Signal<Boolean> signal;
	private Effect effect;
	private boolean updating = false;

	public Checkbox(String label, Signal<Boolean> signal) {
		this(label, signal, Core.scene == null ? new CheckBoxStyle() : null);
	}

	public Checkbox(String label, Signal<Boolean> signal, @Nullable CheckBoxStyle style) {
		this.signal = signal;
		this.checkBox = style != null
				? new SizedCheckBox(label != null ? label : "", style)
				: new SizedCheckBox(label != null ? label : "");
		checkBox.name = "solim-checkbox-checkBox";
		checkBox.setChecked(signal.get());
		checkBox.changed(() -> {
			if (updating) return;
			signal.set(checkBox.isChecked());
		});
		this.effect = Effect.of((Consumer<Effect.Cleanup>) cleanup -> {
			if (checkBox.isChecked() != signal.get()) {
				updating = true;
				try {
					checkBox.setChecked(signal.get());
				} finally {
					updating = false;
				}
			}
		});
		ComponentContext.register(this);
	}

	public Checkbox(String label, boolean initial, Consumer<Boolean> onChanged) {
		this(label, initial, Core.scene == null ? new CheckBoxStyle() : null, onChanged);
	}

	public Checkbox(String label, boolean initial, @Nullable CheckBoxStyle style, Consumer<Boolean> onChanged) {
		this.signal = null;
		this.checkBox = style != null
				? new SizedCheckBox(label != null ? label : "", style)
				: new SizedCheckBox(label != null ? label : "");
		checkBox.name = "solim-checkbox-checkBox";
		checkBox.setChecked(initial);
		checkBox.changed(() -> {
			if (onChanged != null) {
				onChanged.accept(checkBox.isChecked());
			}
		});
		ComponentContext.register(this);
	}

	public static Checkbox of(String label, Signal<Boolean> signal) {
		return new Checkbox(label, signal);
	}

	public static Checkbox of(String label, boolean initial, Consumer<Boolean> onChanged) {
		return new Checkbox(label, initial, onChanged);
	}

	public Checkbox growX() {
		checkBox.growX();
		return this;
	}

	public Checkbox growY() {
		checkBox.growY();
		return this;
	}

	public Checkbox grow() {
		checkBox.grow();
		return this;
	}

	public SizedCheckBox checkBox() {
		return checkBox;
	}

	@Override
	public SizedCheckBox element() {
		return checkBox;
	}

	@Override
	public Checkbox name(String name) {
		ElementModifiers.name(checkBox, name);
		return this;
	}

	@Override
	public void dispose() {
		if (effect != null) effect.dispose();
	}
}
