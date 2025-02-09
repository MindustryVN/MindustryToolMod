package mindytool.gui;

import mindytool.config.Config;
import mindytool.data.SearchConfig;
import mindytool.data.TagData;
import mindytool.data.TagService;
import arc.Core;
import arc.func.Cons;
import arc.scene.ui.ButtonGroup;
import arc.scene.ui.TextButton.TextButtonStyle;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Align;
import mindustry.gen.Icon;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

public class FilterDialog extends BaseDialog {
    private TextButtonStyle style = Styles.togglet;
    private final Cons<Cons<Seq<TagData>>> tagProvider;

    public FilterDialog(Cons<Cons<Seq<TagData>>> tagProvider) {
        super("");

        setFillParent(true);
        addCloseListener();

        this.tagProvider = tagProvider;
    }

    public void show(SearchConfig searchConfig) {
        TagService.onUpdate(() -> show(searchConfig));
        cont.clear();
        cont.pane(table -> {
            table.defaults().minSize(200, 50);
            SortSelector(table, searchConfig);
            table.row();

            tagProvider.get(schematicTags -> {
                for (var tag : schematicTags) {
                    TagSelector(table, searchConfig, tag);
                    table.row();
                }
            });

            table.top();

        })//
                .padLeft(20)//
                .padRight(20)//
                .scrollY(true)//
                .expand()//
                .fill()//
                .left()//
                .top();

        cont.row();
        buttons.clearChildren();
        buttons.defaults().size(Core.graphics.isPortrait() ? 150f : 210f, 64f);
        buttons.button("@back", Icon.left, this::hide);

        show();
    }

    public void SortSelector(Table table, SearchConfig searchConfig) {
        var buttonGroup = new ButtonGroup<>();

        table.table(Styles.flatOver, text -> text.add(Core.bundle.format("messages.sort")).left().labelAlign(Align.left))//
                .top()//
                .left().padBottom(4);

        table.row();
        table.pane(card -> {
            card.defaults().size(300, 50);
            for (var sort : Config.sorts) {
                card.button(formatTag(sort.getName()), style, () -> searchConfig.setSort(sort))//
                        .group(buttonGroup)//
                        .checked(sort.equals(searchConfig.getSort()))//
                        .padRight(4)//
                        .padBottom(4);
            }
        })//
                .top()//
                .left()//
                .scrollY(false)//
                .marginBottom(48)//
                .padBottom(48);
    }

    public void TagSelector(Table table, SearchConfig searchConfig, TagData tag) {
        table.table(Styles.flatOver, text -> text.add(Core.bundle.format("tags.categories." + tag.name())).left().labelAlign(Align.left))//
                .top()//
                .left()//
                .padBottom(4);

        table.row();

        table.pane(card -> {
            card.defaults().height(50);

            for (int i = 0; i < tag.values().size; i++) {
                var value = tag.values().get(i);

                card.button(btn -> {
                    btn.left();
                    if (value.icon() != null && !value.icon().isBlank()) {
                        btn.add(new NetworkImage(value.icon()))//
                                .size(48)//
                                .padRight(4)//
                                .marginRight(4);
                    }
                    btn.add(formatTag(value.name()));
                }, style, () -> searchConfig.setTag(tag, value))//
                        .checked(searchConfig.containTag(tag, value))//
                        .padRight(4)//
                        .padBottom(4)//
                        .left()//
                        .fillX()//
                        .margin(12);

                if (i % 5 == 4) {
                    card.row();
                }
            }
        })//
                .growY()//
                .wrap()//
                .top()//
                .left()//
                .scrollX(true)//
                .scrollY(false)//
                .marginBottom(48)//
                .padBottom(48);
    }

    private String formatTag(String name) {
        return Core.bundle.format("tags.values." + name);
    }

}
