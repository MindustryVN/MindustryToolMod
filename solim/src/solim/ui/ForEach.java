package solim.ui;

import arc.scene.Element;
import arc.scene.ui.layout.Cell;
import java.util.*;
import java.util.function.Function;
import solim.core.BaseComponent;
import solim.core.Component;
import solim.layout.ConstrainedElement;
import solim.layout.SizeConstraints;
import solim.layout.SizedTable;
import solim.signal.Effect;
import solim.signal.Readable;

/**
 * Keyed reactive list component that efficiently manages child components without rebuilding
 * unchanged items.
 */
public final class ForEach<T, K> extends BaseComponent implements ConstrainedElement {
	private final SizedTable container = new SizedTable();
	private final Readable<? extends Iterable<T>> collection;
	private final Function<T, K> keyExtractor;
	private final Function<T, Component> itemFactory;
	private final Map<K, Component> activeComponents = new LinkedHashMap<>();

	public ForEach(
			Readable<? extends Iterable<T>> collection,
			Function<T, K> keyExtractor,
			Function<T, Component> itemFactory) {
		this.collection = collection;
		this.keyExtractor = keyExtractor;
		this.itemFactory = itemFactory;
		this.container.getSizeConstraints().growX = true;
	}

	public static <T, K> ForEach<T, K> of(
			Readable<? extends Iterable<T>> collection,
			Function<T, K> keyExtractor,
			Function<T, Component> itemFactory) {
		return new ForEach<>(collection, keyExtractor, itemFactory);
	}

	public SizedTable container() {
		return container;
	}

	@Override
	public SizeConstraints getSizeConstraints() {
		return container.getSizeConstraints();
	}

	@Override
	protected Element build() {
		container.top().left();
		own(Effect.of(this::reconcile));
		return container;
	}

	private void reconcile() {
		Iterable<T> items = collection.get();
		if (items == null) {
			items = Collections.emptyList();
		}

		Map<K, Component> nextComponents = new LinkedHashMap<>();
		Set<K> currentKeys = new HashSet<>();

		for (T item : items) {
			K key = keyExtractor.apply(item);
			currentKeys.add(key);

			Component comp = activeComponents.get(key);
			if (comp == null) {
				comp = ParentStack.isolate(() -> {
					Component c = itemFactory.apply(item);
					if (c != null) {
						c.element();
					}
					return c;
				});
			}
			nextComponents.put(key, comp);
		}

		for (Map.Entry<K, Component> entry : activeComponents.entrySet()) {
			if (!currentKeys.contains(entry.getKey())) {
				entry.getValue().dispose();
			}
		}

		activeComponents.clear();
		activeComponents.putAll(nextComponents);

		container.clearChildren();
		for (Component comp : activeComponents.values()) {
			Element el = comp.element();
			Cell<?> cell = container.add(el);
			cell.row();
			if (el instanceof ConstrainedElement) {
				((ConstrainedElement) el).getSizeConstraints().applyToCell(cell);
			}
		}
	}

	@Override
	protected void onDispose() {
		for (Component comp : activeComponents.values()) {
			comp.dispose();
		}
		activeComponents.clear();
		container.clearChildren();
	}
}
