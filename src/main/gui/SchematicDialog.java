package main.gui;

import arc.Core;
import arc.graphics.Color;
import arc.scene.ui.Label;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.TextButton;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Align;
import arc.util.Http;
import arc.util.Strings;
import main.config.Config;
import main.data.SchematicData;
import main.data.SearchConfig;
import main.net.PagingRequest;
import mindustry.Vars;
import mindustry.game.Schematic;
import mindustry.game.Schematics;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

import java.util.function.Consumer;

import static mindustry.Vars.*;

public class SchematicDialog extends BaseDialog {

    private final SchematicInfoDialog infoDialog = new SchematicInfoDialog();
    private final SchematicFilterDialog filterDialog = new SchematicFilterDialog();

    private Seq<SchematicData> schematics = new Seq<>();

    private final float IMAGE_SIZE = 196;
    private final float INFO_TABLE_HEIGHT = 60;

    private SearchConfig searchConfig = new SearchConfig();

    private String search = "";

    TextField searchField;

    private PagingRequest<SchematicData> request;
    private ObjectMap<String, String> options = new ObjectMap<String, String>();

    public SchematicDialog() {
        super("Schematic Browser");

        request = new PagingRequest<>(SchematicData.class, Config.API_URL + "schematic");

        setItemPerPage();

        options.put("sort", searchConfig.getSort().getValue());
        request.setOptions(options);

        filterDialog.hidden(() -> {
            if (searchConfig.isChanged()) {
                searchConfig.update();
                options.put("tags", searchConfig.getSelectedTagsString());
                options.put("sort", searchConfig.getSort().getValue());
                request.setPage(0);
                request.getPage(this::handleSchematicResult);
            }
        });

        shown(this::SchematicBrowser);

        onResize(() -> {
            setItemPerPage();
            SchematicBrowser();
        });

        request.getPage(this::handleSchematicResult);
    }

    private void setItemPerPage() {
        int columns = (int) (Core.graphics.getWidth() / Scl.scl(IMAGE_SIZE)) - 1;
        int rows = (int) (Core.graphics.getHeight() / Scl.scl(IMAGE_SIZE + INFO_TABLE_HEIGHT * 2));
        int itemPerPage = Math.max(columns * rows, 20);

        request.setItemPerPage(itemPerPage);
    }

    private void SchematicBrowser() {
        clear();
        addCloseButton();
        row();
        SearchBar();
        row();
        SchematicContainer();
        row();
        Footer();
    }

    public void loadingWrapper(Runnable action) {
        Core.app.post(() -> {
            if (request.isLoading())
                ui.showInfoFade("Loading");
            else
                action.run();
        });
    }

    private void SearchBar() {
        table(searchBar -> {
            searchBar.button("@back", Icon.leftSmall, this::hide)//
                    .width(200)
                    .marginLeft(4)
                    .marginRight(4);

            searchBar.table(searchBarWrapper -> {
                searchBarWrapper.left();
                searchField = searchBarWrapper.field(search, (result) -> {
                    search = result;
                    options.put("name", result);
                    request.setPage(0);
                })
                        .growX()
                        .get();

                searchField.setMessageText("@schematic.search");
            })
                    .fillX()
                    .expandX()
                    .padBottom(4)
                    .marginLeft(4)
                    .marginRight(4);

            searchBar.button(Icon.filterSmall, () -> loadingWrapper(() -> filterDialog.show(searchConfig)))
                    .marginLeft(4)
                    .marginRight(4)
                    .width(60);

            searchBar.button(Icon.zoomSmall, () -> loadingWrapper(() -> request.getPage(this::handleSchematicResult)))
                    .marginLeft(4)
                    .marginRight(4)
                    .width(60);

        })
                .fillX()
                .expandX();

        row();
        pane(tagBar -> {
            for (String tag : searchConfig.getSelectedTags()) {
                tagBar.table(Tex.button, table -> {
                    table.add(Strings.capitalize(tag));
                    table.button(Icon.cancelSmall, Styles.clearNonei, () -> {
                        searchConfig.getSelectedTags().remove(tag);
                        options.put("tags", searchConfig.getSelectedTagsString());
                        request.setPage(0);
                        SchematicBrowser();
                    }).margin(4);
                });
            }
        }).scrollY(false);
    }

