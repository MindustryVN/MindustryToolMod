package solim.ui;

import arc.scene.Element;
import arc.scene.ui.layout.Cell;
import java.util.function.Function;
import solim.core.BaseComponent;
import solim.core.Component;
import solim.layout.ConstrainedElement;
import solim.layout.LayoutModifiers;
import solim.layout.SizeConstraints;
import solim.layout.SizedTable;
import solim.signal.Effect;
import solim.signal.ReactiveContext;
import solim.signal.Readable;

/** Structural reactive component for switching dynamic subtrees based on a reactive value. */
public final class Dynamic<T> extends BaseComponent implements ConstrainedElement, LayoutModifiers<Dynamic<T>> {
	private final SizedTable container = new SizedTable();
	private final Readable<T> source;
	private final Function<T, Component> factory;
	private Component currentComponent;
	private final java.util.List<solim.core.Disposable> currentBindings = new java.util.ArrayList<>();

	public Dynamic(Readable<T> source, Function<T, Component> factory) {
		this.source = source;
		this.factory = factory;
		this.container.getSizeConstraints().growX = true;
	}

	public static <T> Dynamic<T> of(Readable<T> source, Function<T, Component> factory) {
		return new Dynamic<>(source, factory);
	}

	public SizedTable container() {
		return container;
	}

	@Override
	public SizeConstraints getSizeConstraints() {
		return container.getSizeConstraints();
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
			if (currentComponent != null) {
				currentComponent.dispose();
				currentComponent = null;
			}
			for (solim.core.Disposable d : currentBindings) {
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
						java.util.List<solim.core.Disposable> effects = ((ConstrainedElement) el).getSizeConstraints().applyToCell(cell);
						currentBindings.addAll(effects);
					}
				}
				container.invalidateHierarchy();
			}
		});
		return container;
	}

	@Override
	protected void onDispose() {
		if (currentComponent != null) {
			currentComponent.dispose();
			currentComponent = null;
		}
		for (solim.core.Disposable d : currentBindings) {
			d.dispose();
		}
		currentBindings.clear();
		container.clearChildren();
	}
}
