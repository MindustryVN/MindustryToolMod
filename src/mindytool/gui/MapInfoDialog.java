package mindytool.gui;

import mindytool.data.MapDetailData;

import java.security.InvalidParameterException;

import arc.Core;
import arc.graphics.Color;
import arc.scene.ui.layout.Table;
import arc.util.Align;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.ui.dialogs.BaseDialog;

public class MapInfoDialog extends BaseDialog {

    public MapInfoDialog() {
        super("");

        setFillParent(true);
        addCloseListener();
    }

    public void show(MapDetailData data) {
        if (data == null) {
            throw new InvalidParameterException("Map can not be null");
        }
        cont.clear();

        title.setText("[[" + Core.bundle.get("map") + "] " + data.name());
        cont.add(Core.bundle.format("message.like", data.likes())).color(Color.lightGray).row();
        cont.add(new MapImage(data.id())).maxSize(800).row();
        cont.table(tags -> buildTags(data, tags, false)).fillX().left().row();

        cont.row();
        buttons.clearChildren();
        buttons.defaults().size(Core.graphics.isPortrait() ? 150f : 210f, 64f);
        buttons.button("@back", Icon.left, this::hide);
        // buttons.button("@editor.export", Icon.upload, () -> showExport(schem));
        // buttons.button("@edit", Icon.edit, () -> showEdit(schem));

        show();
    }

    void buildTags(MapDetailData map, Table container, boolean hasName) {
        container.clearChildren();
        container.left();

        if (map.tags() == null) {
            return;
        }

        if (hasName)
            container.add("@map.tags").padRight(4);

        container.pane(scrollPane -> {
            scrollPane.left();
            scrollPane.defaults().pad(3).height(42);

            for (var tag : map.tags())
                scrollPane.table(Tex.button, i -> i.add(tag.name())//
                        .padRight(4)//
                        .height(42)//
                        .labelAlign(Align.center));

        })//
                .fillX()//
                .left()//
                .height(42)//
                .scrollY(false);
    }
}
