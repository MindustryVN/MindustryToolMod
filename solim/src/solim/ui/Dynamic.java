package solim.ui;

import arc.scene.Element;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import java.util.function.Function;
import solim.core.BaseComponent;
import solim.core.Component;
import solim.layout.ConstrainedElement;
import solim.signal.Effect;
import solim.signal.Readable;

/** Structural reactive component for switching dynamic subtrees based on a reactive value. */
public final class Dynamic<T> extends BaseComponent {
	private final Table container = new Table();
	private final Readable<T> source;
	private final Function<T, Component> factory;
	private Component currentComponent;

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

	@Override
	protected Element build() {
		container.top().left();
		own(Effect.of(() -> {
			T value = source.get();
			if (currentComponent != null) {
				currentComponent.dispose();
				currentComponent = null;
			}
			container.clearChildren();
			if (value != null && factory != null) {
				currentComponent = ParentStack.isolate(() -> {
					Component c = factory.apply(value);
					if (c != null) {
						c.element();
					}
					return c;
				});
				if (currentComponent != null) {
					Element el = currentComponent.element();
					Cell<?> cell = container.add(el);
					if (el instanceof ConstrainedElement) {
						((ConstrainedElement) el).getSizeConstraints().applyToCell(cell);
					}
				}
				container.invalidateHierarchy();
			}
		}));
		return container;
	}

	@Override
	protected void onDispose() {
		if (currentComponent != null) {
			currentComponent.dispose();
			currentComponent = null;
		}
		container.clearChildren();
	}
}
