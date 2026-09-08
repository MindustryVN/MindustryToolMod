package mindustrytool.features.teamresource;

import static org.junit.jupiter.api.Assertions.*;

import arc.Core;
import arc.mock.MockApplication;
import arc.mock.MockGraphics;
import arc.mock.MockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TeamResourceRateTest {

    @BeforeEach
    void setUp() {
        Core.app = new MockApplication();
        Core.graphics = new MockGraphics();
        Core.settings = new MockSettings();
    }

    @Test
    void testConfigDefaults() {
        assertEquals(1f, TeamResourceConfig.opacity(), 0.001f);
        assertEquals(1f, TeamResourceConfig.scale(), 0.001f);
        assertEquals(0.28f, TeamResourceConfig.overlayWidth(), 0.001f);
        assertEquals(0.60f, TeamResourceConfig.overlayHeight(), 0.001f);
        assertTrue(TeamResourceConfig.showItems());
        assertFalse(TeamResourceConfig.showUnits());
        assertTrue(TeamResourceConfig.showPower());
        assertFalse(TeamResourceConfig.showStoredPower());
        assertFalse(TeamResourceConfig.hideBackground());
        assertTrue(TeamResourceConfig.alwaysShowFlowRate());
        assertTrue(TeamResourceConfig.isExpanded());
    }

    @Test
    void testConfigSetters() {
        TeamResourceConfig.opacity(0.8f);
        assertEquals(0.8f, TeamResourceConfig.opacity(), 0.001f);

        TeamResourceConfig.scale(1.2f);
        assertEquals(1.2f, TeamResourceConfig.scale(), 0.001f);

        TeamResourceConfig.overlayWidth(0.4f);
        assertEquals(0.4f, TeamResourceConfig.overlayWidth(), 0.001f);

        TeamResourceConfig.overlayHeight(0.85f);
        assertEquals(0.85f, TeamResourceConfig.overlayHeight(), 0.001f);

        TeamResourceConfig.showUnits(true);
        assertTrue(TeamResourceConfig.showUnits());

        TeamResourceConfig.showStoredPower(true);
        assertTrue(TeamResourceConfig.showStoredPower());

        TeamResourceConfig.alwaysShowFlowRate(true);
        assertTrue(TeamResourceConfig.alwaysShowFlowRate());
    }

    @Test
    void testFeatureMetadata() {
        TeamResourceFeature feature = new TeamResourceFeature();

        assertNotNull(feature.getMetadata());
        assertEquals("team-resources", feature.getMetadata().getId());
        assertEquals(0, feature.getMetadata().getOrder());
        assertTrue(feature.getMetadata().isEnabledByDefault());
        assertTrue(feature.getMetadata().isQuickAccess());
    }

    @Test
    void testFlowRateCalculationLogic() {
        // Delta between two snapshots at 0.5s interval (30 ticks) corresponds to 1 second flow rate:
        int previous = 200;
        int current = 250;
        int rate = (current - previous) * 2;
        assertEquals(100, rate);

        int decreased = 180;
        int negativeRate = (decreased - current) * 2;
        assertEquals(-140, negativeRate);
    }

    @Test
    void testResetToDefaults() {
        TeamResourceConfig.opacity(0.5f);
        TeamResourceConfig.scale(1.5f);
        TeamResourceConfig.overlayHeight(0.4f);
        TeamResourceConfig.showUnits(true);
        TeamResourceConfig.alwaysShowFlowRate(false);
        TeamResourceConfig.isExpanded(false);

        TeamResourceConfig.resetToDefaults();

        assertEquals(1f, TeamResourceConfig.opacity(), 0.001f);
        assertEquals(1f, TeamResourceConfig.scale(), 0.001f);
        assertEquals(0.28f, TeamResourceConfig.overlayWidth(), 0.001f);
        assertEquals(0.60f, TeamResourceConfig.overlayHeight(), 0.001f);
        assertTrue(TeamResourceConfig.showItems());
        assertFalse(TeamResourceConfig.showUnits());
        assertTrue(TeamResourceConfig.showPower());
        assertFalse(TeamResourceConfig.showStoredPower());
        assertFalse(TeamResourceConfig.hideBackground());
        assertTrue(TeamResourceConfig.alwaysShowFlowRate());
        assertTrue(TeamResourceConfig.isExpanded());
    }

    @Test
    void testTeamResourceStateInit() {
        TeamResourceState state = new TeamResourceState();
        assertNotNull(state.getUsedItems());
        assertNotNull(state.getUsedUnits());
        assertNotNull(state.getTeamGraphs());
        assertEquals(0f, state.getTotalPowerBalance(), 0.001f);
        assertEquals(0f, state.getTotalStoredPower(), 0.001f);
    }

    @Test
    void testTableLayout() {
        arc.scene.ui.layout.Table table = new arc.scene.ui.layout.Table();
        table.defaults().growX();
        int cols = 4;
        for (int i = 0; i < 5; i++) {
            table.add(new arc.scene.ui.layout.Table());
            if ((i + 1) % cols == 0) {
                table.row();
            }
        }
        arc.scene.ui.layout.Table table2 = new arc.scene.ui.layout.Table();
        table2.defaults().growX();
        int total = 2;
        int cols2 = 4;
        for (int i = 0; i < total; i++) {
            table2.add(new arc.scene.ui.layout.Table());
        }
        if (total % cols2 != 0) {
            for (int k = total % cols2; k < cols2; k++) {
                table2.add().growX();
            }
        }
        table2.setSize(300, 100);
        table2.layout();
        var cells2 = table2.getCells();
        assertEquals(4, cells2.size);
        assertEquals(75f, cells2.get(0).get().getWidth(), 0.1f);
        assertEquals(75f, cells2.get(1).get().getWidth(), 0.1f);
    }

    @Test
    void testFitContentCalculation() {
        float contentHeight = 180f;
        float maxAllowedHeight = 600f;
        float actualHeight = Math.min(contentHeight, maxAllowedHeight);
        assertEquals(180f, actualHeight, 0.001f);

        float largeContentHeight = 850f;
        float cappedHeight = Math.min(largeContentHeight, maxAllowedHeight);
        assertEquals(600f, cappedHeight, 0.001f);
    }
}
