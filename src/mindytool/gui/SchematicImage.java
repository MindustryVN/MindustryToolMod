package mindytool.gui;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.Texture;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Button;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Scl;
import arc.util.Scaling;
import mindustry.game.Schematic;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;

import static mindustry.Vars.*;

public class SchematicImage extends Image {
    public float scaling = 16f;
    public float thickness = 4f;
    public Color borderColor = Pal.gray;

    private Schematic schematic;
    private Texture lastTexture;
    boolean set;

    public SchematicImage(Schematic s) {
        super(Tex.clear);
        setScaling(Scaling.fit);
        schematic = s;

        if (schematics.hasPreview(s)) {
            setPreview();
            set = true;
        }
    }

    @Override
    public void draw() {
        boolean checked = parent.parent instanceof Button
                && ((Button) parent.parent).isOver();

        boolean wasSet = set;
        if (!set) {
            Core.app.post(this::setPreview);
            set = true;
        } else if (lastTexture != null && lastTexture.isDisposed()) {
            set = wasSet = false;
        }

        Texture background = Core.assets.get("sprites/schematic-background.png", Texture.class);
        TextureRegion region = Draw.wrap(background);
        float xr = width / scaling;
        float yr = height / scaling;
        region.setU2(xr);
        region.setV2(yr);
        Draw.color();
        Draw.alpha(parentAlpha);
        Draw.rect(region, x + width / 2f, y + height / 2f, width, height);

        if (wasSet) {
            super.draw();
        } else {
            Draw.rect(Icon.refresh.getRegion(), x + width / 2f, y + height / 2f, width / 4f, height / 4f);
        }

        Draw.color(checked ? Pal.accent : borderColor);
        Draw.alpha(parentAlpha);
        Lines.stroke(Scl.scl(thickness));
        Lines.rect(x, y, width, height);
        Draw.reset();
    }

    private void setPreview() {
        TextureRegionDrawable draw = new TextureRegionDrawable(
                new TextureRegion(lastTexture = schematics.getPreview(schematic)));
        setDrawable(draw);
        setScaling(Scaling.fit);
    }
}
