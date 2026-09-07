package solim.overlay;

import arc.Core;
import arc.Events;
import arc.func.Cons;
import arc.func.Func;
import arc.scene.Scene;
import arc.scene.style.Drawable;
import arc.scene.ui.Dialog;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.util.Log;
import arc.util.Nullable;
import mindustry.ui.dialogs.BaseDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import solim.core.Component;
import solim.core.Disposable;
import solim.signal.Signal;
import solim.ui.ParentStack;

/**
 * Declarative dialog component for Solim. Extends Arc Dialog while providing
 * full declarative
 * configuration, reactive signal creation, and clean lifecycle management.
 */
public class SolimDialog extends BaseDialog implements Disposable, arc.util.Disposable {

    private final List<Disposable> disposables = new ArrayList<>();
    private boolean isShown = false;
    private boolean isDisposed = false;

    public SolimDialog() {
        super("");
    }

    public SolimDialog(String title) {
        super(title != null ? title : "");
        setFillParent(true);
    }

    public SolimDialog fillParent(boolean fillParent) {
        setFillParent(fillParent);
        return this;
    }

    public boolean isFillParent() {
        return fillParent;
    }

    public static SolimDialog of(String title, Runnable content) {
        SolimDialog d = new SolimDialog(title);
        d.content(content);
        return d;
    }

    public SolimDialog content(@Nullable Component component) {
        if (component != null) {
            cont.add(component.element()).grow().expand();
            registerDisposable(component::dispose);
        }
        return this;
    }

    public SolimDialog content(@Nullable Runnable contentBuilder) {
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

    public SolimDialog children(@Nullable Runnable contentBuilder) {
        return content(contentBuilder);
    }

    public SolimDialog actionButton(String text, Runnable action) {
        if (buttons != null) {
            buttons.button(text != null ? text : "", action).wrapLabel(false);
        }
        return this;
    }

    public SolimDialog actionButton(String text, Drawable icon, Runnable action) {
        if (buttons != null) {
            if (icon != null) {
                buttons.button(text != null ? text : "", icon, action).wrapLabel(false);
            } else {
                buttons.button(text != null ? text : "", action).wrapLabel(false);
            }
        }
        return this;
    }

    public SolimDialog actionButton(String text, Drawable icon, float width, float height, Runnable action) {
        if (buttons != null) {
            Cell<?> cell;
            if (icon != null) {
                cell = buttons.button(text != null ? text : "", icon, action).wrapLabel(false);
            } else {
                cell = buttons.button(text != null ? text : "", action).wrapLabel(false);
            }
            if (cell != null) {
                cell.size(width, height);
            }
        }
        return this;
    }

    /** Registers a disposable resource with this dialog's lifecycle. */
    public <T extends Disposable> T registerDisposable(T disposable) {
        if (disposable != null) {
            disposables.add(disposable);
        }
        return disposable;
    }

    /**
     * Registers an Arc event listener that automatically unregisters when this
     * dialog is disposed.
     */
    public <T> Disposable listen(Class<T> eventType, Cons<T> listener) {
        Events.on(eventType, listener);
        Disposable d = () -> Events.remove(eventType, listener);
        disposables.add(d);
        return d;
    }

    /**
     * Creates a reactive signal initialized from the supplier that recalculates
     * whenever the callback
     * registrar invokes the given callback (e.g. {@code this.resized(callback)}).
     */
    public <T> Signal<T> createSignal(Consumer<Runnable> callbackRegistrar, Supplier<T> supplier) {
        Signal<T> signal = Signal.of(supplier.get());
        if (callbackRegistrar != null) {
            callbackRegistrar.accept(() -> signal.set(supplier.get()));
        }
        return signal;
    }

    /**
     * Creates a reactive signal initialized from the supplier that recalculates
     * whenever the
     * specified Arc event fires. The event listener is automatically cleaned up
     * when this dialog is
     * disposed.
     */
    public <E, T> Signal<T> createSignal(Class<E> eventType, Supplier<T> supplier) {
        Signal<T> signal = Signal.of(supplier.get());
        listen(eventType, e -> signal.set(supplier.get()));
        return signal;
    }

    /**
     * Creates a reactive signal that updates with mapped event data whenever the
     * specified Arc event
     * fires. The event listener is automatically cleaned up when this dialog is
     * disposed.
     */
    public <E, T> Signal<T> createSignal(Class<E> eventType, Func<E, T> mapper, T initial) {
        Signal<T> signal = Signal.of(initial);
        listen(eventType, e -> signal.set(mapper.get(e)));
        return signal;
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
                Log.err("Error disposing resource in SolimDialog", t);
            }
        }
        disposables.clear();
    }
}
