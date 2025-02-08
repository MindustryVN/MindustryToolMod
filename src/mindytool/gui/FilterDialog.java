package mindytool.gui;

import mindytool.config.Config;
import mindytool.data.SearchConfig;
import mindytool.data.TagData;
import mindytool.data.TagService;
import arc.Core;
import arc.func.Cons;
import arc.graphics.Color;
import arc.scene.ui.ButtonGroup;
import arc.scene.ui.TextButton.TextButtonStyle;
import arc.struct.Seq;
import mindustry.gen.Icon;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

public class FilterDialog extends BaseDialog {
    private TextButtonStyle style = Styles.flatTogglet;
    private final Cons<Cons<Seq<TagData>>> tagConsumer;

    public FilterDialog(Cons<Cons<Seq<TagData>>> tagConsumer) {
        super("");

        this.tagConsumer = tagConsumer;
        setFillParent(true);
        addCloseListener();
    }

    public void show(SearchConfig searchConfig) {
        TagService.onUpdate(() -> show(searchConfig));

        cont.clear();
        cont.setColor(Color.black);
        cont.pane(table -> {
            table.defaults().minSize(200, 50);
            table.table(Styles.grayPanel, text -> text.add(Core.bundle.format("messages.sort")).left());

            var buttonGroup = new ButtonGroup<>();
            table.pane(valueTable -> {
                valueTable.defaults().size(300, 50);
                for (var sort : Config.sorts) {
                    valueTable.button(Core.bundle.format("tags.values." + sort.getName()), style, () -> searchConfig.setSort(sort))///
                            .group(buttonGroup).checked(sort.equals(searchConfig.getSort()));
                }
            }).top().left().scrollY(false).marginBottom(24).padBottom(24);

            table.row();

            tagConsumer.get(schematicTags -> {
                for (var tag : schematicTags) {
                    table.table(Styles.grayPanel, text -> text.add(Core.bundle.format("tags.categories." + tag.name()))).top();
                    table.pane(valueTable -> {
                        valueTable.defaults().height(50);
                        for (int i = 0; i < tag.values().size; i++) {
                            var value = tag.values().get(i);

                            valueTable.button(btn -> {
                                btn.left();
                                if (value.icon() != null && !value.icon().isBlank()) {
                                    btn.add(new NetworkImage(value.icon()))//
                                            .size(24)//
                                            .padRight(4)//
                                            .marginRight(4);
                                }
                                btn.add(Core.bundle.format("tags.values." + value.name()));
                            }, style, () -> searchConfig.setTag(tag.name() + "_" + value.name()))//
                                    .checked(searchConfig.containTag(tag.name() + "_" + value.name()))//
                                    .padRight(4)//
                                    .padBottom(4)//
                                    .left()//
                                    .fillX()//
                                    .margin(12);

                            if (i > 0 && i % 6 == 0) {
                                valueTable.row();
                            }
                        }
                    }).top().left().scrollY(false).marginBottom(24).padBottom(24);
                    table.row();
                }
            });

            table.top();

        }).padLeft(20).padRight(20).scrollY(true).expand().fill().left().top();

        cont.row();
        buttons.clearChildren();
        buttons.defaults().size(Core.graphics.isPortrait() ? 150f : 210f, 64f);
        buttons.button("@back", Icon.left, this::hide);

        show();
    }

}
