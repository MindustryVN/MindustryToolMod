package mindustrytool.features;

import java.util.Optional;

import arc.input.KeyBind;
import arc.scene.style.Drawable;
import arc.scene.style.TextureRegionDrawable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import old.mindustrytool.Utils;

@Getter
@AllArgsConstructor
public class FeatureMetadata {
    private final String id;
    private final Drawable icon;
    private final int order;
    private final boolean enabledByDefault;
    private final boolean quickAccess;
    private final Optional<KeyBind> keybind;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private Drawable icon = null;
        private int order = 0;
        private boolean enabledByDefault = true;
        private boolean quickAccess = false;
        private KeyBind keybind = null;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder icon(TextureRegionDrawable icon) {
            this.icon = Utils.scalable(icon);
            return this;
        }

        public Builder order(int order) {
            this.order = order;
            return this;
        }

        public Builder enabledByDefault(boolean enabledByDefault) {
            this.enabledByDefault = enabledByDefault;
            return this;
        }

        public Builder quickAccess(boolean quickAccess) {
            this.quickAccess = quickAccess;
            return this;
        }

        public FeatureMetadata build() {
            if (id == null)
                throw new IllegalStateException("ID is required");
            if (icon == null)
                throw new IllegalStateException("Icon is required");

            return new FeatureMetadata(id, icon, order, enabledByDefault, quickAccess, Optional.ofNullable(keybind));
        }
    }
}
