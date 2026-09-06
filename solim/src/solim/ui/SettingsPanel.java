package solim.ui;

import arc.Core;
import arc.scene.Element;
import arc.util.Log;
import solim.core.BaseComponent;
import solim.layout.Justify;
import solim.signal.Computed;
import solim.signal.Effect;
import solim.signal.Signal;
import solim.style.Styles;
import static solim.ui.Ui.*;

/**
 * Example final API settings panel from requirement.md §16.
 */
public final class SettingsPanel extends BaseComponent {

    private final Signal<Boolean> darkMode =
        Signal.of(false);

    private final Signal<Boolean> dirty =
        Signal.of(false);

    private final Computed<String> saveText =
        dirty.map(value ->
            value
                ? t("solim.settings.save-dirty", "● Save Changes")
                : t("solim.settings.save", "Save Changes")
        );

    private Effect logger;

    private static String t(String key, String fallback) {
        if (Core.bundle != null && Core.bundle.has(key)) {
            try {
                return Core.bundle.get(key);
            } catch (Throwable ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    @Override
    protected Element build() {

        logger = Effect.of(() -> {
            Log.info("Dirty state: @", dirty.get());
        });

        return column(() -> {

            text(t("solim.settings.title", "Settings"));

            divider();

            row(() -> {

                text(t("solim.settings.dark-mode", "Dark Mode"));

                button(
                    darkMode.map(value ->
                        value ? t("solim.settings.dark-mode.on", "On") : t("solim.settings.dark-mode.off", "Off")
                    ),
                    () -> {
                        darkMode.set(!darkMode.get());
                        dirty.set(true);
                    }
                )
                    .style(
                        darkMode.map(value ->
                            value
                                ? Styles.PRIMARY
                                : Styles.GHOST
                        )
                    );

            });

            spacer();

            row(() -> {

                button(t("solim.settings.discard", "Discard"), () -> {
                    dirty.set(false);
                });

                button(saveText, () -> {
                    dirty.set(false);
                })
                    .enabled(dirty)
                    .style(Styles.PRIMARY);

            })
                .justify(Justify.END)
                .gap(8);

        })
            .padding(24)
            .gap(16)
            .element();
    }

    @Override
    public void dispose() {
        if (logger != null) {
            logger.dispose();
        }
    }

    public Signal<Boolean> darkMode() {
        return darkMode;
    }

    public Signal<Boolean> dirty() {
        return dirty;
    }

    public Computed<String> saveText() {
        return saveText;
    }

    public Effect logger() {
        return logger;
    }
}
