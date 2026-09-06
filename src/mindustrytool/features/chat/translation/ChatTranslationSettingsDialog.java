package mindustrytool.features.chat.translation;

import arc.Core;
import arc.graphics.Color;
import arc.scene.ui.Button;
import arc.scene.ui.ButtonGroup;
import arc.scene.ui.Label;
import arc.scene.ui.TextButton;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Table;
import arc.util.Align;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

public class ChatTranslationSettingsDialog extends BaseDialog {
    public ChatTranslationSettingsDialog(ChatTranslationFeature feature) {
        super(Core.bundle.get("chat-translation.settings.title"));
        name = "chatTranslationSettingDialog";
        addCloseButton();

        Table root = new Table();
        root.top().left().defaults().top().left().padBottom(5);
        root.image().height(4).color(Color.gray).fillX().pad(10).row();

        root.add("@chat-translation.settings.options-section").style(Styles.outlineLabel).padBottom(5).row();

        Table optionsTable = new Table();
        optionsTable.left().defaults().left().padBottom(6);

        optionsTable.check("@chat-translation.settings.auto-incoming", ChatTranslationConfig.isAutoTranslateIncoming(), b -> {
            ChatTranslationConfig.setAutoTranslateIncoming(b);
        }).left().row();

        optionsTable.check("@chat-translation.settings.reverse-enabled", ChatTranslationConfig.isReverseEnabled(), b -> {
            ChatTranslationConfig.setReverseEnabled(b);
        }).left().row();

        optionsTable.check("@chat-translation.settings.reverse-include-original", ChatTranslationConfig.isReverseIncludeOriginal(), b -> {
            ChatTranslationConfig.setReverseIncludeOriginal(b);
        }).left().row();

        optionsTable.table(t -> {
            t.left();
            t.add("@chat-translation.settings.reverse-target-language").padRight(10);
            TextButton targetBtn = t.button(ChatTranslationConfig.getReverseTargetLang().toUpperCase(), Styles.flatBordert, () -> {
                mindustry.ui.dialogs.BaseDialog d = new mindustry.ui.dialogs.BaseDialog(Core.bundle.get("chat-translation.settings.reverse-target-language", "Target Language"));
                d.addCloseButton();
                Table list = new Table();
                String[][] langs = {
                        {"en", "English"},
                        {"zh-cn", "Chinese (Simplified)"},
                        {"ru", "Russian"},
                        {"ja", "Japanese"},
                        {"ko", "Korean"},
                        {"fr", "French"},
                        {"de", "German"},
                        {"es", "Spanish"}
                };
                for (String[] pair : langs) {
                    list.button(pair[1] + " (" + pair[0] + ")", Styles.flatBordert, () -> {
                        ChatTranslationConfig.setReverseTargetLang(pair[0]);
                        d.hide();
                    }).size(280f, 45f).pad(4f).row();
                }
                d.cont.pane(list);
                d.show();
            }).size(120f, 36f).get();
            targetBtn.update(() -> targetBtn.setText(ChatTranslationConfig.getReverseTargetLang().toUpperCase()));
        }).padTop(4f).row();

        root.add(optionsTable).growX().row();

        root.image().height(4).color(Color.gray).fillX().pad(10).row();

        root.add("@chat-translation.settings.providers").style(Styles.outlineLabel).padBottom(5).row();

        Table providerList = new Table();
        ButtonGroup<Button> group = new ButtonGroup<>();
        group.setMinCheckCount(1);

        for (TranslationProvider provider : feature.getProviders()) {
            Button card = new Button(Styles.togglet);
            card.top().left().margin(12);

            card.table(h -> {
                h.left();
                h.label(provider::getName).style(Styles.defaultLabel).growX().left();
            }).growX().row();

            card.collapser(s -> {
                s.add(provider.settings()).growX().padTop(10);
            }, false, card::isChecked).growX().row();

            card.clicked(() -> {
                if (feature.getCurrentProvider() != provider) {
                    feature.setCurrentProvider(provider);
                    ChatTranslationConfig.setProviderId(provider.getId());
                }
            });

            if (feature.getCurrentProvider() == provider) {
                card.setChecked(true);
            }

            group.add(card);
            providerList.add(card).growX().padBottom(8).row();
        }

        root.add(providerList).growX().row();

        root.image().height(4).color(Color.gray).fillX().pad(10).row();

        Label resultLabel = new Label("");
        resultLabel.setWrap(true);
        resultLabel.setAlignment(Align.center);

        TextField testInput = new TextField("Hello, Mindustry!");

        TextButton testButton = new TextButton(Core.bundle.get("chat-translation.settings.test-button"),
                Styles.defaultt);
        testButton.clicked(() -> {
            testButton.setDisabled(true);
            testButton.setText(Core.bundle.get("chat-translation.settings.testing"));
            resultLabel.setText(Core.bundle.get("chat-translation.settings.testing-connection"));

            feature.getCurrentProvider().translate(testInput.getText()).thenAccept(result -> {
                Core.app.post(() -> {
                    testButton.setDisabled(false);
                    testButton.setText(Core.bundle.get("chat-translation.settings.test-button"));
                    resultLabel.setText(Core.bundle.get("chat-translation.settings.success") + result);
                });
            }).exceptionally(e -> {
                Core.app.post(() -> {
                    testButton.setDisabled(false);
                    testButton.setText(Core.bundle.get("chat-translation.settings.test-button"));
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    String message = cause.getMessage() != null ? cause.getMessage() : cause.toString();
                    resultLabel.setText(Core.bundle.get("chat-translation.settings.failed") + message);
                });
                return null;
            });
        });

        root.add(testInput).growX().pad(10).row();
        root.add(testButton).size(250, 50).pad(10)
                .disabled(b -> testButton.getText().toString()
                        .equals(Core.bundle.get("chat-translation.settings.testing")))
                .row();
        root.add(resultLabel).growX().pad(10).row();

        root.label(() -> feature.getLastError() == null ? ""
                : Core.bundle.get("chat-translation.settings.error-prefix") + feature.getLastError())
                .color(Color.red)
                .visible(() -> feature.getLastError() != null)
                .row();

        cont.pane(root).growX().maxWidth(700f).margin(20);
        cont.pack();
    }
}
