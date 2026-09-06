package mindustrytool.features.settings;

import arc.Core;
import arc.scene.style.Drawable;
import arc.scene.ui.layout.Scl;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustrytool.Config;
import solim.overlay.SolimDialog;

/**
 * Declarative Solim dialog for managing mod features, toggling their states,
 * and accessing individual settings and documentation.
 * Declares platform/game helper utilities previously held in Solim.
 */
public final class FeatureSettingDialog extends SolimDialog {

    private final FeatureSettingsView view = new FeatureSettingsView();

    public FeatureSettingDialog() {
        super(bundle("feature.dialog.title", "Features"));

        addCloseButton();
        closeOnBack();

        content(view);

        actionButton(
                bundle("feature.button.report-bug"),
                icon("infoCircle"),
                180f,
                50f,
                () -> openUri(Config.DISCORD_INVITE_URL, bundle("feature.toast.copied"))
        );

        onShown(view::onShown);
        onResize(view::updateWidth);
    }

    public static float calcContentWidth() {
        if (Core.graphics == null) {
            return 800f;
        }
        return Core.graphics.getWidth() / Scl.scl() * 0.9f - 40f;
    }

    public static String bundle(String key) {
        if (Core.bundle != null && Core.bundle.has(key)) {
            return Core.bundle.get(key);
        }
        return key;
    }

    public static String bundle(String key, String fallback) {
        if (Core.bundle != null && Core.bundle.has(key)) {
            return Core.bundle.get(key);
        }
        return fallback;
    }

    public static String bundleFormat(String key, Object... values) {
        if (Core.bundle != null && Core.bundle.has(key)) {
            return Core.bundle.format(key, values);
        }
        return key;
    }

    public static Drawable icon(String name) {
        if (name == null) {
            return null;
        }
        try {
            java.lang.reflect.Field field = Icon.class.getField(name);
            return (Drawable) field.get(null);
        } catch (Throwable t) {
            return null;
        }
    }

    public static boolean openUri(String uri, String fallbackToast) {
        if (Core.app != null && Core.app.openURI(uri)) {
            return true;
        }
        if (Core.app != null && uri != null) {
            Core.app.setClipboardText(uri);
        }
        if (Vars.ui != null && fallbackToast != null) {
            Vars.ui.showInfoFade(fallbackToast);
        }
        return false;
    }

    public FeatureSettingsView view() {
        return view;
    }

    @Override
    protected void onDispose() {
        view.dispose();
    }
}
