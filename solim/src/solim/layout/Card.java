package solim.layout;

import arc.graphics.Color;
import arc.scene.Element;
import arc.scene.event.ClickListener;
import arc.scene.event.InputEvent;
import arc.scene.style.Drawable;
import arc.scene.ui.Button;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.util.Log;
import arc.util.Nullable;
import java.util.ArrayList;
import java.util.List;
import solim.core.Component;
import solim.core.ComponentContext;
import solim.core.Disposable;
import solim.modifier.ElementModifiers;
import solim.signal.Effect;
import solim.signal.Readable;
import solim.ui.ParentStack;
import solim.ui.Ui;

/**
 * Clickable and stylable card container component with support for inner children, reactive
 * width/height/color bindings, and click event bubbling control.
 */
public final class Card implements Component, Disposable {

	public static final ParentStack.Attacher ATTACHER = (table, child) -> {
		Cell<?> cell = table.add(child);
		cell.growX();
		if (Ui.isExpanding(child)) {
			cell.growY();
		}
		cell.row();
		return cell;
	};

	public static class CardButton extends Button {
		private float customPrefWidth = -1f;
		private float customPrefHeight = -1f;

		public CardButton() {
			this(new ButtonStyle());
		}

		public CardButton(Drawable up) {
			super(new ButtonStyle());
			if (up != null) {
				getStyle().up = up;
			}
		}

		public CardButton(@Nullable ButtonStyle style) {
			super(style != null ? style : new ButtonStyle());
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
		ElementModifiers.width(cardButton, width);
		return this;
	}

	public Card width(Readable<Float> width) {
		if (width != null) {
			Effect e = Effect.of(() -> {
				Float w = width.get();
				if (w != null) {
					width(w);
				}
			});
			bindings.add(e);
			ComponentContext.register(e);
		}
		return this;
	}

	public Card height(float height) {
		ElementModifiers.height(cardButton, height);
		return this;
	}

	public Card height(Readable<Float> height) {
		if (height != null) {
			Effect e = Effect.of(() -> {
				Float h = height.get();
				if (h != null) {
					height(h);
				}
			});
			bindings.add(e);
			ComponentContext.register(e);
		}
		return this;
	}

	public Card size(float width, float height) {
		ElementModifiers.size(cardButton, width, height);
		return this;
	}

	public Card size(float size) {
		ElementModifiers.size(cardButton, size);
		return this;
	}

	public Card prefHeight(float prefHeight) {
		return height(prefHeight);
	}

	public Card prefWidth(float prefWidth) {
		return width(prefWidth);
	}

	public Card x(float x) {
		ElementModifiers.x(cardButton, x);
		return this;
	}

	public Card y(float y) {
		ElementModifiers.y(cardButton, y);
		return this;
	}

	public Card position(float x, float y) {
		ElementModifiers.position(cardButton, x, y);
		return this;
	}

	public Card visible(boolean visible) {
		ElementModifiers.visible(cardButton, visible);
		return this;
	}

	public Card top() {
		ElementModifiers.top(container);
		return this;
	}

	public Card bottom() {
		ElementModifiers.bottom(container);
		return this;
	}

	public Card left() {
		ElementModifiers.left(container);
		return this;
	}

	public Card right() {
		ElementModifiers.right(container);
		return this;
	}

	public Card center() {
		ElementModifiers.center(container);
		return this;
	}

	public Card gap(float g) {
		ElementModifiers.gap(container, g);
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

	public Card children(@Nullable Runnable r) {
		ParentStack.push(container, ATTACHER);
		try {
			if (r != null) {
				r.run();
			}
		} finally {
			ParentStack.pop();
		}
		ParentStack.attachToParent(cardButton);
		return this;
	}

	public Card padding(float p) {
		ElementModifiers.padding(container, p);
		return this;
	}

	public Card padding(float top, float left, float bottom, float right) {
		ElementModifiers.padding(container, top, left, bottom, right);
		return this;
	}

	public Card paddingTop(float top) {
		ElementModifiers.paddingTop(container, top);
		return this;
	}

	public Card paddingBottom(float bottom) {
		ElementModifiers.paddingBottom(container, bottom);
		return this;
	}

	public Card paddingLeft(float left) {
		ElementModifiers.paddingLeft(container, left);
		return this;
	}

	public Card paddingRight(float right) {
		ElementModifiers.paddingRight(container, right);
		return this;
	}

	public Card margin(float m) {
		ElementModifiers.margin(container, m);
		return this;
	}

	public Card margin(float top, float left, float bottom, float right) {
		ElementModifiers.margin(container, top, left, bottom, right);
		return this;
	}

	public Card marginTop(float top) {
		ElementModifiers.marginTop(container, top);
		return this;
	}

	public Card marginBottom(float bottom) {
		ElementModifiers.marginBottom(container, bottom);
		return this;
	}

	public Card marginLeft(float left) {
		ElementModifiers.marginLeft(container, left);
		return this;
	}

	public Card marginRight(float right) {
		ElementModifiers.marginRight(container, right);
		return this;
	}

	public Card pad(float p) {
		ElementModifiers.pad(container, p);
		return this;
	}

	public Card pad(float top, float left, float bottom, float right) {
		ElementModifiers.pad(container, top, left, bottom, right);
		return this;
	}

	public Card padTop(float top) {
		ElementModifiers.padTop(container, top);
		return this;
	}

	public Card padBottom(float bottom) {
		ElementModifiers.padBottom(container, bottom);
		return this;
	}

	public Card padLeft(float left) {
		ElementModifiers.padLeft(container, left);
		return this;
	}

	public Card padRight(float right) {
		ElementModifiers.padRight(container, right);
		return this;
	}

	public Card style(Button.ButtonStyle style) {
		if (style != null) {
			cardButton.setStyle(style);
		}
		return this;
	}
	
    public Card background(Drawable background) {
		if (background != null) {
			cardButton.setBackground(background);
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
							Log.err("Error executing card onClick", e);
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
