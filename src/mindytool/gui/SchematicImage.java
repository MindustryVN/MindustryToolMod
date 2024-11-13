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
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindytool.config.Config;
import mindytool.data.SchematicData;

public class SchematicImage extends Image {
    public float scaling = 16f;
    public float thickness = 4f;
    public Color borderColor = Pal.gray;

    private SchematicData schematicData;
    private TextureRegion lastTexture;

    private static ObjectMap<String, TextureRegion> textureCache = new ObjectMap<>();

    public SchematicImage(SchematicData schematicData) {
        super(Tex.clear);
        setScaling(Scaling.fit);
        this.schematicData = schematicData;
    }

    @Override
    public void draw() {
        super.draw();

        // textures are only requested when the rendering happens; this assists with
        // culling
        if (!textureCache.containsKey(schematicData.id)) {
            textureCache.put(schematicData.id, lastTexture = Core.atlas.find("nomap"));

            Http.get(Config.IMAGE_URL + "schematic-previews/" + schematicData.id + ".webp?format=jpeg", res -> {
                try {
                    Pixmap pix = new Pixmap(res.getResult());
                    Core.app.post(() -> {
                        try {
                            var tex = new Texture(pix);
                            tex.setFilter(TextureFilter.linear);
                            textureCache.put(schematicData.id, new TextureRegion(tex));
                        } catch (Exception e) {
                            Log.err(e);
                        }
                    });
                    pix.dispose();
                } catch (Exception e) {
                    Log.err(e);
                }
            }, error -> {
                if (!(error instanceof HttpStatusException requestError)
                        || requestError.status != HttpStatus.NOT_FOUND) {
                    Log.err(error);
                }
            });
        }

        var next = textureCache.get(schematicData.id);
        if (lastTexture != next) {
            lastTexture = next;
            setDrawable(next);
        }
    }
}
