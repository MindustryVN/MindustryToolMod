package main.gui;

import static mindustry.Vars.state;

import arc.Core;
import arc.graphics.Color;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Align;
import main.data.ItemRequirement;
import main.data.SchematicData;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.type.ItemSeq;
import mindustry.type.ItemStack;
import mindustry.ui.dialogs.BaseDialog;

import static mindustry.Vars.*;

public class SchematicInfoDialog extends BaseDialog {

    public SchematicInfoDialog() {
        super("");

        setFillParent(true);
        addCloseListener();
    }

    public void show(SchematicData schematic) {
        cont.clear();

        title.setText("[[" + Core.bundle.get("schematic") + "] " + schematic.name);
        cont.add(Core.bundle.format("schematic.info", schematic.width, schematic.height,
                schematic.width * schematic.height))
                .color(Color.lightGray).row();

        cont.add(new SchematicImage(schematic.id)).maxSize(800).row();
        cont.table(tags -> buildTags(schematic, tags, false)).fillX().left().row();

        ItemSeq arr = toItemSeq(schematic.requirement);
        cont.table(r -> {
            int i = 0;
            for (ItemStack s : arr) {
                r.image(s.item.uiIcon).left().size(iconMed);
                r.label(() -> {
                    Building core = player.core();
                    if (core == null
                            || state.isMenu()
                            || state.rules.infiniteResources
                            || core.items.has(s.item, s.amount))
                        return "[lightgray]" + s.amount;

                    return (core.items.has(s.item, s.amount) ? "[lightgray]" : "[scarlet]")
                            + Math.min(core.items.get(s.item), s.amount) + "[lightgray]/" + s.amount;
                }).padLeft(2).left().padRight(4);

                if (++i % 4 == 0) {
                    r.row();
                }
            }
        });
        cont.row();
        buttons.clearChildren();
        buttons.defaults().size(Core.graphics.isPortrait() ? 150f : 210f, 64f);
        buttons.button("@back", Icon.left, this::hide);
        // buttons.button("@editor.export", Icon.upload, () -> showExport(schem));
        // buttons.button("@edit", Icon.edit, () -> showEdit(schem));

        show();
    }

    void buildTags(SchematicData schematic, Table container, boolean hasName) {
        container.clearChildren();
        container.left();

        if (hasName)
            container.add("@schematic.tags").padRight(4);

        container.pane(scrollPane -> {
            scrollPane.left();
            scrollPane.defaults().pad(3).height(42);
            for (var tag : schematic.tags)
                scrollPane.table(Tex.button, i -> i.add(tag).padRight(4).height(42).labelAlign(Align.center));

        }).fillX().left().height(42).scrollY(false);
    }

    public ItemSeq toItemSeq(ItemRequirement[] requirement) {
        Seq<ItemStack> seq = new Seq<>();

        for (var req : requirement) {
            seq.add(new ItemStack(
                    Vars.content.items().find(i -> i.name.toLowerCase().equals(req.name.toLowerCase())),
                    req.amount));
        }

        return new ItemSeq(seq);
    }
}
