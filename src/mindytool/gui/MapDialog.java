package mindytool.gui;

import arc.Core;
import arc.files.Fi;
import arc.graphics.Color;
import arc.scene.ui.Button;
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
import arc.util.Log;
import arc.util.Strings;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

import static mindustry.Vars.*;

import mindytool.config.Config;
import mindytool.data.MapData;
import mindytool.data.SearchConfig;
import mindytool.data.TagService;
import mindytool.net.Api;
import mindytool.net.PagingRequest;

public class MapDialog extends BaseDialog {

    private final MapInfoDialog infoDialog = new MapInfoDialog();
    private final FilterDialog filterDialog = new FilterDialog((tag) -> TagService.getTag(group -> tag.get(group.map)));

    private Seq<MapData> mapsData = new Seq<>();

    private final float IMAGE_SIZE = 196;
    private final float INFO_TABLE_HEIGHT = 60;

    private SearchConfig searchConfig = new SearchConfig();

    private String search = "";

    TextField searchField;

    private PagingRequest<MapData> request;
    private ObjectMap<String, String> options = new ObjectMap<String, String>();

    public MapDialog() {
        super("Map Browser");

        request = new PagingRequest<>(MapData.class, Config.API_URL + "maps");

        setItemPerPage();

        options.put("sort", searchConfig.getSort().getValue());
        request.setOptions(options);

        filterDialog.hidden(() -> {
            if (searchConfig.isChanged()) {
                searchConfig.update();
                options.put("tags", searchConfig.getSelectedTagsString());
                options.put("sort", searchConfig.getSort().getValue());
                request.setPage(0);
                request.getPage(this::handleMapResult);
            }
        });

        onResize(() -> {
            setItemPerPage();
            MapBrowser();
        });
        request.getPage(this::handleMapResult);
        shown(this::MapBrowser);
    }

    private void setItemPerPage() {
        int columns = (int) (Core.graphics.getWidth() / Scl.scl(IMAGE_SIZE)) - 1;
        int rows = (int) (Core.graphics.getHeight() / Scl.scl(IMAGE_SIZE + INFO_TABLE_HEIGHT * 2));
        int size = Math.max(columns * rows, 20);

        request.setItemPerPage(size);
    }

