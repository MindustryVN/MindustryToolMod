package solim.ui;

import arc.Core;
import arc.scene.Element;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.util.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import solim.core.BaseComponent;
import solim.core.Component;
import solim.core.Disposable;
import solim.layout.ConstrainedElement;
import solim.layout.LayoutModifiers;
import solim.layout.SizeConstraints;
import solim.signal.Effect;
import solim.signal.ReactiveContext;
import solim.signal.Readable;

/**
 * Structural reactive container that dynamically recreates its child component
 * whenever the backing signal value changes.
 */
public final class Dynamic<T> extends BaseComponent implements LayoutModifiers<Dynamic<T>> {
	private final Readable<T> source;
	private final Function<T, Component> factory;
	private final DynamicTable container = new DynamicTable();
	private @Nullable Component currentComponent;
	private final List<Disposable> currentBindings = new ArrayList<>();

	public static class DynamicTable extends Table implements ConstrainedElement {
		private final SizeConstraints constraints = new SizeConstraints();

		@Override
		public SizeConstraints getSizeConstraints() {
			return constraints;
		}
	}

	public Dynamic(Readable<T> source, Function<T, Component> factory) {
		this.source = source;
		this.factory = factory;
	}

	public static <T> Dynamic<T> of(Readable<T> source, Function<T, Component> factory) {
		return new Dynamic<>(source, factory);
	}

	public Table container() {
		return container;
	}

	public Table table() {
		return container;
	}

	@Override
	public SizeConstraints sizeConstraints() {
		return container.getSizeConstraints();
	}

	@Override
	protected Element build() {
		container.top().left();
		Effect.of(() -> {
			T value = source.get();
			if (container.getScene() != null && Core.app != null) {
				Core.app.post(() -> {
					rebuild(value);
				});
			} else {
				rebuild(value);
			}
		});
		return container;
	}

	private void rebuild(T value) {
		if (currentComponent != null) {
			currentComponent.dispose();
			currentComponent = null;
		}
		for (Disposable d : currentBindings) {
			d.dispose();
		}
		currentBindings.clear();
		container.clearChildren();
		if (value != null && factory != null) {
			currentComponent = ReactiveContext.untracked(() ->
				ParentStack.isolate(() -> {
					Component c = factory.apply(value);
					if (c != null) {
						c.element();
					}
					return c;
				})
			);
			if (currentComponent != null) {
				Element el = currentComponent.element();
				Cell<?> cell = container.add(el);
				if (el instanceof ConstrainedElement) {
					List<Disposable> effects = ((ConstrainedElement) el).getSizeConstraints().applyToCell(cell);
					currentBindings.addAll(effects);
				}
			}
			container.invalidateHierarchy();
		}
	}

	@Override
	protected void onDispose() {
		if (currentComponent != null) {
			currentComponent.dispose();
			currentComponent = null;
		}
		for (Disposable d : currentBindings) {
			d.dispose();
		}
		currentBindings.clear();
		container.clearChildren();
	}
}
