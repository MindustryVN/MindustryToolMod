package solim.layout;

import arc.scene.ui.layout.Table;
import arc.util.Nullable;

/**
 * A {@link Table} subclass that overrides {@code getPrefWidth/Height}, {@code getMinWidth/Height},
 * and {@code getMaxWidth/Height} to respect Solim {@link SizeConstraints}.
 *
 * <p>This replaces the raw {@code Table} used inside {@link Row}, {@link Column}, {@link Grid},
 * and the outer table of {@link Scroll}. It implements {@link ConstrainedElement} so parent
 * ATTACHERs can read and apply constraints to the parent cell.
 *
 * <p>The override strategy:
 * <ul>
 *   <li>If a constraint is set, return it (the constraint value wins).</li>
 *   <li>Otherwise, delegate to {@code super} (natural Arc layout behaviour).</li>
 * </ul>
 */
public class SizedTable extends Table implements ConstrainedElement {

	private final SizeConstraints constraints = new SizeConstraints();

	public SizedTable() {
	}

	@Override
	public SizeConstraints getSizeConstraints() {
		return constraints;
	}

	@Override
	public float getPrefWidth() {
		if (constraints == null) return super.getPrefWidth();
		@Nullable Float v = constraints.prefWidth != null ? constraints.prefWidth.get() : null;
		return v != null ? Math.max(0f, v) : super.getPrefWidth();
	}

	@Override
	public float getPrefHeight() {
		if (constraints == null) return super.getPrefHeight();
		@Nullable Float v = constraints.prefHeight != null ? constraints.prefHeight.get() : null;
		return v != null ? Math.max(0f, v) : super.getPrefHeight();
	}

	@Override
	public float getMinWidth() {
		if (constraints == null) return super.getMinWidth();
		@Nullable Float v = constraints.minWidth != null ? constraints.minWidth.get() : null;
		return v != null ? Math.max(0f, v) : super.getMinWidth();
	}

	@Override
	public float getMinHeight() {
		if (constraints == null) return super.getMinHeight();
		@Nullable Float v = constraints.minHeight != null ? constraints.minHeight.get() : null;
		return v != null ? Math.max(0f, v) : super.getMinHeight();
	}

	@Override
	public float getMaxWidth() {
		if (constraints == null) return super.getMaxWidth();
		@Nullable Float v = constraints.maxWidth != null ? constraints.maxWidth.get() : null;
		return v != null ? Math.max(0f, v) : super.getMaxWidth();
	}

	@Override
	public float getMaxHeight() {
		if (constraints == null) return super.getMaxHeight();
		@Nullable Float v = constraints.maxHeight != null ? constraints.maxHeight.get() : null;
		return v != null ? Math.max(0f, v) : super.getMaxHeight();
	}
}
