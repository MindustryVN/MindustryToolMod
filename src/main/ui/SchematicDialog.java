package main.ui;

import arc.Core;
import arc.func.Cons;
import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.Texture.TextureFilter;
import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.layout.Scl;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Http;
import arc.util.Log;
import arc.util.Nullable;
import mindustry.gen.Tex;
import mindustry.io.JsonIO;
import mindustry.ui.BorderImage;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

public class SchematicDialog extends BaseDialog {

    private @Nullable Seq<SchematicListing> schematicList;

    private ObjectMap<String, TextureRegion> schematicImageCache = new ObjectMap<>();

    public SchematicDialog() {
        super("@schematic.browser");
        rebuildBrowser();
    }

    public void rebuildBrowser() {
        clear();

        table(con -> {
            button("Reload", () -> rebuildBrowser());
        }).bottom();

        addCloseButton();
        getSchematicList(schematics -> {
            clear();
            addCloseButton();

            for (SchematicListing schematic : schematics) {
                button(con -> {
                    con.margin(0);
                    con.left();
                    con.add(getSchematicImage(schematic));
                }, Styles.flatBordert, () -> {

                }).size(64f).pad(4f * 2f);

            }
        });

    }

    public BorderImage getSchematicImage(SchematicListing schematic) {
        return new BorderImage() {
            TextureRegion last;

            {
                setDrawable(Tex.nomap);
                pad = Scl.scl(4f);
            }

            @Override
            public void draw() {
                super.draw();
                try {
                    if (!schematicImageCache.containsKey(schematic.id)) {
                        schematicImageCache.put(schematic.id, Core.atlas.find("nomap"));
                        Http.get(String.format(
                                "https://mindustry-tool-backend.onrender.com/api/v2/schematic/%s/image",
                                schematic.id))//
                                .timeout(120000)//
                                .submit(res -> {
                                    Pixmap pix = new Pixmap(res.getResultAsString());
                                    var tex = new Texture(pix);
                                    tex.setFilter(TextureFilter.linear);
                                    schematicImageCache.put(schematic.id, new TextureRegion(tex));
                                    pix.dispose();
                                });
                    }
                    var next = schematicImageCache.get(schematic.id);
                    if (last != next) {
                        last = next;
                        setDrawable(next);
                    }
                }

                catch (Exception e) {
                    Log.err(e);
                }
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static void getSchematicList(Cons<Seq<SchematicListing>> listener) {
        try {
            Log.info("Load schematics");
            Http.get("https://mindustry-tool-backend.onrender.com/api/v2/schematic?tags=&sort=time:1&page=0&items=20")
                    .timeout(1200000)
                    .submit(response -> {
                        String data = response.getResultAsString();
                        Core.app.post(() -> {
                            var schematicList = JsonIO.json.fromJson(Seq.class, SchematicListing.class, data);
                            Log.info("Loaded " + schematicList.size + " schematics");
                            listener.get(schematicList);
                        });
                    });
        } catch (Exception e) {
            Log.err(e);
        }
    }
}