    private Cell<TextButton> Error(Table parent) {
        var error = parent.button(String.format("There is an error, reload? (%s)", request.getError()), Styles.nonet,
                () -> request.getPage(this::handleSchematicResult));

        return error.center()
                .labelAlign(0)
                .expand()
                .fill();
    }

    private Cell<Label> Loading(Table parent) {
        return parent.labelWrap("Loading")
                .center()
                .labelAlign(0)
                .expand()
                .fill();
    }

    private Cell<ScrollPane> SchematicScrollContainer(Table parent) {
        if (schematics.size == 0)
            return parent.pane(container -> container.add("No result"));

        return parent.pane(container -> {
            float sum = 0;

            for (var schematic : schematics) {
                if (sum + Scl.scl(IMAGE_SIZE * 2) >= Core.graphics.getWidth()) {
                    container.row();
                    sum = 0;
                }

                var button = container.table(schematicPreview -> {
                    schematicPreview.table(Tex.pane, buttonContainer -> {
                        buttonContainer.button(Icon.copy, Styles.emptyi, () -> handleCopySchematic(schematic))
                                .marginLeft(16)
                                .marginRight(16);

                        buttonContainer.button(Icon.download, Styles.emptyi, () -> handleDownloadSchematic(schematic))
                                .marginLeft(16)
                                .marginRight(16).pad(4);

                    }).fillX().height(INFO_TABLE_HEIGHT);

                    schematicPreview.row();
                    schematicPreview.button(image -> image.add(new SchematicImage(schematic.id)), //
                            Styles.nonet,
                            () -> infoDialog.show(schematic)).size(IMAGE_SIZE);
                    schematicPreview.row();

                    schematicPreview.table(Tex.pane, t -> {
                        Label label = t.add(schematic.name)
                                .style(Styles.outlineLabel)
                                .color(Color.white)
                                .top()
                                .growX()
                                .maxWidth(196)
                                .get();

                        label.setEllipsis(true);
                        label.setAlignment(Align.center);

                    }).width(196).height(INFO_TABLE_HEIGHT);
                }).margin(4);

                sum += button.prefWidth();
            }
            container.top();
        })
                .pad(20)
                .scrollY(true)
                .expand()
                .fill();

    }

    private void Footer() {
        table(footer -> {
            footer.button(Icon.left, () -> request.previousPage(this::handleSchematicResult))
                    .margin(4)
                    .pad(4)
                    .width(100)
                    .disabled(request.isLoading() || request.getPage() == 0);

            footer.labelWrap(String.valueOf(request.getPage() + 1))
                    .width(50)
                    .style(Styles.outlineLabel)
                    .labelAlign(0)
                    .margin(4)
                    .center();

            footer.button(Icon.right, () -> request.nextPage(this::handleSchematicResult))
                    .margin(4)
                    .pad(4)
                    .width(100)
                    .disabled(request.isLoading() || request.hasMore() == false);

            footer.bottom();
        }).expandX().fillX();
    }

    private Cell<Table> SchematicContainer() {
        return table(container -> {
            if (request.isLoading()) {
                Loading(container);
                return;
            }

            if (request.isError()) {
                Error(container);
                return;
            }

            SchematicScrollContainer(container);
        })
                .expand()
                .fill()
                .margin(10)
                .top();
    }

    private void handleSchematicResult(Seq<SchematicData> schematics) {
        if (schematics != null)
            this.schematics = schematics;
        else
            this.schematics.clear();

        SchematicBrowser();
    }

    private void getSchematicData(SchematicData schematic, Consumer<String> consumer) {
        Core.app.post(() -> Http.get(Config.API_URL + String.format("schematic/%s/data", schematic.id))//
                .timeout(120000)//
                .submit(result -> consumer.accept(result.getResultAsString())));
    }

    private void handleCopySchematic(SchematicData schematic) {
        getSchematicData(schematic, result -> {
            Core.app.setClipboardText(result);
            ui.showInfoFade("@copied");
        });
    }

    private void handleDownloadSchematic(SchematicData schematic) {
        getSchematicData(schematic, result -> {
            Schematic s = Schematics.readBase64(result);
            s.removeSteamID();
            Vars.schematics.add(s);
            Vars.ui.schematics.hide();
            Vars.ui.schematics.show();
            ui.showInfoFade("@schematic.saved");
        });
    }
}
