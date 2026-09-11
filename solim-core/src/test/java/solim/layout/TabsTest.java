package solim.layout;

import static org.junit.jupiter.api.Assertions.*;

import arc.Core;
import arc.scene.event.ClickListener;
import arc.scene.event.InputEvent;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import solim.display.Text;
import solim.input.Button;
import solim.signal.Signal;

class TabsTest {

	@BeforeAll
	static void checkArcContext() {
		Assumptions.assumeTrue(Core.scene != null, "Arc Core.scene is null; skipping skin-dependent tests");
	}

	private void simulateClick(Button button) {
		InputEvent event = new InputEvent();
		button.sizedButton().getListeners().forEach(l -> {
			if (l instanceof ClickListener) {
				((ClickListener) l).clicked(event, 0f, 0f);
			}
		});
	}

	@Test
	void tabsInitialAndSignalSwitching() {
		Signal<Integer> activeTab = Signal.of(0);

		Tabs tabs = new Tabs(activeTab)
				.tab("Tab 0", () -> Text.of("Content 0"))
				.tab("Tab 1", () -> Text.of("Content 1"))
				.tab("Tab 2", () -> Text.of("Content 2"));

		assertEquals(3, tabs.buttons().size());
		assertEquals(3, tabs.contents().size());

		// Initial: Tab 0 is active
		assertTrue(tabs.buttons().get(0).sizedButton().isChecked());
		assertFalse(tabs.buttons().get(1).sizedButton().isChecked());
		assertFalse(tabs.buttons().get(2).sizedButton().isChecked());

		assertTrue(tabs.contents().get(0).visible);
		assertFalse(tabs.contents().get(1).visible);
		assertFalse(tabs.contents().get(2).visible);

		// Switch to Tab 1 via signal
		activeTab.set(1);
		assertFalse(tabs.buttons().get(0).sizedButton().isChecked());
		assertTrue(tabs.buttons().get(1).sizedButton().isChecked());
		assertFalse(tabs.buttons().get(2).sizedButton().isChecked());

		assertFalse(tabs.contents().get(0).visible);
		assertTrue(tabs.contents().get(1).visible);
		assertFalse(tabs.contents().get(2).visible);

		// Switch to Tab 2 via click
		simulateClick(tabs.buttons().get(2));
		assertEquals(2, activeTab.get().intValue());

		assertFalse(tabs.buttons().get(0).sizedButton().isChecked());
		assertFalse(tabs.buttons().get(1).sizedButton().isChecked());
		assertTrue(tabs.buttons().get(2).sizedButton().isChecked());

		assertFalse(tabs.contents().get(0).visible);
		assertFalse(tabs.contents().get(1).visible);
		assertTrue(tabs.contents().get(2).visible);

		tabs.dispose();
	}
}