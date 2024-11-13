package mindytool.gui;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.Texture.TextureFilter;
import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.Image;
import arc.struct.ObjectMap;
import arc.util.Http;
import arc.util.Log;
import arc.util.Scaling;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindytool.config.Config;
import mindytool.data.MapData;

public class MapImage extends Image {
    public float scaling = 16f;
    public float thickness = 4f;
    public Color borderColor = Pal.gray;

    private MapData mapData;
    private TextureRegion lastTexture;

    private static ObjectMap<String, TextureRegion> textureCache = new ObjectMap<>();

    public MapImage(MapData mapData) {
        super(Tex.clear);
        setScaling(Scaling.fit);
        this.mapData = mapData;
    }

    @Override
    public void draw() {
        try {

            super.draw();

            // textures are only requested when the rendering happens; this assists with
            // culling
            if (!textureCache.containsKey(mapData.id)) {
                textureCache.put(mapData.id, lastTexture = Core.atlas.find("nomap"));
                Http.get(Config.IMAGE_URL + "maps/" + mapData.id + ".webp?format=jpeg", res -> {
                    Pixmap pix = new Pixmap(res.getResult());
                    Core.app.post(() -> {
                        try {
                            var tex = new Texture(pix);
                            tex.setFilter(TextureFilter.linear);
                            textureCache.put(mapData.id, new TextureRegion(tex));
                        } catch (Exception e) {
                            Log.err(e);
                        }
                    });
                    pix.dispose();
                }, err -> {
                    Log.err(err);
                });
            }

            var next = textureCache.get(mapData.id);
            if (lastTexture != next) {
                lastTexture = next;
                setDrawable(next);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
