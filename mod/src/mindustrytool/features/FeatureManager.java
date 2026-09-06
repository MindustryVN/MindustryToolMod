package mindustrytool.features;

import arc.Core;
import arc.struct.Seq;

public class FeatureManager {
    private static final Seq<Feature> features = new Seq<>();

    public static void reenable() {
        @SuppressWarnings("unchecked")
        Seq<String> enableds = Core.settings.getJson("mindustrytool.enabled-features", Seq.class, String.class,
                Seq::new);

        for (Feature feature : features) {
            if (enableds.contains(feature.getMetadata().getId())) {
                feature.enable();
            }
        }
    }

    public static void disableAll() {
        Seq<String> enableds = getEnableds().map(f -> f.getMetadata().getId());

        Core.settings.putJson("mindustrytool.enabled-features", String.class, enableds);

        for (Feature feature : features) {
            feature.disable();
        }
    }

    public static void register(Feature... feature) {
        features.addAll(feature);
        features.sort((a, b) -> Integer.compare(a.getMetadata().getOrder(), b.getMetadata().getOrder()));
    }

    public static <T extends Feature> T getFeature(Class<T> featureClass) {
        return featureClass.cast(features.find(f -> f.getClass() == featureClass));
    }

    public static void init() {
        for (Feature feature : features) {
            if (feature.isEnabled()) {
                feature.onEnable();
            }
        }
    }

    public static Seq<Feature> getFeatures() {
        return features;
    }

    public static <T extends Feature> T get(Class<T> featureClass) {
        var feature = features.find(f -> f.getClass().equals(featureClass));
        if (feature == null) {
            throw new IllegalArgumentException("Feature not found: " + featureClass);
        }
        return featureClass.cast(feature);
    }

    public static Seq<Feature> getEnableds() {
        return features.select(f -> f.isEnabled());
    }
}
