package solim.layout;

import solim.signal.Readable;

/**
 * Shared mixin interface that all Solim layout components implement.
 *
 * <p>Provides the full CSS-style size constraint API from a single place, avoiding
 * duplication across {@link Row}, {@link Column}, {@link Card}, {@link Grid}, and
 * {@link Scroll}.
 *
 * <p>Constraint semantics:
 * <ul>
 *   <li>{@code width/height} — preferred size (CSS {@code width/height})</li>
 *   <li>{@code minWidth/minHeight} — lower bound (CSS {@code min-width/min-height})</li>
 *   <li>{@code maxWidth/maxHeight} — upper bound (CSS {@code max-width/max-height})</li>
 *   <li>{@code growX/growY} — independent flex-grow flags; a component can have both
 *       {@code .width(unit(10)).growX()} simultaneously (CSS flex-basis style)</li>
 * </ul>
 *
 * @param <SELF> the concrete component type, enabling fluent chaining
 */
public interface LayoutModifiers<SELF extends LayoutModifiers<SELF>> {

	/** Returns the {@link SizeConstraints} owned by this component's root element. */
	SizeConstraints sizeConstraints();

	// ---------- preferred size ----------

	/** Sets the preferred width to a static value. */
	default SELF width(float v) {
		sizeConstraints().prefWidth = Readable.of(v);
		return self();
	}

	/** Sets the preferred width to a reactive value that updates automatically. */
	default SELF width(Readable<Float> v) {
		sizeConstraints().prefWidth = v;
		return self();
	}

	/** Sets the preferred height to a static value. */
	default SELF height(float v) {
		sizeConstraints().prefHeight = Readable.of(v);
		return self();
	}

	/** Sets the preferred height to a reactive value that updates automatically. */
	default SELF height(Readable<Float> v) {
		sizeConstraints().prefHeight = v;
		return self();
	}

	/** Sets both preferred width and height to the same static value. */
	default SELF size(float s) {
		return width(s).height(s);
	}

	/** Sets preferred width and height to separate static values. */
	default SELF size(float w, float h) {
		return width(w).height(h);
	}

	// ---------- minimum size ----------

	/** Sets the minimum width to a static value. */
	default SELF minWidth(float v) {
		sizeConstraints().minWidth = Readable.of(v);
		return self();
	}

	/** Sets the minimum width to a reactive value. */
	default SELF minWidth(Readable<Float> v) {
		sizeConstraints().minWidth = v;
		return self();
	}

	/** Sets the minimum height to a static value. */
	default SELF minHeight(float v) {
		sizeConstraints().minHeight = Readable.of(v);
		return self();
	}

	/** Sets the minimum height to a reactive value. */
	default SELF minHeight(Readable<Float> v) {
		sizeConstraints().minHeight = v;
		return self();
	}

	// ---------- maximum size ----------

	/** Sets the maximum width to a static value. */
	default SELF maxWidth(float v) {
		sizeConstraints().maxWidth = Readable.of(v);
		return self();
	}

	/** Sets the maximum width to a reactive value. */
	default SELF maxWidth(Readable<Float> v) {
		sizeConstraints().maxWidth = v;
		return self();
	}

	/** Sets the maximum height to a static value. */
	default SELF maxHeight(float v) {
		sizeConstraints().maxHeight = Readable.of(v);
		return self();
	}

	/** Sets the maximum height to a reactive value. */
	default SELF maxHeight(Readable<Float> v) {
		sizeConstraints().maxHeight = v;
		return self();
	}

	// ---------- grow (independent of width/height) ----------

	/**
	 * Marks this component as wanting to grow on the X axis in its parent cell.
	 * Independent from {@code width()} — both can coexist (CSS flex-basis style).
	 */
	default SELF growX() {
		sizeConstraints().growX = true;
		if (this instanceof solim.core.Component) {
			sizeConstraints().applyGrowToParentCell(((solim.core.Component) this).element());
		}
		return self();
	}

	/**
	 * Marks this component as wanting to grow on the Y axis in its parent cell.
	 * Independent from {@code height()} — both can coexist.
	 */
	default SELF growY() {
		sizeConstraints().growY = true;
		if (this instanceof solim.core.Component) {
			sizeConstraints().applyGrowToParentCell(((solim.core.Component) this).element());
		}
		return self();
	}

	/** Marks this component as wanting to grow on both axes. */
	default SELF grow() {
		return growX().growY();
	}

	// ---------- alignment ----------

	/**
	 * Centers this component within its parent cell.
	 */
	default SELF center() {
		sizeConstraints().alignCenter();
		if (this instanceof solim.core.Component) {
			sizeConstraints().applyAlignToParentCell(((solim.core.Component) this).element());
		}
		return self();
	}

	/**
	 * Aligns this component to the top of its parent cell.
	 */
	default SELF top() {
		sizeConstraints().alignTop();
		if (this instanceof solim.core.Component) {
			sizeConstraints().applyAlignToParentCell(((solim.core.Component) this).element());
		}
		return self();
	}

	/**
	 * Aligns this component to the bottom of its parent cell.
	 */
	default SELF bottom() {
		sizeConstraints().alignBottom();
		if (this instanceof solim.core.Component) {
			sizeConstraints().applyAlignToParentCell(((solim.core.Component) this).element());
		}
		return self();
	}

	/**
	 * Aligns this component to the left of its parent cell.
	 */
	default SELF left() {
		sizeConstraints().alignLeft();
		if (this instanceof solim.core.Component) {
			sizeConstraints().applyAlignToParentCell(((solim.core.Component) this).element());
		}
		return self();
	}

	/**
	 * Aligns this component to the right of its parent cell.
	 */
	default SELF right() {
		sizeConstraints().alignRight();
		if (this instanceof solim.core.Component) {
			sizeConstraints().applyAlignToParentCell(((solim.core.Component) this).element());
		}
		return self();
	}

	// ---------- internal ----------

	@SuppressWarnings("unchecked")
	private SELF self() {
		return (SELF) this;
	}
}
