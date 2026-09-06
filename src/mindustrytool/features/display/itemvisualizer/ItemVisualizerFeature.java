package mindustrytool.features.display.itemvisualizer;

import arc.Core;
import arc.Events;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Rect;
import arc.scene.ui.Dialog;
import arc.util.Time;
import mindustry.Vars;
import mindustry.game.EventType.Trigger;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.graphics.Layer;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.world.blocks.distribution.BufferedItemBridge.BufferedItemBridgeBuild;
import mindustry.world.blocks.distribution.ItemBridge.ItemBridgeBuild;
import mindustry.world.blocks.distribution.Router.RouterBuild;
import mindustry.world.blocks.liquid.LiquidBridge.LiquidBridgeBuild;
import mindustry.world.modules.ItemModule;
import mindustrytool.features.Feature;
import mindustrytool.features.FeatureMetadata;

import java.util.Optional;

public class ItemVisualizerFeature implements Feature {
    private boolean active = false;
    private final Rect viewBounds = new Rect();
    private final ItemModule tempItemModule = new ItemModule();

    @Override
    public FeatureMetadata getMetadata() {
        return FeatureMetadata.builder()
                .name("@feature.item-visualizer")
                .description("@feature.item-visualizer.description")
                .icon(Icon.distribution)
                .quickAccess(true)
                .enabledByDefault(false)
                .build();
    }

    @Override
    public void init() {
        ItemVisualizerSettings.load();
        Events.run(Trigger.draw, this::draw);
    }

    @Override
    public void onEnable() {
        active = true;
    }

    @Override
    public void onDisable() {
        active = false;
    }

    @Override
    public Optional<Dialog> setting() {
        return Optional.of(new ItemVisualizerSettingsDialog());
    }

    private void draw() {
        if (!active || !Vars.state.isGame() || Vars.world == null || Vars.indexer == null
                || Vars.ui == null || Vars.ui.hudfrag == null || !Vars.ui.hudfrag.shown) {
            return;
        }

        Core.camera.bounds(viewBounds);
        float z = Draw.z();
        Draw.z(Layer.overlayUI);

        float cx = viewBounds.x + viewBounds.width / 2f;
        float cy = viewBounds.y + viewBounds.height / 2f;
        float range = Math.max(viewBounds.width, viewBounds.height) * 0.75f;

        Vars.indexer.eachBlock(null, cx, cy, range, b -> true, this::renderBuildingOverlay);

        Draw.z(z);
        Draw.reset();
    }

    private void renderBuildingOverlay(Building build) {
        if (build == null || !build.isValid()) return;

        if (ItemVisualizerSettings.showItemBridges) {
            renderItemBridge(build);
        }
        if (ItemVisualizerSettings.showLiquidBridges) {
            renderLiquidBridge(build);
        }
        if (ItemVisualizerSettings.showRouters) {
            renderRouter(build);
        }
    }

    private void renderItemBridge(Building build) {
        int link = -1;
        ItemModule items = null;

        if (build instanceof BufferedItemBridgeBuild buffered) {
            link = buffered.link;
            items = buffered.items;
        } else if (build instanceof ItemBridgeBuild bridge) {
            link = bridge.link;
            items = bridge.items;
        }

        if (link == -1 || items == null) return;
        Building linked = Vars.world.build(link);
        if (linked == null) return;

        renderBridgeFlow(build, linked, items);
    }

    private void renderBridgeFlow(Building from, Building to, ItemModule items) {
        if (items.total() == 0) return;

        tempItemModule.set(items);
        int total = tempItemModule.total();
        if (total <= 0) return;

        int index = total;
        Item item = tempItemModule.take();

        while (item != null && index > 0) {
            drawFlow(from.x, from.y, to.x, to.y, item.uiIcon, (float) index / total);
            index--;
            item = tempItemModule.take();
        }
    }

    private void renderLiquidBridge(Building build) {
        if (build instanceof LiquidBridgeBuild bridge) {
            if (bridge.link == -1 || bridge.liquids == null) return;
            Building linked = Vars.world.build(bridge.link);
            if (linked == null) return;

            Liquid liquid = bridge.liquids.current();
            if (liquid == null || bridge.liquids.currentAmount() <= 0.01f) return;

            float progress = (Time.time % 60f) / 60f;
            float lx = Mathf.lerp(build.x, linked.x, progress);
            float ly = Mathf.lerp(build.y, linked.y, progress);

            Draw.color(liquid.color);
            Draw.rect(liquid.uiIcon, lx, ly, 6f, 6f);
            Draw.color();
        }
    }

    private void renderRouter(Building build) {
        if (build instanceof RouterBuild router) {
            if (router.items != null && router.items.total() > 0) {
                Item item = router.items.first();
                if (item != null && item.uiIcon != null) {
                    Draw.color();
                    Draw.rect(item.uiIcon, router.x, router.y, 6f, 6f);
                }
            }
        }
    }

    private void drawFlow(float x1, float y1, float x2, float y2, TextureRegion icon, float progress) {
        float lx = Mathf.lerp(x1, x2, progress * 0.9f);
        float ly = Mathf.lerp(y1, y2, progress * 0.9f);

        Draw.color();
        Draw.rect(icon, lx, ly, 5f, 5f);
    }
}
