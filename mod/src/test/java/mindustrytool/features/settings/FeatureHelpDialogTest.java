package mindustrytool.features.settings;

import arc.Core;
import arc.scene.Element;
import arc.util.I18NBundle;
import mindustrytool.features.Feature;
import mindustrytool.features.FeatureMetadata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class FeatureHelpDialogTest {

    private Feature featureWithHelp;
    private Feature featureWithoutHelp;

    @BeforeEach
    void setUp() {
        if (Core.settings == null) {
            Core.settings = new arc.Settings();
        }
        if (Core.bundle == null) {
            try {
                File bundleFile = new File("assets/bundles/bundle.properties");
                if (!bundleFile.exists()) {
                    bundleFile = new File("../assets/bundles/bundle.properties");
                }
                if (bundleFile.exists()) {
                    Core.bundle = I18NBundle.createBundle(arc.files.Fi.get(bundleFile.getParentFile().getPath() + "/bundle"));
                }
            } catch (Exception ignored) {
            }
        }

        featureWithHelp = new Feature() {
            private final FeatureMetadata metadata = FeatureMetadata.builder()
                    .id("test-help-feature")
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
                return "Test Help Feature";
            }

            @Override
            public String getDescription() {
                return "This is a test description";
            }

            @Override
            public String getHelp() {
                return "Detailed instructions on how to use this feature.";
            }
        };

        featureWithoutHelp = new Feature() {
            private final FeatureMetadata metadata = FeatureMetadata.builder()
                    .id("test-no-help-feature")
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
                return "No Help Feature";
            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public String getHelp() {
                return null;
            }
        };
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testFeatureHelpViewBuildsWithHelp() {
        Assumptions.assumeTrue(Core.scene != null, "Arc Core.scene is null; skipping skin-dependent tests");
        FeatureHelpView view = new FeatureHelpView(featureWithHelp);
        Element element = view.element();
        assertNotNull(element, "FeatureHelpView element must not be null");
        view.dispose();
    }

    @Test
    void testFeatureHelpViewBuildsWithoutHelp() {
        Assumptions.assumeTrue(Core.scene != null, "Arc Core.scene is null; skipping skin-dependent tests");
        FeatureHelpView view = new FeatureHelpView(featureWithoutHelp);
        Element element = view.element();
        assertNotNull(element, "FeatureHelpView element must not be null even when feature has no help");
        view.dispose();
    }

    @Test
    void testFeatureHelpDialogInstantiationAndDisposal() {
        Assumptions.assumeTrue(Core.scene != null, "Arc Core.scene is null; skipping skin-dependent tests");
        FeatureHelpDialog dialog = new FeatureHelpDialog(featureWithHelp);
        assertNotNull(dialog, "FeatureHelpDialog must be instantiable");
        assertTrue(dialog.isFillParent(), "FeatureHelpDialog must default to full screen as SolimDialog");

        dialog.dispose();
        assertTrue(dialog.isDisposed(), "FeatureHelpDialog must be disposed cleanly");
    }

    @Test
    void testArchitecturalRulesPureSolimHelpDialog() throws Exception {
        Pattern ownPattern = Pattern.compile("\\bown\\(");
        Pattern ownChildPattern = Pattern.compile("\\bownChild\\(");

        File dialogFile = findSourceFile("src/mindustrytool/features/settings/FeatureHelpDialog.java");
        assertTrue(dialogFile.exists(), "FeatureHelpDialog source file must exist");

        String dialogContent = new String(Files.readAllBytes(dialogFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(dialogContent.contains("extends SolimDialog"), "FeatureHelpDialog must extend SolimDialog");
        assertFalse(ownPattern.matcher(dialogContent).find(), "FeatureHelpDialog must not call own()");
        assertFalse(ownChildPattern.matcher(dialogContent).find(), "FeatureHelpDialog must not call ownChild()");
        assertFalse(dialogContent.contains("void onDispose()"), "FeatureHelpDialog must not override onDispose()");

        File viewFile = findSourceFile("src/mindustrytool/features/settings/FeatureHelpView.java");
        assertTrue(viewFile.exists(), "FeatureHelpView source file must exist");

        String viewContent = new String(Files.readAllBytes(viewFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(viewContent.contains("extends BaseComponent"), "FeatureHelpView must extend BaseComponent");
        assertFalse(ownPattern.matcher(viewContent).find(), "FeatureHelpView must not call own()");
        assertFalse(ownChildPattern.matcher(viewContent).find(), "FeatureHelpView must not call ownChild()");
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
}
