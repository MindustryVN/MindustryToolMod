package mindustrytool.services.update;

import arc.Core;
import arc.graphics.Color;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Table;
import arc.util.Log;
import arc.util.Timer;
import mindustry.Vars;
import mindustry.ui.dialogs.BaseDialog;
import mindustrytool.Config;

/**
 * Refactored from {@code old.mindustrytool.services.UpdateAvailableDialog}. All user-visible text
 * via {@code Core.bundle}.
 */
public class UpdateDialog extends BaseDialog {

	public UpdateDialog(String currentVer, String latestVer, String changelog, Runnable done) {
		super(Core.bundle.get("update.dialog.title"));
		name = "updateAvailableDialog";

		Table table = new Table();
		table.defaults().left();

		String newVersionText = Core.bundle.format(
				"update.message.new-version",
				"[#" + Color.crimson.toString() + "]" + currentVer,
				"[#" + Color.green.toString() + "]" + latestVer);

		table.add(newVersionText).wrap().width(500f).padBottom(20).row();

		table.add("Discord: " + Config.DISCORD_INVITE_URL)
				.color(Color.royal)
				.padTop(5f)
				.row();

		table.image().height(4f).color(Color.gray).fillX().pad(10f).row();

		Table changelogTable = new Table();
		changelogTable.top().left();
		changelogTable.add(changelog).growX().wrap().width(480f).left();

		ScrollPane pane = new ScrollPane(changelogTable);
		table.add(pane).size(500f, 400f).scrollX(false).row();

		cont.add(table);

		String cancelLabel = Core.bundle.get("update.button.cancel");
		String updateLabel = Core.bundle.get("update.button.update");

		buttons.button(cancelLabel, () -> {
					remove();
					done.run();
				})
				.size(100f, 50f);

		buttons.button(updateLabel, () -> {
					try {
						remove();
						Vars.ui.mods.show();
						String repoUrl = Config.ORG_NAME + "/" + Config.REPO_NAME;
						Vars.ui.mods.githubImportMod(repoUrl, true, true);
						Vars.ui.mods.toFront();
						Timer.schedule(() -> Vars.ui.loadfrag.toFront(), 0.2f);
					} catch (Throwable e) {
						Log.err(e);
						Vars.ui.showException(e);
					}
				})
				.size(100f, 50f);
	}
}
