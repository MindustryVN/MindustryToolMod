package mindustrytool.features;

public interface Feature {

    FeatureMetadata getMetadata();

    default void onEnable() {
    }

    default void onDisable() {
    }
}
