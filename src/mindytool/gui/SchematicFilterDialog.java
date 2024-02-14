package mindytool.gui;

import mindytool.config.Config;
import mindytool.data.SearchConfig;
import mindytool.data.Tag;

import arc.Core;
import arc.scene.ui.ButtonGroup;
import arc.scene.ui.TextButton.TextButtonStyle;
import mindustry.gen.Icon;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

public class SchematicFilterDialog extends BaseDialog {
    private TextButtonStyle style = Styles.flatTogglet;

    public SchematicFilterDialog() {
        super("");

        setFillParent(true);
        addCloseListener();
    }

    public void show(SearchConfig searchConfig) {
        Tag.onUpdate(() -> show(searchConfig));

        cont.clear();
        cont.pane(table -> {
            table.defaults().minSize(200, 50);
            table.table(Styles.black, text -> text.add(Core.bundle.format("messages.sort")).left());

            var buttonGroup = new ButtonGroup<>();
            table.pane(valueTable -> {
                valueTable.defaults().size(200, 50);
                for (var sort : Config.sorts) {
                    valueTable
                            .button(Core.bundle.format("tags.values." + sort.getName()), style,
                                    () -> searchConfig.setSort(sort))///
                            .group(buttonGroup)
                            .checked(sort.equals(searchConfig.getSort()));
                }
            })
                    .top()
                    .left()
                    .scrollY(false);

            table.row();

            Tag.schematic(schematicTags -> {
                for (var tag : schematicTags) {
                    table.table(Styles.black, text -> text.add(Core.bundle.format("tags.categories." + tag.name)))
                            .top();
                    table.pane(valueTable -> {
                        valueTable.defaults().size(200, 50);
                        for (int i = 0; i < tag.value.length; i++) {
                            var value = tag.value[i];
                            valueTable
                                    .button(Core.bundle.format("tags.values." + value), style,
                                            () -> searchConfig.setTag(tag.name + "_" + value))
                                    .checked(searchConfig.containTag(tag.name + "_" + value));

                            if (i > 0 && i % 8 == 0) {
                                valueTable.row();
                            }
                        }
                    })
                            .top()
                            .left()
                            .scrollY(false);
                    table.row();
                }
            });

            table.top();

        })
                .padLeft(20)
                .padRight(20)
                .scrollY(true)
                .expand()
                .fill()
                .left()
                .top();

        cont.row();
        buttons.clearChildren();
        buttons.defaults().size(Core.graphics.isPortrait() ? 150f : 210f, 64f);
        buttons.button("@back", Icon.left, this::hide);

        show();
    }

}
