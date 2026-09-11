package mindustrytool.features.chat;

import arc.Core;
import arc.func.Cons;
import arc.graphics.Color;
import arc.util.serialization.Base64Coder;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.ui.FileChooser;
import mindustry.ui.Styles;
import solim.UI;
import solim.overlay.SolimDialog;

public class AttachContentDialog extends SolimDialog {

    private final Cons<String> callback;

    public AttachContentDialog(Cons<String> callback) {
        super(Core.bundle.get("chat.attach-content", "Attach Content"));
        this.callback = callback;

        children(() -> {
            UI.column().grow().padding(UI.unit(3)).gap(UI.unit(2)).children(() -> {
                UI.button(this::selectSchematicFile)
                        .style(Styles.defaultb)
                        .growX()
                        .height(UI.unit(12))
                        .children(() -> {
                            UI.row().growX().gap(UI.unit(2)).children(() -> {
                                UI.image(Icon.file).size(UI.unit(6), UI.unit(6));
                                UI.text(Core.bundle.get("chat.select-file", "Select File (.msch)"))
                                        .color(Color.white)
                                        .left();
                            });
                        });

                UI.button(this::selectSaveFile)
                        .style(Styles.defaultb)
                        .growX()
                        .height(UI.unit(12))
                        .children(() -> {
                            UI.row().growX().gap(UI.unit(2)).children(() -> {
                                UI.image(Icon.map).size(UI.unit(6), UI.unit(6));
                                UI.text(Core.bundle.get("map", "Map / Save (.msav)"))
                                        .color(Color.white)
                                        .left();
                            });
                        });

                UI.button(this::pasteFromClipboard)
                        .style(Styles.defaultb)
                        .growX()
                        .height(UI.unit(12))
                        .children(() -> {
                            UI.row().growX().gap(UI.unit(2)).children(() -> {
                                UI.image(Icon.paste).size(UI.unit(6), UI.unit(6));
                                UI.text(Core.bundle.get("chat.paste-link", "Paste from Clipboard"))
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
