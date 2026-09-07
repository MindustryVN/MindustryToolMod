package solim.input;

import arc.scene.Element;
import arc.scene.event.ClickListener;
import arc.scene.event.InputEvent;
import arc.scene.style.Drawable;
import arc.scene.ui.ImageButton;
import arc.scene.ui.Label;
import arc.scene.ui.Tooltip;
import solim.core.Component;
import solim.core.ComponentContext;
import solim.core.Disposable;
import solim.signal.Effect;
import solim.signal.Readable;

import java.util.ArrayList;
import java.util.List;

/**
 * Button widget displaying an icon drawable with support for sizing, tooltips,
 * reactive state, and click event bubbling isolation.
 */
public final class IconButton implements Component, Disposable {

    public static class SizedImageButton extends ImageButton {
        private float customPrefWidth = -1f;
        private float customPrefHeight = -1f;

        public SizedImageButton() {
            this(null, arc.Core.scene != null ? null : new ImageButtonStyle());
        }

        public SizedImageButton(Drawable icon) {
            this(icon, arc.Core.scene != null ? null : new ImageButtonStyle());
        }

        public SizedImageButton(Drawable icon, ImageButtonStyle style) {
            super(icon != null ? icon : new arc.scene.style.TextureRegionDrawable(new arc.graphics.g2d.TextureRegion()),
                    (style != null || arc.Core.scene == null) ? (style != null ? style : new ImageButtonStyle()) : null);
        }

        public void setCustomSize(float width, float height) {
            this.customPrefWidth = width;
            this.customPrefHeight = height;
            setSize(width, height);
            invalidateHierarchy();
        }

        @Override
        public float getPrefWidth() {
            return customPrefWidth >= 0 ? customPrefWidth : super.getPrefWidth();
        }

        @Override
        public float getPrefHeight() {
            return customPrefHeight >= 0 ? customPrefHeight : super.getPrefHeight();
        }
    }

    private final SizedImageButton imageButton;
    private final List<Disposable> bindings = new ArrayList<>();
    private boolean stopClickPropagation = true;
    private Runnable onClick;

    public IconButton() {
        this(new SizedImageButton());
    }

    public IconButton(Drawable icon) {
        this(new SizedImageButton(icon));
    }

    public IconButton(Drawable icon, ImageButton.ImageButtonStyle style) {
        this(new SizedImageButton(icon, style));
    }

    public IconButton(SizedImageButton imageButton) {
        this.imageButton = imageButton;
    }

    public static IconButton of(Drawable icon, Runnable onClick) {
        IconButton b = new IconButton(icon);
        b.onClick(onClick);
        return b;
    }

    public static IconButton of(Drawable icon, ImageButton.ImageButtonStyle style, Runnable onClick) {
        IconButton b = new IconButton(icon, style);
        b.onClick(onClick);
        return b;
    }

    public IconButton onClick(Runnable action) {
        this.onClick = action;
        if (action != null) {
            imageButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (stopClickPropagation && event != null) {
                        event.stop();
                    }
                    if (onClick != null) {
                        onClick.run();
                    }
                }
            });
        }
        return this;
    }

    public IconButton stopClickPropagation(boolean stop) {
        this.stopClickPropagation = stop;
        return this;
    }

    public boolean isStopClickPropagation() {
        return stopClickPropagation;
    }

    public IconButton size(float size) {
        return size(size, size);
    }

    public IconButton size(float width, float height) {
        imageButton.setCustomSize(width, height);
        return this;
    }

    public IconButton style(ImageButton.ImageButtonStyle style) {
        if (style != null) {
            imageButton.setStyle(style);
        }
        return this;
    }

    public IconButton tooltip(String tip) {
        if (tip != null && !tip.isEmpty()) {
            try {
                imageButton.addListener(new Tooltip(t -> t.add(tip)));
            } catch (Throwable ignored) {
            }
        }
        return this;
    }

    public IconButton tooltip(Readable<String> tip) {
        if (tip != null) {
            try {
                imageButton.addListener(new Tooltip(t -> {
                    Label label = new Label("");
                    Effect e = Effect.of(() -> label.setText(tip.get() != null ? tip.get() : ""));
                    bindings.add(e);
                    ComponentContext.register(e);
                    t.add(label);
                }));
            } catch (Throwable ignored) {
            }
        }
        return this;
    }

    public IconButton enabled(Readable<Boolean> signal) {
        if (signal != null) {
            Effect e = Effect.of(() -> imageButton.setDisabled(!Boolean.TRUE.equals(signal.get())));
            bindings.add(e);
            ComponentContext.register(e);
        }
        return this;
    }

    public IconButton visible(Readable<Boolean> signal) {
        if (signal != null) {
            Effect e = Effect.of(() -> imageButton.visible = Boolean.TRUE.equals(signal.get()));
            bindings.add(e);
            ComponentContext.register(e);
        }
        return this;
    }

    public ImageButton imageButton() {
        return imageButton;
    }

    @Override
    public Element element() {
        return imageButton;
    }

    @Override
    public void dispose() {
        for (Disposable d : bindings) {
            d.dispose();
        }
        bindings.clear();
    }
}
