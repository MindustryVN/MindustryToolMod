package mindustrytool.features;

import arc.Core;
import arc.Events;
import arc.scene.ui.Dialog;
import solim.signal.Signal;

public abstract class Feature {

    private FeatureMetadata metadata;
    private Signal<Boolean> enabled;

    protected Feature() {
    }

    protected Feature(FeatureMetadata metadata) {
        this.metadata = metadata;
    }

    public FeatureMetadata getMetadata() {
        return metadata;
    }

    public Signal<Boolean> enabled() {
        if (enabled == null) {
            FeatureMetadata meta = getMetadata();
            boolean defaultVal = meta != null && meta.isEnabledByDefault();
            enabled = Signal.of(Core.settings.getBool(getSettingKey(), defaultVal));
        }
        return enabled;
    }

    public boolean isEnabled() {
        return Boolean.TRUE.equals(enabled().get());
    }

    public void setEnabled(boolean enabled) {
        if (enabled) {
            enable();
        } else {
            disable();
        }
    }

    public void enable() {
        if (isEnabled()) {
            return;
        }

        Core.settings.put(getSettingKey(), true);
        enabled().set(true);
        onEnable();
        Events.fire(new FeatureStateChanged(this, true));
    }

    public void disable() {
        if (!isEnabled()) {
            return;
        }

        Core.settings.put(getSettingKey(), false);
        enabled().set(false);
        onDisable();
        Events.fire(new FeatureStateChanged(this, false));
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public String getSettingKey() {
        return "mindustrytool.feature." + getMetadata().getId() + ".enabled";
    }

    public Dialog getSettingDialog() {
        return null;
    }

    public Dialog getMainDialog() {
        return null;
    }

    public String getName() {
        String id = getMetadata().getId();
        String nameKey = "feature." + id + ".name";
        if (Core.bundle.has(nameKey)) {
            return Core.bundle.get(nameKey);
        }
        String directKey = "feature." + id;
        if (Core.bundle.has(directKey)) {
            return Core.bundle.get(directKey);
        }
        return id;
    }

    public String getDescription() {
        String id = getMetadata().getId();
        String descKey = "feature." + id + ".description";
        if (Core.bundle.has(descKey)) {
            return Core.bundle.get(descKey);
        }
        return "";
    }

    public String getHelp() {
        String id = getMetadata().getId();
        String helpKey = "feature." + id + ".help";
        if (Core.bundle.has(helpKey)) {
            return Core.bundle.get(helpKey);
        }
        return "";
    }
}
