package solim.overlay;

import arc.Core;
import arc.Events;
import arc.func.Cons;
import arc.scene.Element;
import arc.scene.event.Touchable;
import arc.scene.style.Drawable;
import arc.scene.ui.layout.Table;
import arc.util.Nullable;
import java.util.ArrayList;
import java.util.List;
import mindustry.game.EventType.ResizeEvent;
import solim.core.Component;
import solim.core.ComponentContext;
import solim.core.Disposable;
import solim.layout.LayoutModifiers;
import solim.layout.Row;
import solim.layout.SizeConstraints;
import solim.layout.SizedTable;
import solim.modifier.ElementModifiers;
import solim.signal.Effect;
import solim.signal.Readable;
import solim.signal.Signal;
import solim.ui.ParentStack;

/**
 * Floating non-modal HUD overlay component.
 * Root element defaults to {@link Touchable#childrenOnly} so background touches pass through.
 * Content children are nested within an inner enabled container.
 * Automatically adapts to screen resize events via {@link #keepInScreen()}.
 */
public class Hud implements Component, Disposable, LayoutModifiers<Hud> {

	private final SizedTable root;
	private final Table container;
	private final List<Disposable> bindings = new ArrayList<>();
	private final Cons<ResizeEvent> resizeListener;
	private boolean disposed = false;

	public Hud() {
		this.root = new SizedTable();
		this.root.name = "solim-hud-root";
		this.root.touchable = Touchable.childrenOnly;

		this.root.userObject = this;

		this.container = new Table();
		this.container.name = "solim-hud-container";
		this.container.touchable = Touchable.enabled;
		this.container.userObject = this;

		this.root.add(container).pad(0).margin(0);

		this.resizeListener = e -> keepInScreen();
		Events.on(ResizeEvent.class, resizeListener);

		ComponentContext.register(this);
	}

	@Override
	public Element element() {
		return root;
	}

	public SizedTable root() {
		return root;
	}

	public Table container() {
		return container;
	}

	@Override
	public SizeConstraints sizeConstraints() {
		return root.getSizeConstraints();
	}

	public Hud name(String name) {
		ElementModifiers.name(root, name);
		return this;
	}

	public Hud touchable(Touchable touchable) {
		root.touchable = touchable;
		return this;
	}

	public Hud containerTouchable(Touchable touchable) {
		container.touchable = touchable;
		return this;
	}

	public static @Nullable Hud find(@Nullable Element element) {
		Element cur = element;
		while (cur != null) {
			if (cur.userObject instanceof Hud) {
				return (Hud) cur.userObject;
			}
			cur = cur.parent;
		}
		Table t = ParentStack.find(table -> table != null && table.userObject instanceof Hud);
		if (t != null && t.userObject instanceof Hud) {
			return (Hud) t.userObject;
		}
		return null;
	}

	public Hud background(@Nullable Drawable bg) {
		container.background(bg);
		return this;
	}

	public Hud children(@Nullable Runnable r) {
		ParentStack.push(container, Row.ATTACHER);
		try {
			if (r != null) {
				r.run();
			}
		} finally {
			ParentStack.pop();
		}
		root.pack();
		return this;
	}

	public Hud x(float x) {
		ElementModifiers.x(root, x);
		return this;
	}

	public Hud x(Readable<Float> x) {
		if (x != null) {
			Effect e = Effect.of(() -> {
				Float v = x.get();
				if (v != null) {
					ElementModifiers.x(root, v);
				}
			});
			bindings.add(e);
			ComponentContext.register(e);
		}
		return this;
	}

	public Hud y(float y) {
		ElementModifiers.y(root, y);
		return this;
	}

	public Hud y(Readable<Float> y) {
		if (y != null) {
			Effect e = Effect.of(() -> {
				Float v = y.get();
				if (v != null) {
					ElementModifiers.y(root, v);
				}
			});
			bindings.add(e);
			ComponentContext.register(e);
		}
		return this;
	}

	public Hud position(float x, float y) {
		ElementModifiers.position(root, x, y);
		return this;
	}

	public Hud position(Readable<Float> x, Readable<Float> y) {
		x(x);
		y(y);
		return this;
	}

	public Hud opacity(float opacity) {
		ElementModifiers.opacity(container, opacity);
		return this;
	}

	public Hud opacity(Readable<Float> opacity) {
		if (opacity != null) {
			Effect e = Effect.of(() -> {
				Float v = opacity.get();
				if (v != null) {
					ElementModifiers.opacity(container, v);
				}
			});
			bindings.add(e);
			ComponentContext.register(e);
		}
		return this;
	}

	public Hud alpha(float alpha) {
		return opacity(alpha);
	}

	public Hud alpha(Readable<Float> alpha) {
		return opacity(alpha);
	}

	public Hud scale(float s) {
		container.setScale(s);
		return this;
	}

	public Hud scale(Readable<Float> s) {
		if (s != null) {
			Effect e = Effect.of(() -> {
				Float v = s.get();
				if (v != null) {
					container.setScale(v);
				}
			});
			bindings.add(e);
			ComponentContext.register(e);
		}
		return this;
	}

	public Hud draggable(Element handle) {
		ElementModifiers.draggable(handle, this);
		return this;
	}

	public Hud draggable(Element handle, @Nullable Signal<Float> xSignal, @Nullable Signal<Float> ySignal) {
		ElementModifiers.draggable(handle, this, xSignal, ySignal);
		return this;
	}

	public void keepInScreen() {
		float sw = Core.scene != null ? Core.scene.getWidth() : (Core.graphics != null ? Core.graphics.getWidth() : 0f);
		float sh = Core.scene != null ? Core.scene.getHeight() : (Core.graphics != null ? Core.graphics.getHeight() : 0f);
		if (sw <= 0f || sh <= 0f) return;

		float w = root.getWidth();
		float h = root.getHeight();

		float curX = root.x;
		float curY = root.y;

		if (curX < 0) curX = 0;
		if (curY < 0) curY = 0;
		if (curX + w > sw) curX = Math.max(0, sw - w);
		if (curY + h > sh) curY = Math.max(0, sh - h);

		root.setPosition(curX, curY);
	}

	public void pack() {
		root.pack();
	}

	@Override
	public void dispose() {
		if (disposed) return;
		disposed = true;
		Events.remove(ResizeEvent.class, resizeListener);
		for (Disposable d : bindings) {
			d.dispose();
		}
		bindings.clear();
	}
}
