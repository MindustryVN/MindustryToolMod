package mindustrytool.features.teamresource;

import static solim.ui.Ui.unit;

import arc.Core;
import arc.scene.Element;
import mindustry.game.Team;
import mindustry.gen.Tex;
import mindustry.ui.Styles;
import solim.core.BaseComponent;
import solim.overlay.SolimDialog;
import solim.ui.Ui;

/**
 * Modal dialog for selecting an active team when many teams exist, using SolimDialog.
 */
public class TeamResourceAllTeamsDialog extends SolimDialog {

    public TeamResourceAllTeamsDialog(TeamResourceState state) {
        super(Core.bundle.get("team-resources.all-teams", "All Teams"));

        name = "teamResourceAllTeamsDialog";
        addCloseButton();
        closeOnBack();

        content(new BaseComponent() {
            @Override
            protected Element build() {
                return Ui.scroll().size(360f, 280f).children(() -> {
                    Ui.dynamic(state.validTeamsSignal, teams -> Ui.grid(3).gap(unit(1)).children(() -> {
                        if (teams != null) {
                            for (Team team : teams) {
                                Ui.button()
                                        .style(Styles.clearTogglei)
                                        .checked(state.selectedTeamSignal.map(sel -> sel == team))
                                        .onClick(() -> {
                                            state.setSelectedTeam(team);
                                            hide();
                                        })
                                        .growX()
                                        .children(() -> {
                                            Ui.row().left().gap(unit(1)).padding(unit(1)).children(() -> {
                                                Ui.image(Tex.whiteui).size(unit(4)).color(team.color);
                                                Ui.text(team.localized()).color(team.color);
                                            });
                                        });
                            }
                        }
                    }));
                }).element();
            }
        });
    }
}
