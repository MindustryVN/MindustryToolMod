package mindustrytool.features;

import java.util.Optional;

import arc.Core;
import arc.scene.ui.Dialog;

public interface Feature {
    FeatureMetadata getMetadata();

    default void init() {
    };

    default void onEnable() {
    };

    default void onDisable() {
    };

    default void onEnableChange(boolean enabled) {

    }

    default Optional<Dialog> setting() {
        return Optional.empty();
    }

    default Optional<Dialog> dialog() {
        return Optional.empty();
    }

    default String getSettingKey() {
        String n = getMetadata().name();
        if (n.startsWith("@")) n = n.substring(1);
        if (n.startsWith("feature.")) n = n.substring(8);
        return "mindustrytool." + n + ".enabled";
    }

    default boolean isEnabled() {
        var metadata = getMetadata();

        String key = getSettingKey();
        if (Core.settings.has(key)) {
            return Core.settings.getBool(key, metadata.enabledByDefault());
        }
        if (Core.settings.has("mindustrytool." + metadata.name() + ".enabled")) {
            return Core.settings.getBool("mindustrytool." + metadata.name() + ".enabled", metadata.enabledByDefault());
        }
        return metadata.enabledByDefault();
    }
}
