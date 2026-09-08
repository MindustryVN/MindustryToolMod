package mindustrytool.features.teamresource;

import arc.Core;
import arc.scene.style.Drawable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Dialog;
import arc.util.Nullable;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustrytool.components.FileIcon;
import mindustrytool.features.Feature;
import mindustrytool.features.FeatureMetadata;

/**
 * Feature responsible for registering and managing the Team Resource Tracker overlay.
 */
public class TeamResourceFeature extends Feature {
    private @Nullable TeamResourceState state;
    private @Nullable TeamResourceOverlay overlay;

    public TeamResourceFeature() {
        super(FeatureMetadata.builder()
                .id("team-resources")
                .icon(getFeatureIcon())
                .order(0)
                .enabledByDefault(true)
                .quickAccess(true)
                .build());
    }

    private static Drawable getFeatureIcon() {
        try {
            return FileIcon.of("team-resources.png");
        } catch (Throwable t) {
            return Icon.layers != null ? Icon.layers : new TextureRegionDrawable();
        }
    }

    @Override
    public void onEnable() {
        if (state == null) {
            state = new TeamResourceState();
        }
        if (overlay == null) {
            overlay = new TeamResourceOverlay(state);
        }

        overlay.remove();
        Vars.ui.hudGroup.addChild(overlay);
        overlay.setPosition(TeamResourceConfig.x(), TeamResourceConfig.y());
        Core.settings.put("coreitems", false);
        Core.app.post(() -> {
            if (overlay != null) {
                overlay.rebuild();
            }
        });
    }

    @Override
    public void onDisable() {
        if (overlay != null) {
            overlay.remove();
        }
        Core.settings.put("coreitems", true);
    }

    @Override
    public @Nullable Dialog getSettingDialog() {
        if (state == null) {
            state = new TeamResourceState();
        }
        if (overlay == null) {
            overlay = new TeamResourceOverlay(state);
        }
        return overlay.getSettingDialog();
    }

    public @Nullable TeamResourceState getState() {
        return state;
    }

    public @Nullable TeamResourceOverlay getOverlay() {
        return overlay;
    }
}
