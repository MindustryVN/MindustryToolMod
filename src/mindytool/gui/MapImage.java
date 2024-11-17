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
import arc.util.Http.HttpStatus;
import arc.util.Http.HttpStatusException;
import arc.util.Log;
import arc.util.Scaling;
import mindustry.Vars;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindytool.Main;
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
        super.draw();

        // textures are only requested when the rendering happens; this assists with
        // culling
        if (!textureCache.containsKey(mapData.id)) {
            textureCache.put(mapData.id, lastTexture = Core.atlas.find("nomap"));

            var file = Main.mapsDir.child(mapData.id + ".jepg");

            if (file.exists()) {
                try {
                    byte[] result = file.readBytes();
                    Pixmap pix = new Pixmap(result);
                    Core.app.post(() -> {
                        try {
                            var tex = new Texture(pix);
                            tex.setFilter(TextureFilter.linear);
                            textureCache.put(mapData.id, new TextureRegion(tex));
                            pix.dispose();
                        } catch (Exception e) {
                            Log.err(mapData.name, e);
                        }
                    });
                } catch (Exception error) {
                    Log.info(new String(file.readBytes()));
                    Log.err(mapData.name, error);
                }
            } else {

                Http.get(Config.IMAGE_URL + "map-previews/" + mapData.id + ".webp?format=jpeg", res -> {
                    byte[] result = res.getResult();
                    try {
                        if (result.length == 0)
                            return;

                        Pixmap pix = new Pixmap(result);
                        Vars.mainExecutor.execute(() -> {
                            try {
                                file.writeBytes(result);

                            } catch (Exception error) {
                                Log.err(mapData.name, error);
                            }
                        });

                        Core.app.post(() -> {
                            try {
                                var tex = new Texture(pix);
                                tex.setFilter(TextureFilter.linear);
                                textureCache.put(mapData.id, new TextureRegion(tex));
                                pix.dispose();
                            } catch (Exception e) {
                                Log.err(mapData.name, e);
                            }
                        });
                    } catch (Exception error) {
                        Log.info(new String(result));
                        Log.err(mapData.name, error);
                    }
                }, error -> {
                    if (!(error instanceof HttpStatusException requestError)
                            || requestError.status != HttpStatus.NOT_FOUND) {
                        Log.err(mapData.name, error);
                    }
                });
            }
        }

        var next = textureCache.get(mapData.id);
        if (lastTexture != next) {
            lastTexture = next;
            setDrawable(next);
        }
    }

}
