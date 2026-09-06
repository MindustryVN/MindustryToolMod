package solim.overlay;

import arc.Core;
import arc.Events;
import arc.func.Cons;
import arc.func.Func;
import arc.scene.Scene;
import arc.scene.style.Drawable;
import arc.scene.ui.Dialog;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import solim.core.Component;
import solim.core.Disposable;
import solim.signal.Signal;
import solim.ui.ParentStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Declarative dialog component for Solim.
 * Extends Arc Dialog while providing full declarative configuration,
 * reactive signal creation, and clean lifecycle management.
 */
public class SolimDialog extends Dialog implements Disposable, arc.util.Disposable {

    private final List<Disposable> disposables = new ArrayList<>();
    private boolean isShown = false;
    private boolean isDisposed = false;

    public SolimDialog(String title) {
        super(title != null ? title : "");
    }

    public static SolimDialog of(String title, Runnable content) {
        SolimDialog d = new SolimDialog(title);
        d.content(content);
        return d;
    }

    public SolimDialog content(Component component) {
        if (component != null) {
            cont.add(component.element()).grow().expand();
        }
        return this;
    }

    public SolimDialog content(Runnable contentBuilder) {
        if (contentBuilder != null) {
            ParentStack.push(cont);
            try {
                contentBuilder.run();
            } finally {
                ParentStack.pop();
            }
        }
        return this;
    }

    public SolimDialog actionButton(String text, Runnable action) {
        if (buttons != null) {
            buttons.button(text != null ? text : "", action);
        }
        return this;
    }

    public SolimDialog actionButton(String text, Drawable icon, Runnable action) {
        if (buttons != null) {
            if (icon != null) {
                buttons.button(text != null ? text : "", icon, action);
            } else {
                buttons.button(text != null ? text : "", action);
            }
        }
        return this;
    }

    public SolimDialog actionButton(String text, Drawable icon, float width, float height, Runnable action) {
        if (buttons != null) {
            Cell<?> cell;
            if (icon != null) {
                cell = buttons.button(text != null ? text : "", icon, action);
            } else {
                cell = buttons.button(text != null ? text : "", action);
            }
            if (cell != null) {
                cell.size(width, height);
            }
        }
        return this;
    }

    /**
     * Registers a disposable resource with this dialog's lifecycle.
     */
    public <T extends Disposable> T registerDisposable(T disposable) {
        if (disposable != null) {
            disposables.add(disposable);
        }
        return disposable;
    }

    /**
     * Registers an Arc event listener that automatically unregisters when this dialog is disposed.
     */
    public <T> Disposable listen(Class<T> eventType, Cons<T> listener) {
        Events.on(eventType, listener);
        Disposable d = () -> Events.remove(eventType, listener);
        disposables.add(d);
        return d;
    }

    /**
     * Creates a reactive signal initialized from the supplier that recalculates whenever
     * the callback registrar invokes the given callback (e.g. {@code this.resized(callback)}).
     */
    public <T> Signal<T> createSignal(Consumer<Runnable> callbackRegistrar, Supplier<T> supplier) {
        Signal<T> signal = Signal.of(supplier.get());
        if (callbackRegistrar != null) {
            callbackRegistrar.accept(() -> signal.set(supplier.get()));
        }
        return signal;
    }

    /**
     * Creates a reactive signal initialized from the supplier that recalculates whenever
     * the specified Arc event fires. The event listener is automatically cleaned up when
     * this dialog is disposed.
     */
    public <E, T> Signal<T> createSignal(Class<E> eventType, Supplier<T> supplier) {
        Signal<T> signal = Signal.of(supplier.get());
        listen(eventType, e -> signal.set(supplier.get()));
        return signal;
    }

    /**
     * Creates a reactive signal that updates with mapped event data whenever the specified Arc event fires.
     * The event listener is automatically cleaned up when this dialog is disposed.
     */
    public <E, T> Signal<T> createSignal(Class<E> eventType, Func<E, T> mapper, T initial) {
        Signal<T> signal = Signal.of(initial);
        listen(eventType, e -> signal.set(mapper.get(e)));
        return signal;
    }

    /**
     * Returns a reactive width signal tied to this dialog's resized callback, recalculating responsive width on resize.
     */
    public Signal<Float> responsiveWidthSignal() {
        return createSignal(this::resized, this::calcResponsiveWidth);
    }

    public float calcResponsiveWidth() {
        if (Core.graphics == null) {
            return 800f;
        }
        return Core.graphics.getWidth() / Scl.scl() * 0.9f - 40f;
    }

    public Table dialog() {
        return this;
    }

    @Override
    public Dialog show(Scene scene) {
        isShown = true;
        if (scene != null || Core.scene != null) {
            super.show(scene != null ? scene : Core.scene);
        }
        return this;
    }

    @Override
    public Dialog show() {
        return show(Core.scene);
    }

    @Override
    public void hide() {
        isShown = false;
        if (Core.scene != null) {
            super.hide();
        }
    }

    @Override
    public boolean isShown() {
        if (Core.scene != null) {
            return super.isShown();
        }
        return isShown;
    }

    public boolean isDisposed() {
        return isDisposed;
    }

    protected void onDispose() {
    }

    @Override
    public void dispose() {
        if (isDisposed) {
            return;
        }
        isDisposed = true;
        hide();
        onDispose();
        for (Disposable d : disposables) {
            try {
                d.dispose();
            } catch (Throwable t) {
                arc.util.Log.err("Error disposing resource in SolimDialog", t);
            }
        }
        disposables.clear();
    }
}
