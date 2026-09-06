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

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;
import java.util.regex.Pattern;

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
    void testFeatureCardReactiveWidthAndEnabled() {
        Signal<Float> width = Signal.of(300f);
        Signal<Boolean> enabled = Signal.of(true);
        FeatureCard card = new FeatureCard(testFeature, width, enabled, () -> {});
        assertNotNull(card);
        assertTrue(card.enabled().get());

        width.set(400f);
        enabled.set(false);
        assertFalse(card.enabled().get());

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
    void testPropertyReactivityWithoutStructuralRebuild() {
        FeatureSettingsView view = new FeatureSettingsView();
        Signal<Boolean> featureSignal = view.getFeatureEnabledSignal(testFeature);
        assertTrue(featureSignal.get());

        int initialRev = view.revision().get();

        // Firing event for specific feature updates feature signal directly
        testFeature.setEnabled(false);
        arc.Events.fire(new mindustrytool.features.FeatureStateChanged(testFeature, false));

        assertFalse(featureSignal.get(), "Feature enabled signal must update");
        // Structural revision is not required for single feature property toggle
        assertEquals(initialRev, view.revision().get(), "Property update should not bump structural revision");

        view.dispose();
    }

    @Test
    void testEventListenerLifecycleAndUnregister() {
        FeatureSettingsView view = new FeatureSettingsView();
        Signal<Boolean> featureSignal = view.getFeatureEnabledSignal(testFeature);
        assertTrue(featureSignal.get());

        // Firing event updates feature state
        testFeature.setEnabled(false);
        arc.Events.fire(new mindustrytool.features.FeatureStateChanged(testFeature, false));
        assertFalse(featureSignal.get());

        // After disposing view, event listener must be unregistered
        view.dispose();
        testFeature.setEnabled(true);
        arc.Events.fire(new mindustrytool.features.FeatureStateChanged(testFeature, true));
        // Disposed view signal stays unchanged
        assertFalse(featureSignal.get(), "Disposed view must not receive events");
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

            dialog.show();
            assertTrue(dialog.isShown());

            dialog.dispose();
            assertTrue(dialog.isDisposed());
            assertTrue(dialog.view().isDisposed());
        }
    }

    private static File findSourceFile(String relativePath) {
        File f = new File(relativePath);
        if (f.exists()) return f;
        f = new File("mod/" + relativePath);
        if (f.exists()) return f;
        f = new File("../mod/" + relativePath);
        if (f.exists()) return f;
        return new File(relativePath);
    }

    @Test
    void testArchitecturalRulesPureSolimSettingDialog() throws Exception {
        Pattern ownPattern = Pattern.compile("\\bown\\(");
        Pattern ownChildPattern = Pattern.compile("\\bownChild\\(");

        // Enforce that Solim.java does not exist in solim library
        File solimJavaFile = new File("solim/src/solim/Solim.java");
        if (!solimJavaFile.exists()) {
            solimJavaFile = new File("../solim/src/solim/Solim.java");
        }
        assertFalse(solimJavaFile.exists(), "Solim.java must not exist in solim UI library");

        File dialogFile = findSourceFile("src/mindustrytool/features/settings/FeatureSettingDialog.java");
        assertTrue(dialogFile.exists(), "FeatureSettingDialog source file must exist");

        String dialogContent = new String(Files.readAllBytes(dialogFile.toPath()), StandardCharsets.UTF_8);
        assertFalse(ownPattern.matcher(dialogContent).find(), "FeatureSettingDialog must not call own()");
        assertFalse(ownChildPattern.matcher(dialogContent).find(), "FeatureSettingDialog must not call ownChild()");

        // Verify other files
        String[] paths = {
                "src/mindustrytool/features/settings/FeatureCard.java",
                "src/mindustrytool/features/settings/FeatureSettingsView.java"
        };

        for (String p : paths) {
            File f = findSourceFile(p);
            if (f.exists()) {
                String content = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
                assertFalse(ownPattern.matcher(content).find(), "File " + p + " must not call own()");
                assertFalse(ownChildPattern.matcher(content).find(), "File " + p + " must not call ownChild()");
                if (p.endsWith("FeatureCard.java")) {
                    assertFalse(content.contains("public static void build("), "FeatureCard must not have static build()");
                    assertFalse(content.contains("getPrefWidth()"), "FeatureCard must not override getPrefWidth()");
                }
            }
        }
    }

    @Test
    void testFeatureSettingDialogHelpers() {
        assertNotNull(FeatureSettingDialog.bundle("non.existent.key", "fallback"));
        assertEquals("fallback", FeatureSettingDialog.bundle("non.existent.key", "fallback"));
        assertEquals("key", FeatureSettingDialog.bundle("key"));
        assertEquals("key", FeatureSettingDialog.bundleFormat("key", "val"));
        assertTrue(FeatureSettingDialog.calcContentWidth() > 0f);
        assertNull(FeatureSettingDialog.icon(null));
        assertNull(FeatureSettingDialog.icon("nonExistentIcon12345"));
    }
}
