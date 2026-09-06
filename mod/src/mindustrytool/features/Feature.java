package mindustrytool.features;

import arc.Core;
import arc.Events;

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
}
