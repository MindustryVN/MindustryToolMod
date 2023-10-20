package main.ui;

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
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class SchematicDialog extends BaseDialog {

    private final List<String> sorts = Arrays.asList("time_1", "time_-1", "like_1");

    private Seq<SchematicData> schematics = new Seq<>();

    private final float IMAGE_SIZE = 196;
    private final float INFO_TABLE_HEIGHT = 60;

    private String sort = "time_1";
    private List<String> selectedTags = Arrays.asList("");

    private String search = "";

    TextField searchField;

    private List<String> schematicTags = new ArrayList<>();

    private boolean showSortDropdown = false;

    private PagingRequest<SchematicData> request;
    private ObjectMap<String, String> options = new ObjectMap<String, String>();

    public SchematicDialog() {
        super("Schematic Browser");

        request = new PagingRequest<>(SchematicData.class, "http://localhost:8080/api/v2/schematic");
        options.put("sort", sort);

        request.setOptions(options);
        request.getPage(this::handleSchematicResult);

        setItemPerPage();

        shown(this::SchematicBrowser);

        onResize(() -> {
            setItemPerPage();
            SchematicBrowser();
        });
    }

    private void setItemPerPage() {

        int columns = (int) (Core.graphics.getWidth() / Scl.scl(IMAGE_SIZE)) - 1;
        int rows = (int) (Core.graphics.getHeight() / Scl.scl(IMAGE_SIZE + INFO_TABLE_HEIGHT * 2));
        int itemPerPage = Math.max(columns * rows, 20);

        request.setItemPerPage(itemPerPage);
    }

    private void SchematicBrowser() {
        options.put("sort", sort);

        if (selectedTags.size() > 0)
            options.put("tags", Arrays.toString(selectedTags.toArray()));

        request.setOptions(options);

        clear();
        addCloseButton();
        SearchBar();
        row();
        SchematicContainer();
        row();
        Footer();
    }

    private void handleSearchResult(String result) {
        search = result;
        SchematicBrowser();
    }

    private void SearchBar() {
        table(searchBar -> {
            searchBar.table(searchBarWrapper -> {
                searchBarWrapper.left();
                searchBarWrapper.image(Icon.zoom);
                searchField = searchBarWrapper.field(search, this::handleSearchResult)
                        .growX()
                        .get();

                searchField.setMessageText("@schematic.search");
            }).fillX().expandX().padBottom(4);

            searchBar.button(sort, () -> {
                showSortDropdown = !showSortDropdown;
                SchematicBrowser();
            })
                    .width(100);

            Consumer<String> handleDropdownPress = (sortString) -> {
                sort = sortString;
                showSortDropdown = false;
                SchematicBrowser();
            };

            if (showSortDropdown) {
                searchBar.stack(new Table(Tex.pane, dropdown -> {
                    dropdown.setPosition(searchBar.x - 100, searchBar.y);
                    dropdown.setWidth(100);
                    dropdown.setHeight((sorts.size() - 1) * 20);
                    dropdown.toFront();

                    dropdown.visible(() -> true);

                    for (String sortString : sorts.stream().filter(s -> !s.equals(sort)).toList()) {
                        dropdown.button(sortString, () -> handleDropdownPress.accept(sortString))
                                .width(100)
                                .height(20)
                                .row();
                    }
                }));
            }

        }).fillX()
                .expandX()
                .marginLeft(8)
                .marginRight(8);

        row();
        pane(tagBar -> {
            for (String tag : schematicTags.stream().filter(s -> s.contains(search)).toList()) {
                tagBar.button(tag, Styles.nonet, () -> {
                    selectedTags.add(tag);
                });
            }
        });
    }

    private Cell<TextButton> Error(Table parent) {
        var error = parent.button("There is an error, reload?", Styles.nonet, () -> request.getPage(this::handleSchematicResult));

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
        return parent.pane(container -> {
            float sum = 0;

            for (var schematic : schematics) {
                if (sum + Scl.scl(IMAGE_SIZE) >= Core.graphics.getWidth()) {
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
                    schematicPreview.button(image -> image.add(new SchematicImage(schematic.id)), Styles.nonet, () -> {
                        // Image click callback
                    }).size(IMAGE_SIZE);
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
        });
    }

    private Cell<Table> SchematicContainer() {
        return table(container -> {
            if (request.isError()) {
                Error(container);
                return;
            }

            if (request.isLoading()) {
                Loading(container);
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

        SchematicBrowser();
    }

    private void handleCopySchematic(SchematicData schematic) {

    }

    private void handleDownloadSchematic(SchematicData schematic) {

    }
}
