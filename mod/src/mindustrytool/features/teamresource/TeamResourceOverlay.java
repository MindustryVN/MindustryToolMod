package mindustrytool.features.teamresource;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.scene.Element;
import arc.scene.event.ClickListener;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.event.Touchable;
import arc.scene.style.Drawable;
import arc.scene.ui.Dialog;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.Scaling;
import mindustry.Vars;
import mindustry.core.UI;
import mindustry.game.EventType.ResetEvent;
import mindustry.game.EventType.ResizeEvent;
import mindustry.game.EventType.WorldLoadEvent;
import mindustry.game.Team;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.type.UnitType;
import mindustry.ui.Styles;

/**
 * In-game HUD overlay component for displaying team resources, flow rates, units, and power.
 * Features a clean card-based grid layout with explicit cell sizing to prevent any text clipping or overlap.
 */
public class TeamResourceOverlay extends Table {
    private final TeamResourceState state;
    private final TeamResourceInputListener statsListener = new TeamResourceInputListener();
    private @Nullable Dialog settingsDialog;
    private boolean isHovered = false;

    public TeamResourceOverlay(TeamResourceState state) {
        this.state = state;
        name = "team-resources-overlay";
        touchable = Touchable.enabled;

        visible(() -> Vars.state.isGame() && Vars.ui.hudfrag != null && Vars.ui.hudfrag.shown);

        Events.run(WorldLoadEvent.class, () -> {
            state.onWorldLoad();
            Core.app.post(this::rebuild);
        });

        Events.run(ResetEvent.class, () -> {
            state.reset();
            clear();
        });

        Events.on(ResizeEvent.class, event -> rebuild());

        update(() -> {
            if (!visible) {
                return;
            }

            if (state.update()) {
                rebuild();
            }
        });
    }

    public TeamResourceState getState() {
        return state;
    }

    public @Nullable Team getSelectedTeam() {
        return state.getSelectedTeam();
    }

    public void setSelectedTeam(Team team) {
        state.setSelectedTeam(team);
        rebuild();
    }

    public Dialog getSettingDialog() {
        if (settingsDialog == null) {
            settingsDialog = new TeamResourceSettingsDialog(this);
        }
        return settingsDialog;
    }

