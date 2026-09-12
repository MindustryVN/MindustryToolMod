package mindustrytool.features.teamresource;

import arc.Core;
import mindustry.game.Team;
import mindustry.gen.Tex;
import mindustry.ui.Styles;
import solim.UI;
import solim.overlay.SolimDialog;

/**
 * Modal dialog for selecting an active team when many teams exist, using SolimDialog.
 */
public class TeamResourceAllTeamsDialog extends SolimDialog {

    public TeamResourceAllTeamsDialog(TeamResourceState state) {
        super(Core.bundle.get("team-resources.all-teams", "All Teams"));

        name("teamResourceAllTeamsDialog");
        addCloseButton();
        closeOnBack();

        children(() -> {
            UI.scroll().size(360f, 280f).children(() -> {
                UI.dynamic(state.validTeamsSignal, teams -> UI.grid(3).gap(UI.unit(1)).children(() -> {
                    if (teams != null) {
                        for (Team team : teams) {
                            UI.button()
                                    .style(Styles.clearTogglei)
                                    .checked(state.selectedTeamSignal.map(sel -> sel == team))
                                    .onClick(() -> {
                                        state.setSelectedTeam(team);
                                        hide();
                                    })
                                    .growX()
                                    .children(() -> {
                                        UI.row().left().gap(UI.unit(1)).padding(UI.unit(1)).children(() -> {
                                            UI.image(Tex.whiteui).size(UI.unit(4)).color(team.color);
                                            UI.text(team.localized()).color(team.color);
                                        });
                                    });
                        }
                    }
                }));
            });
        });
    }
}
