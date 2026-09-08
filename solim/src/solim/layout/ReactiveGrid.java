package solim.layout;

import arc.scene.Element;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.util.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import solim.core.BaseComponent;
import solim.core.Component;
import solim.core.ComponentContext;
import solim.core.Disposable;
import solim.modifier.ElementModifiers;
import solim.signal.Effect;
import solim.signal.Readable;
import solim.ui.ParentStack;

/**
 * Keyed reactive grid that reflows existing component cells when column count changes and
 * structurally reconciles items when the item collection changes.
 */
public final class ReactiveGrid<T, K> extends BaseComponent implements LayoutModifiers<ReactiveGrid<T, K>> {
	private final SizedTable table = new SizedTable();
	private final Readable<Integer> columnCount;
	private final Readable<? extends Iterable<T>> items;
	private final Function<T, K> keyExtractor;
	private final Function<T, Component> itemFactory;
	private final Map<K, Component> activeComponents = new LinkedHashMap<>();
	private final List<Disposable> itemBindings = new ArrayList<>();

	private Runnable emptyRunnable;
	private Supplier<Component> emptyViewSupplier;
	private Component currentEmptyComponent;
	private float gap = 0f;

	public ReactiveGrid(
			Readable<Integer> columnCount,
			Readable<? extends Iterable<T>> items,
			Function<T, K> keyExtractor,
			Function<T, Component> itemFactory) {
		this.table.name = "solim-reactive-grid-table";
		this.columnCount = columnCount;
		this.items = items;
		this.keyExtractor = keyExtractor;
		this.itemFactory = itemFactory;
		growX();
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
		ElementModifiers.gap(table, gap);
		return this;
	}

	public ReactiveGrid<T, K> gap(@Nullable Readable<Float> gapSignal) {
		if (gapSignal != null) {
			Effect e = Effect.of(() -> {
				Float g = gapSignal.get();
				if (g != null) {
					gap(g);
				}
			});
			registerDisposable(e);
			ComponentContext.register(e);
		}
		return this;
	}


	public SizedTable table() {
		return table;
	}

	@Override
	public SizeConstraints sizeConstraints() {
		return table.getSizeConstraints();
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
		for (Disposable d : itemBindings) {
			d.dispose();
		}
		itemBindings.clear();

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
				table.add(emptyTable).center().colspan(cols).growX();
			} else if (emptyViewSupplier != null) {
				if (currentEmptyComponent == null) {
					currentEmptyComponent = ParentStack.isolate(() -> {
						Component c = emptyViewSupplier.get();
						if (c != null) {
							c.element();
						}
						return c;
					});
				}
				table.add(currentEmptyComponent.element()).center().colspan(cols).growX();
			}
			return;
		}

		if (currentEmptyComponent != null) {
			currentEmptyComponent.dispose();
			currentEmptyComponent = null;
		}

		int col = 0;
		for (Component comp : activeComponents.values()) {
			Element el = comp.element();
			Cell<?> cell = table.add(el).pad(gap / 2f).top().left();
			if (el instanceof ConstrainedElement) {
				List<Disposable> effects = ((ConstrainedElement) el).getSizeConstraints().applyToCell(cell);
				itemBindings.addAll(effects);
			}
			if ((el instanceof ConstrainedElement) && ((ConstrainedElement) el).getSizeConstraints().growX) {
				cell.uniformX();
			}
			if (++col % cols == 0) {
				table.row();
			}
		}
		while (col % cols != 0) {
			table.add().uniformX().growX().pad(gap / 2f);
			col++;
		}
		table.row();
		table.invalidateHierarchy();
	}

	@Override
	protected void onDispose() {
		for (Disposable d : itemBindings) {
			d.dispose();
		}
		itemBindings.clear();

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
