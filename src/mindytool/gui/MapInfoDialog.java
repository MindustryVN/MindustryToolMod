package mindytool.gui;

import mindytool.data.MapDetailData;

import java.security.InvalidParameterException;

import arc.Core;
import arc.graphics.Color;
import mindustry.gen.Icon;
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
        cont.table(card -> {
            card.left();
            card.add("@author").marginRight(4).padRight(4);
            UserCard.draw(card, data.userId());
        }).fillX().left();
        cont.row();
        cont.table(stats -> DetailStats.draw(stats, data.likes(), data.dislikes(), data.downloadCount())).fillX().left();
        cont.row();
        cont.table(container -> TagContainer.draw(container, data.tags())).fillX().left().row();
        cont.row();
        cont.add(data.description()).left();
        buttons.clearChildren();
        buttons.defaults().size(Core.graphics.isPortrait() ? 150f : 210f, 64f);
        buttons.button("@back", Icon.left, this::hide);
        // buttons.button("@editor.export", Icon.upload, () -> showExport(schem));
        // buttons.button("@edit", Icon.edit, () -> showEdit(schem));

        show();
    }
}
