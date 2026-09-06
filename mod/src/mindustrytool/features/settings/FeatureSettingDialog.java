package mindustrytool.features.settings;

import arc.Core;
import arc.scene.ui.layout.Scl;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.ui.dialogs.BaseDialog;
import mindustrytool.Config;
import solim.core.Disposable;

/**
 * Dialog for managing mod features, toggling their states, and accessing their
 * individual settings and documentation.
 * Mindustry BaseDialog host that mounts the declarative FeatureSettingsView.
 */
public final class FeatureSettingDialog extends BaseDialog implements Disposable, arc.util.Disposable {

    private FeatureSettingsView view;
    private boolean isDisposed = false;

    public FeatureSettingDialog() {
        super(Core.bundle != null ? Core.bundle.get("feature.dialog.title") : "Features");

        view = new FeatureSettingsView();

        addCloseButton();
        closeOnBack();

        buildButtons();

        cont.add(view.element()).grow();

        shown(() -> {
            if (view.isDisposed()) {
                view = new FeatureSettingsView();
                cont.clear();
                cont.add(view.element()).grow();
            }
            view.onShown();
        });

        resized(() -> {
            if (!view.isDisposed()) {
                view.updateWidth(calcWidth());
            }
        });

        hidden(() -> {
            view.dispose();
        });
    }

    private void buildButtons() {
        if (Core.bundle != null && buttons != null) {
            buttons.button(Core.bundle.get("feature.button.report-bug"), Icon.infoCircle, () -> {
                if (!Core.app.openURI(Config.DISCORD_INVITE_URL)) {
                    Core.app.setClipboardText(Config.DISCORD_INVITE_URL);
                    Vars.ui.showInfoFade(Core.bundle.get("feature.toast.copied"));
                }
            }).size(180f, 50f);
        }
    }

    private static float calcWidth() {
        if (Core.graphics == null) {
            return 800f;
        }
        return Core.graphics.getWidth() / Scl.scl() * 0.9f - 40f;
    }

    public FeatureSettingsView view() {
        return view;
    }

    @Override
    public boolean isDisposed() {
        return isDisposed;
    }

    @Override
    public void dispose() {
        if (!isDisposed) {
            isDisposed = true;
            view.dispose();
        }
    }
}
