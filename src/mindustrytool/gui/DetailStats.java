package mindustrytool.gui;

import arc.scene.ui.layout.Table;
import mindustry.gen.Icon;

public class DetailStats {
    public static void draw(Table table, Long likes, Long dislikes, Long downloadCount) {
        table.left();
        table.image(Icon.upOpenSmall).padLeft(2).padRight(2);
        table.add(" " + likes.toString() + " ").marginLeft(2);
        table.image(Icon.downOpenSmall).padLeft(2).padRight(2);
        table.add(" " + dislikes.toString() + " ").marginLeft(2);
        table.image(Icon.downloadSmall).padLeft(2).padRight(2);
        table.add(" " + downloadCount.toString() + " ").marginLeft(2);
    }
}
