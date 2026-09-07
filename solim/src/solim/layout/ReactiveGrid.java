package solim.layout;

import arc.scene.Element;
import arc.scene.ui.layout.Table;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import solim.core.BaseComponent;
import solim.core.Component;
import solim.core.ComponentContext;
import solim.signal.Effect;
import solim.signal.Readable;
import solim.ui.ParentStack;

/**
 * Keyed reactive grid that reflows existing component cells when column count changes and
 * structurally reconciles items when the item collection changes.
 */
public final class ReactiveGrid<T, K> extends BaseComponent {
	private final Table table = new Table();
	private final Readable<Integer> columnCount;
	private final Readable<? extends Iterable<T>> items;
	private final Function<T, K> keyExtractor;
	private final Function<T, Component> itemFactory;
	private final Map<K, Component> activeComponents = new LinkedHashMap<>();

	private Runnable emptyRunnable;
	private Supplier<Component> emptyViewSupplier;
	private Component currentEmptyComponent;
	private float gap = 10f;

	public ReactiveGrid(
			Readable<Integer> columnCount,
			Readable<? extends Iterable<T>> items,
			Function<T, K> keyExtractor,
			Function<T, Component> itemFactory) {
		this.columnCount = columnCount;
		this.items = items;
		this.keyExtractor = keyExtractor;
		this.itemFactory = itemFactory;
	}

	public static <T, K> ReactiveGrid<T, K> of(
			Readable<Integer> columnCount,
			Readable<? extends Iterable<T>> items,
			Function<T, K> keyExtractor,
			Function<T, Component> itemFactory) {
		return new ReactiveGrid<>(columnCount, items, keyExtractor, itemFactory);
	}

	public ReactiveGrid<T, K> empty(Runnable emptyRunnable) {
		this.emptyRunnable = emptyRunnable;
		return this;
	}

	public ReactiveGrid<T, K> emptyView(Supplier<Component> supplier) {
		this.emptyViewSupplier = supplier;
		return this;
	}

	public ReactiveGrid<T, K> gap(float gap) {
		this.gap = gap;
		return this;
	}

	public Table table() {
		return table;
	}

	@Override
	protected Element build() {
		table.top().left();

		registerDisposable(Effect.of(() -> {
			Iterable<T> itemList = items.get();
			int cols = Math.max(1, columnCount.get() != null ? columnCount.get() : 1);
			updateItemsAndReflow(itemList, cols);
		}));

		return table;
	}

	private void updateItemsAndReflow(Iterable<T> itemList, int cols) {
		if (itemList == null) {
			itemList = Collections.emptyList();
		}

		Map<K, Component> nextComponents = new LinkedHashMap<>();
		Set<K> currentKeys = new HashSet<>();

		ComponentContext.pause();
		try {
			for (T item : itemList) {
				K key = keyExtractor.apply(item);
				currentKeys.add(key);

				Component comp = activeComponents.get(key);
				if (comp == null) {
					comp = itemFactory.apply(item);
				}
				nextComponents.put(key, comp);
			}
		} finally {
			ComponentContext.resume();
		}

		// Dispose components no longer present in collection
		for (Map.Entry<K, Component> entry : activeComponents.entrySet()) {
			if (!currentKeys.contains(entry.getKey())) {
				entry.getValue().dispose();
			}
		}

		activeComponents.clear();
		activeComponents.putAll(nextComponents);

		reflow(cols);
	}

	private void reflow(int cols) {
		table.clear();
		table.top().left();

		if (activeComponents.isEmpty()) {
			if (emptyRunnable != null) {
				Table emptyTable = new Table();
				ParentStack.push(emptyTable);
				try {
					emptyRunnable.run();
				} finally {
					ParentStack.pop();
				}
				table.add(emptyTable).center().pad(40f);
			} else if (emptyViewSupplier != null) {
				if (currentEmptyComponent == null) {
					currentEmptyComponent = emptyViewSupplier.get();
				}
				table.add(currentEmptyComponent.element()).pad(40f).center();
			}
			return;
		}

		if (currentEmptyComponent != null) {
			currentEmptyComponent.dispose();
			currentEmptyComponent = null;
		}

		int col = 0;
		for (Component comp : activeComponents.values()) {
			table.add(comp.element()).pad(gap / 2f).top().left();
			if (++col % cols == 0) {
				table.row();
			}
		}
		if (col % cols != 0) {
			table.row();
		}
	}

	@Override
	protected void onDispose() {
		for (Component comp : activeComponents.values()) {
			comp.dispose();
		}
		activeComponents.clear();

		if (currentEmptyComponent != null) {
			currentEmptyComponent.dispose();
			currentEmptyComponent = null;
		}
		table.clear();
	}
}
