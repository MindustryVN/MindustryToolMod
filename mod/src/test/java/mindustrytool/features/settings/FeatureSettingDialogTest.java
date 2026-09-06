package mindustrytool.features.settings;

import arc.Core;
import arc.struct.Seq;
import mindustrytool.features.Feature;
import mindustrytool.features.FeatureManager;
import mindustrytool.features.FeatureMetadata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import solim.signal.Signal;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class FeatureSettingDialogTest {

    private Feature testFeature;

    @BeforeEach
    void setUp() {
        testFeature = new Feature() {
            private boolean enabled = true;

            @Override
            public FeatureMetadata getMetadata() {
                return new FeatureMetadata("test-feature", null, 0, true, false, Optional.empty());
            }

            @Override
            public String getName() {
                return "Test Feature";
            }

            @Override
            public String getDescription() {
                return "Test Feature Description";
            }

            @Override
            public boolean isEnabled() {
                return enabled;
            }

            @Override
            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }
        };

        FeatureManager.register(testFeature);
    }

    @AfterEach
    void tearDown() {
        FeatureManager.getFeatures().remove(testFeature);
    }

    @Test
    void testFeatureCardReactiveWidth() {
        Signal<Float> width = Signal.of(300f);
        FeatureCard card = new FeatureCard(testFeature, width, () -> {});
        assertNotNull(card);
        width.set(400f);
        card.dispose();
        assertTrue(card.isDisposed());
    }

    @Test
    void testMatchesFilter() {
        // Match by name (case-insensitive)
        assertTrue(FeatureSettingsView.matchesFilter(testFeature, "test"));
        assertTrue(FeatureSettingsView.matchesFilter(testFeature, "FEATURE"));

        // Match by description
        assertTrue(FeatureSettingsView.matchesFilter(testFeature, "description"));

        // Match by ID
        assertTrue(FeatureSettingsView.matchesFilter(testFeature, "test-feature"));

        // Non-matching query
        assertFalse(FeatureSettingsView.matchesFilter(testFeature, "nomatch"));
    }

    @Test
    void testFeatureSettingsViewReactiveSignals() {
        FeatureSettingsView view = new FeatureSettingsView();

        // Initial state
        assertEquals("", view.filter().get());
        assertTrue(view.filteredFeatures().get().contains(testFeature));
        assertTrue(view.columnCount().get() >= 1);
        assertTrue(view.cardWidth().get() > 0);

        // Filtering by name
        view.filter().set("Test");
        assertTrue(view.filteredFeatures().get().contains(testFeature));

        // Filtering with non-matching query
        view.filter().set("NonExistentQueryXYZ");
        assertFalse(view.filteredFeatures().get().contains(testFeature));

        // Reset filter
        view.filter().set("");
        assertTrue(view.filteredFeatures().get().contains(testFeature));

        // Content width reactivity
        view.contentWidth().set(1200f);
        int cols = view.columnCount().get();
        assertEquals(Math.max(1, (int) (1200f / 340f)), cols);

        // Refresh triggers revision
        int revBefore = view.revision().get();
        view.refresh();
        assertEquals(revBefore + 1, view.revision().get());

        // Dispose cleans up
        view.dispose();
        assertTrue(view.isDisposed());
    }

    @Test
    void testEventListenerLifecycleAndUnregister() {
        FeatureSettingsView view = new FeatureSettingsView();
        int initialRev = view.revision().get();

        // Firing event increments revision
        arc.Events.fire(new mindustrytool.features.FeatureStateChanged(testFeature, false));
        assertEquals(initialRev + 1, view.revision().get(), "Event must trigger revision increment");

        // After disposing view, event listener must be unregistered
        view.dispose();
        arc.Events.fire(new mindustrytool.features.FeatureStateChanged(testFeature, true));
        assertEquals(initialRev + 1, view.revision().get(), "Disposed view must not receive events");
    }

    @Test
    void testReopeningViewWithoutDuplicateListeners() {
        FeatureSettingsView view1 = new FeatureSettingsView();
        view1.dispose();

        FeatureSettingsView view2 = new FeatureSettingsView();
        int initialRev = view2.revision().get();

        arc.Events.fire(new mindustrytool.features.FeatureStateChanged(testFeature, false));
        assertEquals(initialRev + 1, view2.revision().get(), "Only active view must receive event once");

        view2.dispose();
    }

    @Test
    void testResponsiveColumnsAndCardWidth() {
        FeatureSettingsView view = new FeatureSettingsView();

        view.updateWidth(340f);
        assertEquals(1, view.columnCount().get());
        assertEquals(340f, view.cardWidth().get(), 0.001f);

        view.updateWidth(680f);
        assertEquals(2, view.columnCount().get());
        assertEquals(340f, view.cardWidth().get(), 0.001f);

        view.updateWidth(1020f);
        assertEquals(3, view.columnCount().get());
        assertEquals(340f, view.cardWidth().get(), 0.001f);

        view.dispose();
    }

    @Test
    void testDialogLifecycleIfSceneAvailable() {
        if (Core.scene != null) {
            FeatureSettingDialog dialog = new FeatureSettingDialog();
            assertNotNull(dialog.view());
            assertFalse(dialog.isDisposed());

            dialog.dispose();
            assertTrue(dialog.isDisposed());
            assertTrue(dialog.view().isDisposed());
        }
    }
}
