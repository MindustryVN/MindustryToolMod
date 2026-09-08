package mindustrytool.features.teamresource;

import arc.Core;
import mindustry.game.Team;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

public class TeamResourceAllTeamsDialog extends BaseDialog {
    public TeamResourceAllTeamsDialog(TeamResourceOverlay overlay) {
        super(Core.bundle.get("team-resources.all-teams", "All Teams"));
        name = "teamResourceAllTeamsDialog";
        addCloseButton();

        cont.pane(p -> {
            p.defaults().width(150f).height(50f).pad(5f);
            int i = 0;
            for (Team team : Team.all) {
                if (!team.active() || !team.data().hasCore()) {
                    continue;
                }

                p.button(b -> {
                    b.image().color(team.color).margin(4f).size(24f).padRight(10f);
                    b.add(team.localized()).color(team.color);
                }, Styles.flatTogglet, () -> {
                    overlay.setSelectedTeam(team);
                    hide();
                }).checked(team == overlay.getSelectedTeam());

                if (++i % 3 == 0) {
                    p.row();
                }
            }
        }).grow();

        Core.app.post(this::show);
    }
}
