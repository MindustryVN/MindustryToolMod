package solim.ui;

import arc.scene.Element;
import arc.scene.ui.layout.Table;
import solim.core.BaseComponent;
import solim.core.Component;
import solim.signal.Effect;
import solim.signal.Readable;

import java.util.function.Function;

/**
 * Structural reactive component for switching dynamic subtrees based on a reactive value.
 */
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
                currentComponent = factory.apply(value);
                if (currentComponent != null) {
                    container.add(currentComponent.element()).grow();
                }
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
