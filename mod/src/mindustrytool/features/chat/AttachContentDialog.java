package mindustrytool.features.chat;

import arc.Core;
import arc.func.Cons;
import arc.graphics.Color;
import arc.util.serialization.Base64Coder;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.ui.FileChooser;
import mindustry.ui.Styles;
import solim.overlay.SolimDialog;
import solim.ui.Ui;

public class AttachContentDialog extends SolimDialog {

    private final Cons<String> callback;

    public AttachContentDialog(Cons<String> callback) {
        super(Core.bundle.get("chat.attach-content", "Attach Content"));
        this.callback = callback;

        children(() -> {
            Ui.column().grow().padding(Ui.unit(3)).gap(Ui.unit(2)).children(() -> {
                Ui.button(this::selectSchematicFile)
                        .style(Styles.defaultb)
                        .growX()
                        .height(Ui.unit(12))
                        .children(() -> {
                            Ui.row().growX().gap(Ui.unit(2)).children(() -> {
                                Ui.image(Icon.file).size(Ui.unit(6), Ui.unit(6));
                                Ui.text(Core.bundle.get("chat.select-file", "Select File (.msch)"))
                                        .color(Color.white)
                                        .left();
                            });
                        });

                Ui.button(this::selectSaveFile)
                        .style(Styles.defaultb)
                        .growX()
                        .height(Ui.unit(12))
                        .children(() -> {
                            Ui.row().growX().gap(Ui.unit(2)).children(() -> {
                                Ui.image(Icon.map).size(Ui.unit(6), Ui.unit(6));
                                Ui.text(Core.bundle.get("map", "Map / Save (.msav)"))
                                        .color(Color.white)
                                        .left();
                            });
                        });

                Ui.button(this::pasteFromClipboard)
                        .style(Styles.defaultb)
                        .growX()
                        .height(Ui.unit(12))
                        .children(() -> {
                            Ui.row().growX().gap(Ui.unit(2)).children(() -> {
                                Ui.image(Icon.paste).size(Ui.unit(6), Ui.unit(6));
                                Ui.text(Core.bundle.get("chat.paste-link", "Paste from Clipboard"))
                                        .color(Color.white)
                                        .left();
                            });
                        });
            });
        });

        actionButton(Core.bundle.get("button.cancel", "Cancel"), this::hide);
    }

    private void selectSchematicFile() {
        FileChooser.open("msch").submit(file -> {
            if (file == null) return;
            try {
                byte[] bytes = file.readBytes();
                String base64 = new String(Base64Coder.encode(bytes));
                callback.get(base64);
                hide();
            } catch (Throwable t) {
                Vars.ui.showException(t);
            }
        });
    }

    private void selectSaveFile() {
        FileChooser.open("msav").submit(file -> {
            if (file == null) return;
            try {
                byte[] bytes = file.readBytes();
                String base64 = new String(Base64Coder.encode(bytes));
                callback.get(base64);
                hide();
            } catch (Throwable t) {
                Vars.ui.showException(t);
            }
        });
    }

    private void pasteFromClipboard() {
        String content = Core.app.getClipboardText();
        if (content != null && !content.trim().isEmpty()) {
            callback.get(content.trim());
            hide();
        } else {
            Vars.ui.showInfoFade(Core.bundle.get("chat.clipboard-empty", "Clipboard is empty"));
        }
    }
}
