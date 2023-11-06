package main.gui;

import arc.Core;
import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.Texture.TextureFilter;
import arc.graphics.g2d.TextureRegion;
import arc.struct.ObjectMap;
import arc.util.Http;
import arc.util.Log;
import arc.util.Scaling;
import arc.util.Http.HttpResponse;
import main.config.Config;
import mindustry.gen.Tex;
import mindustry.ui.BorderImage;

public class SchematicImage extends BorderImage {

    public float scaling = 16f;

    public final String schematicId;

    private static ObjectMap<String, TextureRegion> schematicImageCache = new ObjectMap<>();
    private TextureRegion lastImage;

    public SchematicImage(String schematicId) {
        this.schematicId = schematicId;

        setScaling(Scaling.fit);
        setDrawable(Tex.nomap);
    }

    @Override
    public void draw() {
        super.draw();

        var schematicImage = schematicImageCache.get(schematicId);
        if (schematicImage == null) {
            schematicImageCache.put(schematicId, Core.atlas.find("nomap"));
            Http.get(Config.API_URL + String.format("schematic/%s/image", schematicId))//
                    .timeout(120000)//
                    .error(error -> Log.err(error))
                    .submit(this::handleSchematicImageResult);
        }

        var currentImage = schematicImageCache.get(schematicId);
        if (lastImage != currentImage) {
            lastImage = currentImage;
            setDrawable(currentImage);
        }
    }

    private void handleSchematicImageResult(HttpResponse imageResult) {
        Pixmap pix = new Pixmap(imageResult.getResult());
        Core.app.post(() -> {
            var texture = new Texture(pix);
            texture.setFilter(TextureFilter.linear);
            schematicImageCache.put(schematicId, new TextureRegion(texture));
            pix.dispose();
        });

    }
}