    private void MapBrowser() {
        clear();

        try {
            addCloseButton();
            row();
            SearchBar();
            row();
            MapContainer();
            row();
            Footer();
        } catch (Exception ex) {
            clear();
            addCloseButton();
            table(container -> Error(container, Core.bundle.format("messages.error") + "\n Error: " + ex.getMessage()));
            Log.err(ex);
        }
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
                    .width(150).padLeft(2).padRight(2);

            searchBar.table(searchBarWrapper -> {
                searchBarWrapper.left();
                searchField = searchBarWrapper.field(search, (result) -> {
                    search = result;
                    options.put("name", result);
                    request.setPage(0);
                }).growX().get();

                searchField.setMessageText("@map.search");
            }).fillX().expandX().padBottom(2).padLeft(2).padRight(2);

            searchBar.button(Icon.filterSmall, () -> loadingWrapper(() -> filterDialog.show(searchConfig))).padLeft(2).padRight(2).width(60);

            searchBar.button(Icon.zoomSmall, () -> loadingWrapper(() -> request.getPage(this::handleMapResult))).padLeft(2).padRight(2).width(60);

        }).fillX().expandX();

        row();
        pane(tagBar -> {
            for (String tag : searchConfig.getSelectedTags()) {
                tagBar.table(Tex.button, table -> {
                    table.add(Strings.capitalize(tag));
                    table.button(Icon.cancelSmall, Styles.clearNonei, () -> {
                        searchConfig.getSelectedTags().remove(tag);
                        options.put("tags", searchConfig.getSelectedTagsString());
                        request.setPage(0);
                        MapBrowser();
                    }).margin(4);
                });
            }
        }).scrollY(false);
    }

    private Cell<TextButton> Error(Table parent, String message) {
        Cell<TextButton> error = parent.button(message, Styles.nonet, () -> request.getPage(this::handleMapResult));

        return error.center().labelAlign(0).expand().fill();
    }

    private Cell<Label> Loading(Table parent) {
        return parent.labelWrap(Core.bundle.format("messages.loading")).center().labelAlign(0).expand().fill();
    }

    private Cell<ScrollPane> MapScrollContainer(Table parent) {
        if (mapsData.size == 0)
            return parent.pane(container -> container.add("message.no-result"));

        return parent.pane(container -> {
            float sum = 0;

            for (MapData mapData : mapsData) {
                if (sum + Scl.scl(IMAGE_SIZE * 2) >= Math.max(Core.graphics.getHeight(), Core.graphics.getWidth())) {
                    container.row();
                    sum = 0;
                }

                Button[] button = { null };
                button[0] = container.button(mapPreview -> {
                    mapPreview.top();
                    mapPreview.margin(0f);
                    mapPreview.table(buttons -> {
                        buttons.center();
                        buttons.defaults().size(50f);
                        buttons.button(Icon.download, Styles.emptyi, () -> handleDownloadMap(mapData)).padLeft(2).padRight(2);
                        buttons.button(Icon.info, Styles.emptyi, () -> Api.findMapById(mapData.id(), infoDialog::show)).tooltip("@info.title");

                    }).growX().height(50f);

                    mapPreview.row();
                    mapPreview.stack(new MapImage(mapData.id()), new Table(mapName -> {
                        mapName.top();
                        mapName.table(Styles.black3, c -> {
                            Label label = c.add(mapData.name()).style(Styles.outlineLabel).color(Color.white).top().growX().width(200f - 8f).get();
                            label.setEllipsis(true);
                            label.setAlignment(Align.center);
                        }).growX().margin(1).pad(4).maxWidth(Scl.scl(200f - 8f)).padBottom(0);
                    })).size(200f);

                    mapPreview.row();
                    mapPreview.table(stats -> DetailStats.draw(stats, mapData.likes(), mapData.dislikes(), mapData.downloadCount())).margin(8);

                }, () -> {

                }).pad(4).style(Styles.flati).get();

                button[0].getStyle().up = Tex.pane;

                sum += button[0].getPrefWidth();
            }
            container.top();
        }).pad(20).scrollY(true).expand().fill();

    }

    private void Footer() {
        table(footer -> {
            footer.button(Icon.left, () -> request.previousPage(this::handleMapResult)).margin(4).pad(4).width(100).disabled(request.isLoading() || request.getPage() == 0 || request.isError()).height(40);

            footer.table(Tex.buttonDisabled, table -> {
                table.labelWrap(String.valueOf(request.getPage() + 1)).width(50).style(Styles.defaultLabel).labelAlign(0).center().fill();
            }).pad(4).height(40);

            footer.button(Icon.edit, () -> {
                ui.showTextInput("@select-page", "", "", input -> {
                    try {
                        request.setPage(Integer.parseInt(input));
                        shown(this::MapBrowser);
                    } catch (Exception e) {
                        ui.showInfo("Invalid input");
                    }
                });
            })//
                    .margin(4)//
                    .pad(4)//
                    .width(100)//
                    .disabled(request.isLoading() || request.hasMore() == false || request.isError()).height(40);

            footer.button(Icon.right, () -> request.nextPage(this::handleMapResult)).margin(4).pad(4).width(100).disabled(request.isLoading() || request.hasMore() == false || request.isError()).height(40);

            footer.button("@upload", () -> Core.app.openURI(Config.UPLOAD_MAP_URL)).margin(4).pad(4).width(100).disabled(request.isLoading() || request.hasMore() == false || request.isError()).height(40);

            footer.bottom();
        }).expandX().fillX();
    }

    private Cell<Table> MapContainer() {
        return table(container -> {
            if (request.isLoading()) {
                Loading(container);
                return;
            }

            if (request.isError()) {
                Error(container, String.format("There is an error, reload? (%s)", request.getError()));
                return;
            }

            MapScrollContainer(container);
        }).expand().fill().margin(10).top();
    }

    private void handleMapResult(Seq<MapData> maps) {
        if (maps != null)
            this.mapsData = maps;
        else
            this.mapsData.clear();

        MapBrowser();
    }

    private void handleDownloadMap(MapData map) {
        Api.downloadMap(map.id(), result -> {
            Fi mapFile = Vars.customMapDirectory.child(map.id().toString());
            mapFile.writeBytes(result);
            Vars.maps.importMap(mapFile);
            ui.showInfoFade("@map.saved");
        });
    }
}
