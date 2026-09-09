package solim.layout;

import arc.scene.Element;
import arc.scene.style.Drawable;
import arc.scene.ui.Button.ButtonStyle;
import arc.util.Nullable;
import java.util.ArrayList;
import java.util.List;
import solim.core.Component;
import solim.core.ComponentContext;
import solim.core.Disposable;
import solim.display.SolimImage;
import solim.display.Text;
import solim.input.Button;
import solim.modifier.ElementModifiers;
import solim.signal.Effect;
import solim.signal.Readable;
import solim.signal.Signal;
import solim.ui.ParentStack;

/**
 * Tabs layout component: provides a tab header button bar and switches between tab content panels reactively.
 */
public final class Tabs implements Component, LayoutModifiers<Tabs> {

	private final SizedTable root;
	private final SizedTable headerBar;
	private final SolimStack contentStack;
	private final Signal<Integer> activeTab;
	private @Nullable ButtonStyle tabButtonStyle;
	private final List<Button> tabButtons = new ArrayList<>();
	private final List<SizedTable> tabContents = new ArrayList<>();
	private final List<Disposable> bindings = new ArrayList<>();

	public Tabs(Signal<Integer> activeTab) {
		this.activeTab = activeTab;
		this.root = new SizedTable();
		this.root.name = "solim-tabs-root";
		this.root.top().left();

		this.headerBar = new SizedTable();
		this.headerBar.name = "solim-tabs-headerBar";
		this.headerBar.top().left();
		this.root.add(headerBar).growX().row();

		this.contentStack = new SolimStack();
		this.root.add(contentStack.element()).grow();

		ComponentContext.register(this);
	}

	public static Tabs of(Signal<Integer> activeTab) {
		return new Tabs(activeTab);
	}

	public Tabs tabStyle(@Nullable ButtonStyle style) {
		this.tabButtonStyle = style;
		return this;
	}

	public Tabs tab(String title, Runnable contentBuilder) {
		return tab(Readable.of(title), null, contentBuilder);
	}

	public Tabs tab(Readable<String> title, Runnable contentBuilder) {
		return tab(title, null, contentBuilder);
	}

	public Tabs tab(String title, @Nullable Drawable icon, Runnable contentBuilder) {
		return tab(Readable.of(title), icon, contentBuilder);
	}

	public Tabs tab(Readable<String> title, @Nullable Drawable icon, Runnable contentBuilder) {
		int index = tabButtons.size();

		Button.SizedButton sizedBtn = new Button.SizedButton(tabButtonStyle);

		Button btn = new Button(sizedBtn);
		btn.onClick(() -> activeTab.set(index));
		btn.checked(activeTab.map(idx -> idx != null && idx == index));
		btn.growX();

		btn.children(() -> {
			if (icon != null) {
				new SolimImage(icon);
			}
			Text.of(title);
		});
		tabButtons.add(btn);
		headerBar.add(btn.element()).growX();

		SizedTable contentContainer = new SizedTable();
		contentContainer.top().left();
		contentContainer.getSizeConstraints().growX = true;
		contentContainer.getSizeConstraints().growY = true;
		ParentStack.push(contentContainer, Column.ATTACHER);
		try {
			if (contentBuilder != null) {
				contentBuilder.run();
			}
		} finally {
			ParentStack.pop();
		}

		Effect eff = Effect.of(() -> {
			Integer cur = activeTab.get();
			contentContainer.visible = (cur != null && cur == index);
		});
		bindings.add(eff);
		ComponentContext.register(eff);

		tabContents.add(contentContainer);
		contentStack.add(contentContainer);

		return this;
	}

	public List<Button> buttons() {
		return tabButtons;
	}

	public List<SizedTable> contents() {
		return tabContents;
	}

	public SizedTable root() {
		return root;
	}

	public SizedTable headerBar() {
		return headerBar;
	}

	@Override
	public Element element() {
		return root;
	}

	@Override
	public SizeConstraints sizeConstraints() {
		return root.getSizeConstraints();
	}

	public Tabs name(String name) {
		ElementModifiers.name(root, name);
		return this;
	}

	@Override
	public void dispose() {
		for (Disposable d : bindings) {
			d.dispose();
		}
		bindings.clear();
		for (Button b : tabButtons) {
			b.dispose();
		}
	}
}
