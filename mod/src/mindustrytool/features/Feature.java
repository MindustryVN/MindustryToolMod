package mindustrytool.features;

import arc.Core;
import arc.Events;
import arc.scene.ui.Dialog;

public interface Feature {

    FeatureMetadata getMetadata();

    default void enable() {
        if (isEnabled())
            return;

        Core.settings.put(getSettingKey(), true);
        onEnable();
        Events.fire(new FeatureStateChanged(this, true));
    }

    default void disable() {
        if (!isEnabled())
            return;

        Core.settings.put(getSettingKey(), false);
        onDisable();
        Events.fire(new FeatureStateChanged(this, false));
    }

    default void setEnabled(boolean enabled) {
        if (enabled) {
            enable();
        } else {
            disable();
        }
    }

    default void onEnable() {
    }

    default void onDisable() {
    }

    default boolean isEnabled() {
        return Core.settings.getBool(
                getSettingKey(),
                getMetadata().isEnabledByDefault());
    }

    default String getSettingKey() {
        return "mindustrytool.feature." + getMetadata().getId() + ".enabled";
    }

    default Dialog getSettingDialog() {
        return null;
    }

    default Dialog getMainDialog() {
        return null;
    }

    default String getName() {
        String id = getMetadata().getId();
        String nameKey = "feature." + id + ".name";
        if (Core.bundle != null && Core.bundle.has(nameKey)) {
            return Core.bundle.get(nameKey);
        }
        String directKey = "feature." + id;
        if (Core.bundle != null && Core.bundle.has(directKey)) {
            return Core.bundle.get(directKey);
        }
        return id;
    }

    default String getDescription() {
        String id = getMetadata().getId();
        String descKey = "feature." + id + ".description";
        if (Core.bundle != null && Core.bundle.has(descKey)) {
            return Core.bundle.get(descKey);
        }
        return "";
    }

    default String getHelp() {
        String id = getMetadata().getId();
        String helpKey = "feature." + id + ".help";
        if (Core.bundle != null && Core.bundle.has(helpKey)) {
            return Core.bundle.get(helpKey);
        }
        return "";
    }
}
