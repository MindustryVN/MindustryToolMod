package solim.overlay;

import arc.Core;
import arc.scene.Scene;
import arc.scene.style.Drawable;
import arc.scene.ui.Dialog;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import mindustry.ui.dialogs.BaseDialog;
import solim.core.Component;
import solim.core.Disposable;
import solim.ui.ParentStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Declarative dialog component for Solim.
 * Extends Mindustry BaseDialog while providing full declarative configuration,
 * automated responsive width dispatch, and clean lifecycle management.
 */
public class SolimDialog extends BaseDialog implements Disposable, arc.util.Disposable {

    private final List<Runnable> shownListeners = new ArrayList<>();
    private final List<Consumer<Float>> resizeConsumers = new ArrayList<>();
    private boolean isShown = false;
    private boolean isDisposed = false;

    public SolimDialog(String title) {
        super(title != null ? title : "");
        this.resized(() -> notifyResize(calcResponsiveWidth()));
    }

    public static SolimDialog of(String title, Runnable content) {
        SolimDialog d = new SolimDialog(title);
        d.content(content);
        return d;
    }

    public SolimDialog content(Component component) {
        if (component != null) {
            cont.add(component.element()).grow();
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

    public SolimDialog onShown(Runnable listener) {
        if (listener != null) {
            shownListeners.add(listener);
        }
        return this;
    }

    public SolimDialog onResize(Consumer<Float> resizeConsumer) {
        if (resizeConsumer != null) {
            resizeConsumers.add(resizeConsumer);
            resizeConsumer.accept(calcResponsiveWidth());
        }
        return this;
    }

    public float calcResponsiveWidth() {
        if (Core.graphics == null) {
            return 800f;
        }
        return Core.graphics.getWidth() / Scl.scl() * 0.9f - 40f;
    }

    private void notifyResize(float width) {
        for (Consumer<Float> consumer : resizeConsumers) {
            consumer.accept(width);
        }
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
        for (Runnable r : shownListeners) {
            r.run();
        }
        notifyResize(calcResponsiveWidth());
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
    }
}
