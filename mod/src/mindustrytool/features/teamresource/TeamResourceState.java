package mindustrytool.features.teamresource;

import arc.Core;
import arc.graphics.Color;
import mindustry.graphics.Pal;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import arc.util.Interval;
import arc.util.Log;
import arc.util.Nullable;
import mindustry.Vars;
import mindustry.core.UI;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.type.Item;
import mindustry.type.UnitType;
import mindustry.world.blocks.power.PowerGraph;
import mindustry.world.modules.ItemModule;

/**
 * Encapsulates game logic and state for tracking team resources, flow rates, units, and power grids.
 */
public class TeamResourceState {
    private final ObjectSet<Item> usedItems = new ObjectSet<>();
    private final ObjectSet<UnitType> usedUnits = new ObjectSet<>();
    private final Interval timer = new Interval(2);

    private @Nullable Team selectedTeam;
    private @Nullable ItemModule coreItems;
    private @Nullable ItemModule lastSnapshot;
    private @Nullable ItemModule rateDisplay;

    private boolean viewingStats = false;
    private boolean holdingForStats = false;
    private Seq<PowerGraph> teamGraphs = new Seq<>();

    public void reset() {
        usedItems.clear();
        usedUnits.clear();
        if (lastSnapshot != null) {
            lastSnapshot.clear();
        }
        if (rateDisplay != null) {
            rateDisplay.clear();
        }
        selectedTeam = Vars.player.team();
        teamGraphs.clear();
    }

    public void onWorldLoad() {
        reset();
    }

    public boolean update() {
        boolean needsRebuild = false;

        if (selectedTeam == null || !selectedTeam.active()) {
            selectedTeam = Vars.player.team();
            needsRebuild = true;
        }

        coreItems = selectedTeam.core() != null ? selectedTeam.core().items : null;
        if (coreItems == null) {
            return needsRebuild;
        }

        if (Vars.content != null && Vars.content.items().contains(item -> coreItems.get(item) > 0 && usedItems.add(item))) {
            needsRebuild = true;
        }

        if (timer.get(0, 30f)) {
            updateRates();

            if (TeamResourceConfig.showPower()) {
                if (updatePowerStats()) {
                    needsRebuild = true;
                }
            }
        }

        return needsRebuild;
    }

    public @Nullable Team getSelectedTeam() {
        return selectedTeam;
    }

    public void setSelectedTeam(Team team) {
        this.selectedTeam = team;
        this.usedUnits.clear();
        if (lastSnapshot != null) {
            lastSnapshot.clear();
        }
    }

    public @Nullable ItemModule getCoreItems() {
        return coreItems;
    }

    public ObjectSet<Item> getUsedItems() {
        return usedItems;
    }

    public ObjectSet<UnitType> getUsedUnits() {
        return usedUnits;
    }

    public Seq<PowerGraph> getTeamGraphs() {
        return teamGraphs;
    }

    public boolean isViewingStats() {
        return viewingStats;
    }

    public void setViewingStats(boolean viewingStats) {
        this.viewingStats = viewingStats;
    }

    public boolean isHoldingForStats() {
        return holdingForStats;
    }

    public void setHoldingForStats(boolean holdingForStats) {
        this.holdingForStats = holdingForStats;
    }

    public void clearSnapshot() {
        if (lastSnapshot != null) {
            lastSnapshot.clear();
        }
    }

    public int getItemAmount(Item item) {
        return coreItems != null ? coreItems.get(item) : 0;
    }

    public int getItemRate(Item item) {
        return rateDisplay != null ? rateDisplay.get(item) : 0;
    }

    public String getFormattedAmount(Item item) {
        if (coreItems == null) {
            return "0";
        }
        return UI.formatAmount(coreItems.get(item));
    }

    public String getFormattedRate(Item item) {
        if (rateDisplay == null) {
            return "0/s";
        }
        int rate = rateDisplay.get(item);
        if (rate == 0) {
            return "0/s";
        }
        String prefix = rate > 0 ? "+" : "";
        return prefix + rate + "/s";
    }

    public Color getRateColor(Item item) {
        if (rateDisplay == null) {
            return Color.gray;
        }
        int rate = rateDisplay.get(item);
        if (rate > 0) {
            return Pal.heal;
        }
        if (rate < 0) {
            return Pal.remove;
        }
        return Color.gray;
    }

    public CharSequence formatItem(Item item) {
        if (coreItems == null) {
            return "0";
        }

        try {
            int amount = coreItems.get(item);
            int rate = rateDisplay != null ? rateDisplay.get(item) : 0;

            if (TeamResourceConfig.alwaysShowFlowRate()) {
                return formatAmountWithRate(amount, rate);
            }

            if (viewingStats) {
                return formatRate(rate);
            }

            String color = "[white]";
            if (rate < 0) {
                color = "[scarlet]";
            } else if (rate > 0) {
                color = "[lime]";
            }

            return color + UI.formatAmount(amount);
        } catch (Exception e) {
            Log.err("Failed to format item", e);
            return "0";
        }
    }

    public float getTotalPowerBalance() {
        float total = 0f;
        for (PowerGraph graph : teamGraphs) {
            total += graph.getPowerBalance();
        }
        return total;
    }

    public float getTotalStoredPower() {
        float total = 0f;
        for (PowerGraph graph : teamGraphs) {
            total += graph.getLastPowerStored();
        }
        return total;
    }

    public float getTotalPowerCapacity() {
        float total = 0f;
        for (PowerGraph graph : teamGraphs) {
            total += graph.getLastCapacity();
        }
        return total;
    }

    private void updateRates() {
        if (coreItems == null || Vars.content == null) {
            return;
        }
        if (lastSnapshot == null) {
            lastSnapshot = new ItemModule();
        }
        if (rateDisplay == null) {
            rateDisplay = new ItemModule();
        }

        if (lastSnapshot.any()) {
            coreItems.each((item, amount) -> rateDisplay.set(item, (amount - lastSnapshot.get(item)) * 2));
        }
        lastSnapshot.set(coreItems);
    }

    private boolean updatePowerStats() {
        if (selectedTeam == null) {
            return false;
        }

        ObjectSet<PowerGraph> found = new ObjectSet<>();
        Groups.build.each(b -> {
            if (b.team == selectedTeam && b.power != null && b.power.graph != null) {
                PowerGraph graph = b.power.graph;
                if (graph.getLastPowerProduced() > 0 || graph.getLastCapacity() > 0 || graph.getLastPowerNeeded() > 0) {
                    found.add(graph);
                }
            }
        });

        Seq<PowerGraph> newGraphs = found.toSeq();
        newGraphs.sort((a, b) -> {
            int cap = Float.compare(b.getLastCapacity(), a.getLastCapacity());
            if (cap != 0) {
                return cap;
            }
            return Integer.compare(a.hashCode(), b.hashCode());
        });

        if (!newGraphs.equals(teamGraphs)) {
            teamGraphs = newGraphs;
            return true;
        }
        return false;
    }

    private String formatAmountWithRate(int amount, int rate) {
        String color = "[white]";
        if (rate < 0) {
            color = "[scarlet]";
        } else if (rate > 0) {
            color = "[lime]";
        }
        return color + UI.formatAmount(amount) + " [gray](" + formatRate(rate) + "[gray])";
    }

    private String formatRate(int rate) {
        if (rate == 0) {
            return "0/s";
        }
        String prefix = rate > 0 ? "[lime]+" : "[scarlet]";
        return prefix + rate + "[gray]/s";
    }
}
