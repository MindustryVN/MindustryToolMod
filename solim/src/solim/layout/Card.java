package solim.layout;

import arc.graphics.Color;
import arc.scene.Element;
import arc.scene.event.ClickListener;
import arc.scene.event.InputEvent;
import arc.scene.style.Drawable;
import arc.scene.ui.Button;
import arc.scene.ui.layout.Table;
import solim.core.Component;
import solim.core.ComponentContext;
import solim.core.Disposable;
import solim.signal.Effect;
import solim.signal.Readable;

import java.util.ArrayList;
import java.util.List;

/**
 * Clickable and stylable card container component with support for inner children,
 * reactive width/height/color bindings, and click event bubbling control.
 */
public final class Card implements Component, Disposable {

    public static class CardButton extends Button {
        private float customPrefWidth = -1f;
        private float customPrefHeight = -1f;

        public CardButton() {
            this(arc.Core.scene != null ? null : new ButtonStyle());
        }

        public CardButton(Drawable up) {
            super(new ButtonStyle());
            if (up != null) {
                getStyle().up = up;
            }
        }

        public CardButton(ButtonStyle style) {
            super((style != null || arc.Core.scene == null) ? (style != null ? style : new ButtonStyle()) : null);
        }

        public void setCustomPrefWidth(float width) {
            this.customPrefWidth = width;
            invalidateHierarchy();
        }

        public void setCustomPrefHeight(float height) {
            this.customPrefHeight = height;
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

    private final CardButton cardButton;
    private final Table container = new Table();
    private final List<Disposable> bindings = new ArrayList<>();
    private Runnable onClick;

    public Card() {
        this(new Button.ButtonStyle());
    }

    public Card(Drawable background) {
        this.cardButton = new CardButton(background);
        this.cardButton.top().left();
        this.container.top().left();
        this.cardButton.add(container).grow().top().left();
    }

    public Card(Button.ButtonStyle style) {
        this.cardButton = new CardButton(style != null ? style : new Button.ButtonStyle());
        this.cardButton.top().left();
        this.container.top().left();
        this.cardButton.add(container).grow().top().left();
    }

    public static Card of(Runnable children) {
        return new Card();
    }

    public static Card of(Button.ButtonStyle style, Runnable children) {
        return new Card(style);
    }

    public Table container() {
        return container;
    }

    public CardButton cardButton() {
        return cardButton;
    }

    @Override
    public Element element() {
        return cardButton;
    }

    public Card width(float width) {
        float val = Math.max(0f, width);
        cardButton.setWidth(val);
        cardButton.setCustomPrefWidth(val);
        cardButton.invalidateHierarchy();
        return this;
    }

    public Card width(Readable<Float> width) {
        if (width != null) {
            Effect e = Effect.of(() -> {
                Float w = width.get();
                float val = Math.max(0f, w != null ? w : 0f);
                cardButton.setWidth(val);
                cardButton.setCustomPrefWidth(val);
                cardButton.invalidateHierarchy();
            });
            bindings.add(e);
            ComponentContext.register(e);
        }
        return this;
    }

    public Card height(float height) {
        float val = Math.max(0f, height);
        cardButton.setHeight(val);
        cardButton.setCustomPrefHeight(val);
        cardButton.invalidateHierarchy();
        return this;
    }

    public Card height(Readable<Float> height) {
        if (height != null) {
            Effect e = Effect.of(() -> {
                Float h = height.get();
                float val = Math.max(0f, h != null ? h : 0f);
                cardButton.setHeight(val);
                cardButton.setCustomPrefHeight(val);
                cardButton.invalidateHierarchy();
            });
            bindings.add(e);
            ComponentContext.register(e);
        }
        return this;
    }

    public Card prefHeight(float prefHeight) {
        cardButton.setCustomPrefHeight(prefHeight);
        return this;
    }

    public Card prefWidth(float prefWidth) {
        cardButton.setCustomPrefWidth(prefWidth);
        return this;
    }

    public Card color(Color color) {
        if (color != null) {
            cardButton.setColor(color);
        }
        return this;
    }

    public Card color(Readable<Color> color) {
        if (color != null) {
            Effect e = Effect.of(() -> {
                Color c = color.get();
                if (c != null) {
                    cardButton.setColor(c);
                }
            });
            bindings.add(e);
            ComponentContext.register(e);
        }
        return this;
    }

    public Card padding(float padding) {
        container.margin(padding);
        return this;
    }

    public Card style(Button.ButtonStyle style) {
        if (style != null) {
            cardButton.setStyle(style);
        }
        return this;
    }

    public Card name(String name) {
        cardButton.name = name;
        return this;
    }

    public Card onClick(Runnable onClick) {
        this.onClick = onClick;
        if (onClick != null) {
            cardButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (event != null && event.stopped) {
                        return;
                    }
                    if (Card.this.onClick != null) {
                        try {
                            Card.this.onClick.run();
                        } catch (Exception e) {
                            arc.util.Log.err("Error executing card onClick", e);
                        }
                    }
                }
            });
        }
        return this;
    }

    @Override
    public void dispose() {
        for (Disposable d : bindings) {
            d.dispose();
        }
        bindings.clear();
    }
}
