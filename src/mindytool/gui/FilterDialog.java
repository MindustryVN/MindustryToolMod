package mindytool.gui;

import mindytool.config.Config;
import mindytool.data.ModData;
import mindytool.data.ModService;
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
import arc.util.Log;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

public class FilterDialog extends BaseDialog {
    private TextButtonStyle style = Styles.togglet;
    private final Cons<Cons<Seq<TagData>>> tagProvider;
    private float scale = 1;
    private int cols = 1;
    private int cardSize = 0;
    private final int CARD_GAP = 4;
    private String modId;

    public FilterDialog(SearchConfig searchConfig, Cons<Cons<Seq<TagData>>> tagProvider) {
        super("");

        setFillParent(true);
        addCloseListener();

        this.tagProvider = tagProvider;

        onResize(() -> {
            if (searchConfig != null) {
                show(searchConfig);
            }
        });

        ModService.onUpdate(() -> {
            TagService.setModId(modId);
            show(searchConfig);
        });

        TagService.onUpdate(() -> show(searchConfig));
    }

    public void show(SearchConfig searchConfig) {
        try {
            scale = Vars.mobile ? 0.8f : 1f;
            cardSize = (int) (300 * scale);
            cols = (int) Math.max(Math.floor(Core.scene.getWidth() / (cardSize + CARD_GAP)), 1);

            cont.clear();
            cont.pane(table -> {
                ModService.getMod(mods -> ModSelector(table, searchConfig, mods));

                table.row();
                SortSelector(table, searchConfig);
                table.row();
                table.top();

                tagProvider.get(schematicTags -> {
                    for (var tag : schematicTags) {
                        if (tag.values().isEmpty())
                            continue;
                        TagSelector(table, searchConfig, tag);
                        table.row();
                    }
                });
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
        } catch (Exception e) {
            Log.err(e);
        }
    }

    public void ModSelector(Table table, SearchConfig searchConfig, Seq<ModData> mods) {
        table.table(Styles.flatOver,
                text -> text.add(Core.bundle.format("messages.mod"))//
                        .fontScale(scale)//
                        .left()//
                        .labelAlign(Align.left))//
                .top()//
                .left()//
                .expandX()
                .padBottom(4);

        table.row();
        table.pane(card -> {
            card.defaults().size(cardSize, 50);
            int i = 0;
            for (var mod : mods) {
                card.button(btn -> {
                    btn.left();
                    if (mod.getIcon() != null && !mod.getIcon().isBlank()) {
                        btn.add(new NetworkImage(mod.getIcon()))//
                                .size(48 * scale)//
                                .padRight(4)//
                                .marginRight(4);
                    }
                    btn.add(mod.getName()).fontScale(scale);
                }, style,
                        () -> {
                            if (mod.getId().equals(modId)) {
                                modId = null;
                            } else {
                                modId = mod.getId();
                            }
                            TagService.setModId(modId);
                            Core.app.post(() -> show(searchConfig));
                        })//
                        .checked(mod.getId().equals(modId))//
                        .padRight(CARD_GAP)//
                        .padBottom(CARD_GAP)//
                        .left()//
                        .fillX()//
                        .margin(12);

                if (++i % cols == 0) {
                    card.row();
                }
            }
        })//
                .top()//
                .left()//
                .expandX()
                .scrollY(false)//
                .padBottom(48);
    }

    public void SortSelector(Table table, SearchConfig searchConfig) {
        var buttonGroup = new ButtonGroup<>();

        table.table(Styles.flatOver,
                text -> text.add(Core.bundle.format("messages.sort"))//
                        .fontScale(scale)//
                        .left()//
                        .labelAlign(Align.left))//
                .top()//
                .left()//
                .expandX()
                .padBottom(4);

        table.row();
        table.pane(card -> {
            card.defaults().size(cardSize, 50);
            int i = 0;
            for (var sort : Config.sorts) {
                card.button(btn -> btn.add(formatTag(sort.getName())).fontScale(scale)//
                        , style, () -> {
                            searchConfig.setSort(sort);
                        })//
                        .group(buttonGroup)//
                        .checked(sort.equals(searchConfig.getSort()))//
                        .padRight(CARD_GAP)//
                        .padBottom(CARD_GAP);

                if (++i % cols == 0) {
                    card.row();
                }
            }
        })//
                .top()//
                .left()//
                .expandX()
                .scrollY(false)//
                .padBottom(48);
    }

    public void TagSelector(Table table, SearchConfig searchConfig, TagData tag) {
        table.table(Styles.flatOver,
                text -> text.add(tag.name())//
                        .fontScale(scale)//
                        .left()
                        .labelAlign(Align.left))//
                .top()//
                .left()//
                .padBottom(4);

        table.row();

        table.pane(card -> {
            card.defaults().size(cardSize, 50);
            int z = 0;

            for (int i = 0; i < tag.values().size; i++) {
                var value = tag.values().get(i);

                card.button(btn -> {
                    btn.left();
                    if (value.icon() != null && !value.icon().isBlank()) {
                        btn.add(new NetworkImage(value.icon()))//
                                .size(48 * scale)//
                                .padRight(4)//
                                .marginRight(4);
                    }
                    btn.add(formatTag(value.name())).fontScale(scale);
                }, style, () -> {
                    searchConfig.setTag(tag, value);
                })//
                        .checked(searchConfig.containTag(tag, value))//
                        .padRight(CARD_GAP)//
                        .padBottom(CARD_GAP)//
                        .left()//
                        .expandX()
                        .margin(12);

                if (++z % cols == 0) {
                    card.row();
                }
            }
        })//
                .growY()//
                .wrap()//
                .top()//
                .left()//
                .expandX()
                .scrollX(true)//
                .scrollY(false)//
                .padBottom(48);
    }

    private String formatTag(String name) {
        return name;
    }

}
