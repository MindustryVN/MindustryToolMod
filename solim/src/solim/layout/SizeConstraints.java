package solim.layout;

import arc.scene.ui.layout.Cell;
import arc.util.Nullable;
import java.util.ArrayList;
import java.util.List;
import solim.core.Disposable;
import solim.signal.Effect;
import solim.signal.Readable;

/**
 * Value object that holds all size constraints for a Solim layout component.
 *
 * <p>Mirrors the CSS box model:
 * <ul>
 *   <li>{@code prefWidth/Height} — preferred size (like CSS {@code width/height})</li>
 *   <li>{@code minWidth/Height} — lower bound (like CSS {@code min-width/min-height})</li>
 *   <li>{@code maxWidth/Height} — upper bound (like CSS {@code max-width/max-height})</li>
 *   <li>{@code growX/growY} — independent flex-grow flags (like CSS {@code flex-grow})</li>
 * </ul>
 *
 * <p>{@code growX/growY} are fully independent from {@code prefWidth/prefHeight}: a component
 * can have {@code width(unit(10)).growX()} simultaneously (CSS flex-basis style), where the
 * preferred width is 10 units but the parent cell may still stretch it if it has grow.
 */
public final class SizeConstraints {

	/** Preferred width. Null means "no constraint — use natural size". */
	public @Nullable Readable<Float> prefWidth;

	/** Preferred height. Null means "no constraint — use natural size". */
	public @Nullable Readable<Float> prefHeight;

	/** Minimum width. Applied as cell.minWidth(). */
	public @Nullable Readable<Float> minWidth;

	/** Minimum height. Applied as cell.minHeight(). */
	public @Nullable Readable<Float> minHeight;

	/** Maximum width. Applied as cell.maxWidth(). */
	public @Nullable Readable<Float> maxWidth;

	/** Maximum height. Applied as cell.maxHeight(). */
	public @Nullable Readable<Float> maxHeight;

	/** Whether the parent cell should grow this element on the X axis. */
	public boolean growX = false;

	/** Whether the parent cell should grow this element on the Y axis. */
	public boolean growY = false;

	/** Returns true if any explicit preferred or bounded width constraint is set. */
	public boolean hasExplicitWidth() {
		return prefWidth != null || minWidth != null || maxWidth != null;
	}

	/** Returns true if any explicit preferred or bounded height constraint is set. */
	public boolean hasExplicitHeight() {
		return prefHeight != null || minHeight != null || maxHeight != null;
	}

	/**
	 * Applies all constraints to the given cell. Static values are applied immediately.
	 * Reactive values ({@code Signal}, {@code Computed}) install an {@code Effect} that
	 * re-applies the constraint whenever the source changes.
	 *
	 * @return a list of installed {@link Effect} instances — the caller must own and
	 *         dispose them when the component is disposed.
	 */
	public List<Disposable> applyToCell(Cell<?> cell) {
		List<Disposable> effects = new ArrayList<>();

		effects.addAll(bind(cell, prefWidth,  v -> applyPrefWidth(cell, v)));
		effects.addAll(bind(cell, prefHeight, v -> applyPrefHeight(cell, v)));
		effects.addAll(bind(cell, minWidth,   v -> applyMinWidth(cell, v)));
		effects.addAll(bind(cell, minHeight,  v -> applyMinHeight(cell, v)));
		effects.addAll(bind(cell, maxWidth,   v -> applyMaxWidth(cell, v)));
		effects.addAll(bind(cell, maxHeight,  v -> applyMaxHeight(cell, v)));

		if (growX) cell.growX();
		if (growY) cell.growY();

		return effects;
	}

	// ---------- private helpers ----------

	private static List<Disposable> bind(Cell<?> cell, @Nullable Readable<Float> source, FloatApplier applier) {
		List<Disposable> result = new ArrayList<>();
		if (source == null) return result;
		Effect e = Effect.of(() -> {
			Float v = source.get();
			if (v != null) {
				applier.apply(v);
				// Invalidate the parent table so layout is recalculated with the new constraint.
				if (cell.getTable() != null) {
					cell.getTable().invalidateHierarchy();
				}
			}
		});
		result.add(e);
		return result;
	}

	private static void applyPrefWidth(Cell<?> cell, float v)   { cell.width(Math.max(0f, v)); }
	private static void applyPrefHeight(Cell<?> cell, float v)  { cell.height(Math.max(0f, v)); }
	private static void applyMinWidth(Cell<?> cell, float v)    { cell.minWidth(Math.max(0f, v)); }
	private static void applyMinHeight(Cell<?> cell, float v)   { cell.minHeight(Math.max(0f, v)); }
	private static void applyMaxWidth(Cell<?> cell, float v)    { cell.maxWidth(Math.max(0f, v)); }
	private static void applyMaxHeight(Cell<?> cell, float v)   { cell.maxHeight(Math.max(0f, v)); }

	@FunctionalInterface
	private interface FloatApplier {
		void apply(float value);
	}
}
