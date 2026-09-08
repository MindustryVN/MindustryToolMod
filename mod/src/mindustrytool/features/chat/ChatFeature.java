package mindustrytool.features.chat;

import arc.Core;
import arc.scene.Element;
import arc.scene.ui.Dialog;
import arc.util.Nullable;
import mindustry.gen.Icon;
import mindustrytool.config.ConfigGroup;
import mindustrytool.config.ConfigValue;
import mindustrytool.features.Feature;
import mindustrytool.features.FeatureMetadata;
import solim.signal.Signal;

public class ChatFeature extends Feature {

    public final ConfigGroup config;
    public final ConfigValue<Float> opacityConfig;
    public final ConfigValue<Float> scaleConfig;
    public final ConfigValue<Float> widthRatioConfig;
    public final ConfigValue<Float> heightRatioConfig;
    public final ConfigValue<Boolean> collapsedConfig;

    public final ConfigValue<Float> xConfig;
    public final ConfigValue<Float> yConfig;

    public final Signal<Float> xSignal;
    public final Signal<Float> ySignal;

    private final ChatStore store;
    private final ChatService service;

    private @Nullable ChatOverlayHudView hudView;
    private @Nullable ChatSettingsDialog settingsDialog;

    public ChatFeature() {
        super(FeatureMetadata.builder()
                .id("chat")
                .icon(Icon.chat)
                .order(20)
                .enabledByDefault(true)
                .quickAccess(true)
                .build());

        config = ConfigGroup.of(getMetadata());

        opacityConfig = config.floatValue("opacity", 1.0f);
        scaleConfig = config.floatValue("scale", 1.0f);
        widthRatioConfig = config.floatValue("width-ratio", 0.6f);
        heightRatioConfig = config.floatValue("height-ratio", 0.6f);
        collapsedConfig = config.boolValue("collapsed", false);

        float defX = 40f;
        float defY = Core.graphics != null ? Core.graphics.getHeight() / 2f : 300f;

        xConfig = config.floatValue("x", defX);
        yConfig = config.floatValue("y", defY);

        xSignal = xConfig.signal();
        ySignal = yConfig.signal();

        store = new ChatStore();
        service = new ChatService(store, () -> !Boolean.TRUE.equals(collapsedConfig.get()));
    }

    public ChatStore getStore() {
        return store;
    }

    public ChatService getService() {
        return service;
    }

    public void resetPosition() {
        float defX = 40f;
        float defY = Core.graphics != null ? Core.graphics.getHeight() / 2f : 300f;
        xConfig.set(defX);
        yConfig.set(defY);
    }

    public void resetAppearance() {
        opacityConfig.reset();
        scaleConfig.reset();
        widthRatioConfig.reset();
        heightRatioConfig.reset();
    }

    @Override
    public void onEnable() {
        service.start();

        if (Core.scene != null) {
            if (hudView != null) {
                hudView.element().remove();
                hudView.dispose();
            }

            hudView = new ChatOverlayHudView(this);
            Element el = hudView.element();
            el.name = "chat-overlay-hud";

            Core.app.post(() -> {
                if (hudView != null && Core.scene != null) {
                    Core.scene.add(el);
                    el.toFront();
                }
            });
        }
    }

    @Override
    public void onDisable() {
        service.stop();

        if (hudView != null) {
            hudView.element().remove();
            hudView.dispose();
            hudView = null;
        }
    }

    @Override
    public @Nullable Dialog getSettingDialog() {
        if (settingsDialog == null) {
            settingsDialog = new ChatSettingsDialog(this);
        }
        return settingsDialog;
    }
}
