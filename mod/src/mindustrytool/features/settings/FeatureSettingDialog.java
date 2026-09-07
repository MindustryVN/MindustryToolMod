package mindustrytool.features.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import arc.Core;
import arc.scene.Element;
import arc.scene.Group;
import lombok.AllArgsConstructor;
import lombok.Data;
import mindustry.gen.Icon;
import mindustrytool.Config;
import mindustrytool.utils.JsonUtils;
import solim.overlay.SolimDialog;

public final class FeatureSettingDialog extends SolimDialog {
    public FeatureSettingDialog() {
        super(Core.bundle.get("feature.dialog.title", "Features"));
        addCloseButton();
        closeOnBack();
        content(new FeatureSettingsView());
        actionButton(Core.bundle.get("feature.button.report-bug"), Icon.infoCircle,
                () -> Core.app.openURI(Config.DISCORD_INVITE_URL));
        actionButton("Copy UI tree", () -> {
            UiNode root = new UiNode(
                    "Scene",
                    0f,
                    0f,
                    Core.graphics.getWidth(),
                    Core.graphics.getHeight(),
                    new ArrayList<>());

            discoverSolimElements(root, Core.scene.root, element -> isSolimElement(element) && element instanceof FeatureSettingDialog);

            Core.app.setClipboardText(JsonUtils.toJsonPretty(root));
        });
    }

    private void discoverSolimElements(UiNode parent, Element element, Predicate<Element> pred) {
        if (pred.test(element)) {
            parent.children.add(buildUiTree(element));
            return;
        }

        if (element instanceof Group group) {
            for (Element child : group.getChildren()) {
                discoverSolimElements(parent, child, pred);
            }
        }
    }

    private UiNode buildUiTree(Element element) {
        UiNode node = createNode(element);

        if (element instanceof Group group) {
            for (Element child : group.getChildren()) {
                node.children.add(buildUiTree(child));
            }
        }

        return node;
    }

    private boolean isSolimElement(Element element) {
        Package pkg = element.getClass().getPackage();

        return pkg != null
                && pkg.getName().startsWith("solim.") || isSolinPackage(getClass());
    }

    private boolean isSolinPackage(Class<?> clazz) {
        return clazz.getPackageName().startsWith("solim.") || isSolinPackage(clazz.getSuperclass());
    }

    private UiNode createNode(Element element) {
        return new UiNode(
                element.getClass().getSimpleName(),
                element.x,
                element.y,
                element.getWidth(),
                element.getHeight(),
                new ArrayList<>());
    }

    @AllArgsConstructor
    @Data
    private static class UiNode {
        public String name;
        public float x, y;
        public float width, height;
        public List<UiNode> children;
    }
}
