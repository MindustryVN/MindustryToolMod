package solim.overlay;

import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Table;
import solim.core.Disposable;
import solim.ui.ParentStack;

/**
 * Dialog wrapping a Table-based overlay.
 * Provides show/hide.
 */
public final class SolimDialog implements Disposable {
    private final String title;
    private final Runnable content;
    private final Table dialog = new Table();
    private final Table contentTable = new Table();
    private boolean shown = false;

    public SolimDialog(String title, Runnable content) {
        this.title = title;
        this.content = content;
        dialog.top();
        // add title
        if (title != null) {
            dialog.add(title).row();
        }
        // attach content
        ParentStack.push(contentTable);
        try {
            content.run();
        } finally {
            ParentStack.pop();
        }
        dialog.add(contentTable);
    }

    public static SolimDialog of(String title, Runnable content) {
        return new SolimDialog(title, content);
    }

    public Table dialog() {
        return dialog;
    }

    public void show() {
        shown = true;
    }

    public void hide() {
        shown = false;
    }

    public boolean isShown() {
        return shown;
    }

    @Override
    public void dispose() {
        hide();
    }
}
