package solim.ui;

import static org.junit.jupiter.api.Assertions.*;

import arc.Core;
import org.junit.jupiter.api.Test;

class SettingsPanelTest {

	@Test
	void reactiveStateAndComputed() {
		SettingsPanel panel = new SettingsPanel();
		assertFalse(panel.darkMode().get());
		assertFalse(panel.dirty().get());
		assertTrue(panel.saveText().get().contains("Save Changes"));

		panel.dirty().set(true);
		assertTrue(panel.dirty().get());
		assertTrue(panel.saveText().get().contains("●"));

		panel.darkMode().set(true);
		assertTrue(panel.darkMode().get());

		panel.dispose();
	}

	@Test
	void renderIfSceneAvailable() {
		if (Core.scene != null) {
			SettingsPanel panel = new SettingsPanel();
			assertNotNull(panel.element());
			panel.dispose();
		}
	}
}
