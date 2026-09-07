package mindustrytool.features.settings;

import arc.Core;
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
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class FeatureSettingDialogTest {

    private Feature testFeature;

    @BeforeEach
    void setUp() {
        if (Core.settings == null) {
            Core.settings = new arc.Settings();
        }
        testFeature = new Feature() {
            private final FeatureMetadata metadata = FeatureMetadata.builder()
                    .id("test-feature")
                    .icon(null)
                    .order(0)
                    .enabledByDefault(true)
                    .quickAccess(false)
                    .keybind(null)
                    .build();

            @Override
            public FeatureMetadata getMetadata() {
                return metadata;
            }

            @Override
            public String getName() {
                return "Test Feature";
            }

            @Override
            public String getDescription() {
                return "Test Feature Description";
            }
        };

        FeatureManager.register(testFeature);
    }

    @AfterEach
    void tearDown() {
        FeatureManager.unregister(testFeature);
    }

    @Test
    void testFeatureCardReactiveWidthAndEnabled() {
        Signal<Float> width = Signal.of(300f);
        Signal<Boolean> enabled = Signal.of(true);
        FeatureCard card = new FeatureCard(testFeature, width, enabled, () -> {
        });
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
    void testFeatureManagerSignalReactivity() {
        assertTrue(FeatureManager.getFeatures().contains(testFeature));
        assertTrue(FeatureManager.features().get().contains(testFeature));

        Feature dynamicFeature = new Feature() {
            private final FeatureMetadata metadata = FeatureMetadata.builder()
                    .id("dynamic-feature")
                    .icon(null)
                    .order(1)
                    .enabledByDefault(true)
                    .quickAccess(false)
                    .keybind(null)
                    .build();

            @Override
            public FeatureMetadata getMetadata() {
                return metadata;
            }

            @Override
            public String getName() {
                return "Dynamic Feature";
            }
        };

        FeatureManager.register(dynamicFeature);
        assertTrue(FeatureManager.features().get().contains(dynamicFeature));

        FeatureManager.unregister(dynamicFeature);
        assertFalse(FeatureManager.features().get().contains(dynamicFeature));
    }

    @Test
    void testPropertyReactivityViaDirectFeatureSignal() {
        assertTrue(testFeature.enabled().get());
        assertTrue(testFeature.isEnabled());

        // Modifying feature state directly updates feature enabled Signal
        testFeature.setEnabled(false);
        assertFalse(testFeature.enabled().get(), "Feature enabled signal must update");
        assertFalse(testFeature.isEnabled());

        testFeature.setEnabled(true);
        assertTrue(testFeature.enabled().get());
        assertTrue(testFeature.isEnabled());
    }

    private static File findSourceFile(String relativePath) {
        File f = new File(relativePath);
        if (f.exists())
            return f;
        f = new File("mod/" + relativePath);
        if (f.exists())
            return f;
        f = new File("../mod/" + relativePath);
        if (f.exists())
            return f;
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
        assertFalse(dialogContent.contains("void onDispose()"), "FeatureSettingDialog must not override onDispose()");

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
                    assertFalse(content.contains("public static void build("),
                            "FeatureCard must not have static build()");
                    assertFalse(content.contains("getPrefWidth()"), "FeatureCard must not override getPrefWidth()");
                    assertFalse(content.contains("Binding.bind"),
                            "FeatureCard must not use separate Binding utility class");
                    assertTrue(content.contains("card("), "FeatureCard must use solim card facade");
                }
            }
        }
    }
}
