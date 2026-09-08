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
public final class Card implements Component, Disposable, LayoutModifiers<Card> {

	public static final ParentStack.Attacher ATTACHER = (table, child) -> {
		Cell<?> cell = table.add(child);
		if (Ui.isExpanding(child)) {
			cell.growY();
		}
		cell.row();
		return cell;
	};

	/**
	 * Custom Button subclass that implements {@link ConstrainedElement}, so that ATTACHERs
	 * can read and apply size constraints from the parent cell when {@code Card} is attached.
	 */
	public static class CardButton extends Button implements ConstrainedElement {

		private final SizeConstraints constraints = new SizeConstraints();

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

		@Override
		public SizeConstraints getSizeConstraints() {
			return constraints;
		}

		@Override
		public float getPrefWidth() {
			if (constraints == null) return super.getPrefWidth();
			Float v = constraints.prefWidth != null ? constraints.prefWidth.get() : null;
			return v != null ? Math.max(0f, v) : super.getPrefWidth();
		}

		@Override
		public float getPrefHeight() {
			if (constraints == null) return super.getPrefHeight();
			Float v = constraints.prefHeight != null ? constraints.prefHeight.get() : null;
			return v != null ? Math.max(0f, v) : super.getPrefHeight();
		}

		@Override
		public float getMinWidth() {
			if (constraints == null) return super.getMinWidth();
			Float v = constraints.minWidth != null ? constraints.minWidth.get() : null;
			return v != null ? Math.max(0f, v) : super.getMinWidth();
		}

		@Override
		public float getMinHeight() {
			if (constraints == null) return super.getMinHeight();
			Float v = constraints.minHeight != null ? constraints.minHeight.get() : null;
			return v != null ? Math.max(0f, v) : super.getMinHeight();
		}

		@Override
		public float getMaxWidth() {
			if (constraints == null) return super.getMaxWidth();
			Float v = constraints.maxWidth != null ? constraints.maxWidth.get() : null;
			return v != null ? Math.max(0f, v) : super.getMaxWidth();
		}

		@Override
		public float getMaxHeight() {
			if (constraints == null) return super.getMaxHeight();
			Float v = constraints.maxHeight != null ? constraints.maxHeight.get() : null;
			return v != null ? Math.max(0f, v) : super.getMaxHeight();
		}
	}

	private final CardButton cardButton;
	private final Table container = new Table();
	private final List<Disposable> bindings = new ArrayList<>();
	private @Nullable Runnable onClick;

	public Card() {
		this(new Button.ButtonStyle());
	}

	public Card(Drawable background) {
		this.cardButton = new CardButton(background);
		this.cardButton.name = "solim-card-cardButton";
		this.container.name = "solim-card-container";
		this.cardButton.top().left();
		this.container.top().left();
		this.cardButton.add(container).grow().top().left();
	}

	public Card(Button.ButtonStyle style) {
		this.cardButton = new CardButton(style != null ? style : new Button.ButtonStyle());
		this.cardButton.name = "solim-card-cardButton";
		this.container.name = "solim-card-container";
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

	@Override
	public SizeConstraints sizeConstraints() {
		return cardButton.getSizeConstraints();
	}

	public Card name(String name) {
		ElementModifiers.name(cardButton, name);
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

	@Override
	public Card top() {
		ElementModifiers.top(container);
		return LayoutModifiers.super.top();
	}

	@Override
	public Card bottom() {
		ElementModifiers.bottom(container);
		return LayoutModifiers.super.bottom();
	}

	@Override
	public Card left() {
		ElementModifiers.left(container);
		return LayoutModifiers.super.left();
	}

	@Override
	public Card right() {
		ElementModifiers.right(container);
		return LayoutModifiers.super.right();
	}

	@Override
	public Card center() {
		ElementModifiers.center(container);
		return LayoutModifiers.super.center();
	}

	public Card gap(float g) {
		ElementModifiers.gap(container, g);
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