    public void rebuild() {
        clear();

        this.color.a = TeamResourceConfig.opacity();
        Drawable bg = TeamResourceConfig.hideBackground() ? null : Styles.black6;
        setBackground(bg);

        float screenW = Core.scene != null ? Core.scene.getWidth() : Core.graphics.getWidth() / Scl.scl();
        float screenH = Core.scene != null ? Core.scene.getHeight() : Core.graphics.getHeight() / Scl.scl();
        float scale = TeamResourceConfig.scale();
        boolean expanded = TeamResourceConfig.isExpanded();

        // Calculate responsive overlay dimensions with sensible bounds
        float userWidth = screenW * TeamResourceConfig.overlayWidth();
        float minWidth = Vars.mobile ? 180f * scale : 220f * scale;
        float maxWidth = screenW * 0.98f;
        float widthToUse = Mathf.clamp(userWidth, minWidth, maxWidth);
        final float finalWidth = expanded ? widthToUse : Math.min(widthToUse, 240f * scale);

        float userHeight = screenH * TeamResourceConfig.overlayHeight();
        float maxHeight = screenH * 0.95f;
        final float maxOverlayHeight = Math.min(userHeight, maxHeight);

        final float[] totalHeightHolder = new float[]{44f * scale};

        table(content -> {
            content.top().left();

            // 1. Header: Drag handle, Expand toggle, Team selector, and Settings button
            content.table(header -> {
                header.left();

                // Drag handle
                header.button(Icon.move, Styles.clearNonei, () -> {})
                        .size(32f * scale)
                        .margin(2)
                        .get().addListener(new InputListener() {
                            float lastX;
                            float lastY;

                            @Override
                            public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
                                lastX = x;
                                lastY = y;
                                return true;
                            }

                            @Override
                            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                                moveBy(x - lastX, y - lastY);
                                keepInScreen();
                                TeamResourceConfig.x(TeamResourceOverlay.this.x);
                                TeamResourceConfig.y(TeamResourceOverlay.this.y);
                            }
                        });

                // Expand / Collapse toggle button (▼ / ▶)
                header.button(b -> {
                    b.label(() -> TeamResourceConfig.isExpanded() ? "▼" : "▶")
                            .style(Styles.outlineLabel)
                            .fontScale(0.8f * scale);
                }, Styles.clearNonei, () -> {
                    TeamResourceConfig.isExpanded(!TeamResourceConfig.isExpanded());
                    rebuild();
                }).size(32f * scale).padRight(4f * scale)
                  .tooltip(Core.bundle.get(expanded ? "team-resources.collapse" : "team-resources.expand", "Toggle Expand"));

                // Team chips list
                header.pane(p -> {
                    p.left();

                    Seq<Team> validTeams = new Seq<>();
                    for (Team team : Team.all) {
                        if (team.active() && team.data().hasCore()) {
                            validTeams.add(team);
                        }
                    }

                    for (Team team : validTeams) {
                        p.button(b -> b.image().size(20f * scale).color(team.color), Styles.clearTogglei, () -> {
                            state.setSelectedTeam(team);
                            rebuild();
                        }).checked(sel -> state.getSelectedTeam() == team).size(28f * scale).pad(2f * scale);
                    }

                    if (validTeams.size > 5) {
                        p.button("...", Styles.flatBordert, () -> new TeamResourceAllTeamsDialog(this))
                                .size(28f * scale).pad(2f * scale);
                    }
                }).scrollX(true).scrollY(false).height(32f * scale).growX();

                // Settings button
                header.button(Icon.settings, Styles.clearNonei, () -> getSettingDialog().show())
                        .size(32f * scale)
                        .margin(2)
                        .tooltip(Core.bundle.get("team-resources.settings.title", "Settings"));
            }).growX().padBottom(expanded ? 6f * scale : 0f).row();

            // 2. Expanded Body: Items, Units, and Power sections
            if (expanded) {
                float headerHeight = 36f * scale;
                float maxBodyHeight = Math.max(60f * scale, maxOverlayHeight - headerHeight - 16f * scale);

                Table stats = new Table();
                stats.top().left().margin(4f * scale);

                float availableWidth = finalWidth - 24f * scale;
                float minCardWidth = 70f * scale;
                int cols = Math.max(2, (int) (availableWidth / (minCardWidth + 4f * scale)));

                // Items Section with responsive Badge Cards stretching to fill width
                if (TeamResourceConfig.showItems() && Vars.content != null) {
                    stats.table(itemsTable -> {
                        itemsTable.left().defaults().growX();
                        itemsTable.touchable = Touchable.enabled;
                        itemsTable.addListener(statsListener);

                        float cardHeight = 36f * scale;
                        float iconSize = 20f * scale;

                        int i = 0;
                        for (Item item : Vars.content.items()) {
                            if (!state.getUsedItems().contains(item)) {
                                continue;
                            }

                            itemsTable.table(Styles.black3, itemCard -> {
                                itemCard.left().margin(2f * scale, 4f * scale, 2f * scale, 4f * scale);
                                itemCard.image(item.uiIcon).size(iconSize).padRight(3f * scale).scaling(Scaling.fit);

                                itemCard.table(labels -> {
                                    labels.left();
                                    labels.label(() -> state.getFormattedAmount(item))
                                            .style(Styles.outlineLabel)
                                            .fontScale(0.72f * scale)
                                            .left().row();

                                    labels.label(() -> (TeamResourceConfig.alwaysShowFlowRate() || state.isViewingStats())
                                            ? state.getFormattedRate(item)
                                            : "")
                                            .color(state.getRateColor(item))
                                            .style(Styles.outlineLabel)
                                            .fontScale(0.60f * scale)
                                            .left();
                                }).left();
                            }).growX().height(cardHeight).pad(2f * scale);

                            if (++i % cols == 0) {
                                itemsTable.row();
                            }
                        }

                        if (i % cols != 0) {
                            for (int k = i % cols; k < cols; k++) {
                                itemsTable.add().growX();
                            }
                        }
                    }).growX().row();
                }

                // Units Section with responsive Badge Cards
                Team selectedTeam = state.getSelectedTeam();
                if (TeamResourceConfig.showUnits() && selectedTeam != null && Vars.content != null) {
                    stats.table(unitsTable -> {
                        unitsTable.left().defaults().growX();

                        for (UnitType type : Vars.content.units()) {
                            int count = selectedTeam.data().countType(type);
                            if (count > 0) {
                                state.getUsedUnits().add(type);
                            }
                        }

                        float unitCardHeight = 30f * scale;
                        float unitIconSize = 22f * scale;

                        int i = 0;
                        for (UnitType type : Vars.content.units()) {
                            if (!state.getUsedUnits().contains(type)) {
                                continue;
                            }

                            unitsTable.table(Styles.black3, unitCard -> {
                                unitCard.left().margin(2f * scale, 4f * scale, 2f * scale, 4f * scale);
                                unitCard.image(type.uiIcon).size(unitIconSize).padRight(3f * scale).scaling(Scaling.fit);
                                unitCard.label(() -> {
                                    Team current = state.getSelectedTeam();
                                    if (current == null) {
                                        return "";
                                    }
                                    int c = current.data().countType(type);
                                    return c > 0 ? UI.formatAmount(c) : "";
                                }).style(Styles.outlineLabel).fontScale(0.72f * scale).left();
                            }).growX().height(unitCardHeight).pad(2f * scale);

                            if (++i % cols == 0) {
                                unitsTable.row();
                            }
                        }

                        if (i % cols != 0) {
                            for (int k = i % cols; k < cols; k++) {
                                unitsTable.add().growX();
                            }
                        }
                    }).growX().padTop(6f * scale).row();
                }

                // Power Section with clean spacing and SplitBar
                if (TeamResourceConfig.showPower()) {
                    stats.table(power -> {
                        power.left().defaults().growX();
                        float barHeight = 20f * scale;

                        if (state.getTeamGraphs().isEmpty()) {
                            power.add(Core.bundle.get("team-resources.no-power", "No power grid"))
                                    .fontScale(0.8f * scale)
                                    .color(Color.gray)
                                    .padTop(6f * scale);
                        } else {
                            // Power Balance label
                            power.table(balTable -> {
                                balTable.left();
                                float totalBalance = state.getTotalPowerBalance();
                                Color balColor = totalBalance >= 0 ? Pal.heal : Pal.remove;
                                String balText = Core.bundle.get("team-resources.power-prefix", "Power: ")
                                        + (totalBalance >= 0 ? "+" : "")
                                        + UI.formatAmount((long) (totalBalance * 60));

                                balTable.label(() -> balText)
                                        .style(Styles.outlineLabel)
                                        .color(balColor)
                                        .fontScale(0.82f * scale)
                                        .left();
                            }).padTop(6f * scale).padBottom(3f * scale).row();

                            power.add(new SplitBar(state.getTeamGraphs(), SplitBar.Mode.SATISFACTION, scale))
                                    .height(barHeight)
                                    .padBottom(4f * scale)
                                    .row();

                            if (TeamResourceConfig.showStoredPower()) {
                                // Stored Energy label
                                power.table(storeTable -> {
                                    storeTable.left();
                                    float totalStored = state.getTotalStoredPower();
                                    float totalCap = state.getTotalPowerCapacity();
                                    String storeText = Core.bundle.get("team-resources.stored-prefix", "Stored: ")
                                            + UI.formatAmount((long) totalStored) + " / "
                                            + UI.formatAmount((long) totalCap);

                                    storeTable.label(() -> storeText)
                                            .style(Styles.outlineLabel)
                                            .color(Color.lightGray)
                                            .fontScale(0.8f * scale)
                                            .left();
                                }).padTop(5f * scale).padBottom(3f * scale).row();

                                power.add(new SplitBar(state.getTeamGraphs(), SplitBar.Mode.STORED, scale))
                                        .height(barHeight)
                                        .padBottom(2f * scale)
                                        .row();
                            }
                        }
                    }).growX().padTop(Scl.scl(4f * scale));
                }

                // Fit content calculation: eliminate empty dead space completely
                float contentHeight = stats.getPrefHeight();
                float actualBodyHeight = Math.min(contentHeight, maxBodyHeight);

                ScrollPane pane = new ScrollPane(stats);
                pane.setScrollingDisabled(true, false);
                content.add(pane).scrollX(false).scrollY(true).growX().height(actualBodyHeight).row();

                totalHeightHolder[0] = headerHeight + actualBodyHeight + 16f * scale;
            }
        }).width(finalWidth).top().left().margin(4f * scale);

        float calculatedTotalHeight = totalHeightHolder[0];
        setSize(finalWidth + 8f * scale, calculatedTotalHeight + 8f * scale);
        invalidateHierarchy();
        keepInScreen();
    }

    private void keepInScreen() {
        float sw = getScene() != null ? getScene().getWidth() : Core.graphics.getWidth() / Scl.scl();
        float sh = getScene() != null ? getScene().getHeight() : Core.graphics.getHeight() / Scl.scl();
        float w = getWidth();
        float h = getHeight();

        x = Mathf.clamp(x, 0, Math.max(0, sw - w));
        y = Mathf.clamp(y, 0, Math.max(0, sh - h));
    }

    private class TeamResourceInputListener extends ClickListener {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
            try {
                state.setHoldingForStats(true);
                state.setViewingStats(true);
                state.clearSnapshot();
                return super.touchDown(event, x, y, pointer, button);
            } catch (Exception e) {
                state.setViewingStats(false);
                state.setHoldingForStats(false);
            }
            return false;
        }

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button) {
            try {
                state.setHoldingForStats(false);
                if (!Vars.mobile) {
                    if (!isHovered) {
                        state.setViewingStats(false);
                    }
                } else {
                    state.setViewingStats(false);
                }
                state.clearSnapshot();
                super.touchUp(event, x, y, pointer, button);
            } catch (Exception ignored) {
            }
        }

        @Override
        public void enter(InputEvent event, float x, float y, int pointer, @Nullable Element fromActor) {
            if (pointer == -1 && !state.isHoldingForStats()) {
                isHovered = true;
                state.setViewingStats(true);
                state.clearSnapshot();
            }
            super.enter(event, x, y, pointer, fromActor);
        }

        @Override
        public void exit(InputEvent event, float x, float y, int pointer, @Nullable Element toActor) {
            if (pointer == -1 && !state.isHoldingForStats()) {
                isHovered = false;
                state.setViewingStats(false);
                state.clearSnapshot();
            }
            super.exit(event, x, y, pointer, toActor);
        }
    }
}
