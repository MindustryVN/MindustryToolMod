package mindustrytool.features.teamresource;

import arc.Core;
import arc.func.Boolc;
import arc.graphics.Color;
import arc.scene.event.Touchable;
import arc.scene.ui.CheckBox;
import arc.scene.ui.Dialog;
import arc.scene.ui.Label;
import arc.scene.ui.Slider;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import mindustry.gen.Icon;
import mindustry.ui.Styles;
import solim.overlay.SolimDialog;

/**
 * Settings dialog for configuring Team Resource Tracker options, using SolimDialog.
 */
public class TeamResourceSettingsDialog extends SolimDialog {
    private final TeamResourceOverlay overlay;

    public TeamResourceSettingsDialog(TeamResourceOverlay overlay) {
        super(Core.bundle.get("team-resources.settings.title", "Team Resources Settings"));
        this.overlay = overlay;

        addCloseButton();
        closeOnBack();

        actionButton(Core.bundle.get("team-resources.reset-to-defaults", "Reset to Defaults"), Icon.refresh, 250f, 64f, () -> {
            TeamResourceConfig.resetToDefaults();
            overlay.rebuild();
            rebuildContent();
        });

        rebuildContent();
    }

    @Override
    public Dialog show() {
        rebuildContent();
        return super.show();
    }

    public void rebuildContent() {
        Table table = cont;
        table.clear();
        table.defaults().pad(6).left();

        float width = Math.min(Core.graphics.getWidth() / 1.2f, 460f);

        // Opacity Slider
        Slider opacitySlider = new Slider(0.1f, 1f, 0.05f, false);
        opacitySlider.setValue(TeamResourceConfig.opacity());
        Label opacityValue = new Label(Math.round(TeamResourceConfig.opacity() * 100) + "%", Styles.outlineLabel);
        opacityValue.setColor(Color.lightGray);

        Table opacityContent = new Table();
        opacityContent.touchable = Touchable.disabled;
        opacityContent.margin(3f, 33f, 3f, 33f);
        opacityContent.add(Core.bundle.get("team-resources.opacity", "Opacity"), Styles.outlineLabel).left().growX();
        opacityContent.add(opacityValue).padLeft(10f).right();

        opacitySlider.changed(() -> {
            TeamResourceConfig.opacity(opacitySlider.getValue());
            opacityValue.setText(Math.round(TeamResourceConfig.opacity() * 100) + "%");
            overlay.rebuild();
        });
        table.stack(opacitySlider, opacityContent).width(width).left().padTop(4f).row();

        // Scale Slider
        Slider sizeSlider = new Slider(0.3f, 2f, 0.1f, false);
        sizeSlider.setValue(TeamResourceConfig.scale());
        Label sizeValue = new Label(Math.round(TeamResourceConfig.scale() * 100) + "%", Styles.outlineLabel);
        sizeValue.setColor(Color.lightGray);

        Table sizeContent = new Table();
        sizeContent.touchable = Touchable.disabled;
        sizeContent.margin(3f, 33f, 3f, 33f);
        sizeContent.add(Core.bundle.get("team-resources.size", "Scale"), Styles.outlineLabel).left().growX();
        sizeContent.add(sizeValue).padLeft(10f).right();

        sizeSlider.changed(() -> {
            TeamResourceConfig.scale(sizeSlider.getValue());
            sizeValue.setText(Math.round(TeamResourceConfig.scale() * 100) + "%");
            overlay.rebuild();
        });
        table.stack(sizeSlider, sizeContent).width(width).left().padTop(4f).row();

        // Overlay Width Slider
        Slider widthSlider = new Slider(0.15f, 1f, 0.05f, false);
        widthSlider.setValue(TeamResourceConfig.overlayWidth());
        Label widthValue = new Label(Math.round(TeamResourceConfig.overlayWidth() * 100) + "%", Styles.outlineLabel);
        widthValue.setColor(Color.lightGray);

        Table widthContent = new Table();
        widthContent.touchable = Touchable.disabled;
        widthContent.margin(3f, 33f, 3f, 33f);
        widthContent.add(Core.bundle.get("team-resources.width", "Overlay Width"), Styles.outlineLabel).left().growX();
        widthContent.add(widthValue).padLeft(10f).right();

        widthSlider.changed(() -> {
            TeamResourceConfig.overlayWidth(widthSlider.getValue());
            widthValue.setText(Math.round(TeamResourceConfig.overlayWidth() * 100) + "%");
            overlay.rebuild();
        });
        table.stack(widthSlider, widthContent).width(width).left().padTop(4f).row();

        // Overlay Height Slider (controls max height ceiling, overlay wraps content)
        Slider heightSlider = new Slider(0.15f, 1f, 0.05f, false);
        heightSlider.setValue(TeamResourceConfig.overlayHeight());
        Label heightValue = new Label(Math.round(TeamResourceConfig.overlayHeight() * 100) + "%", Styles.outlineLabel);
        heightValue.setColor(Color.lightGray);

        Table heightContent = new Table();
        heightContent.touchable = Touchable.disabled;
        heightContent.margin(3f, 33f, 3f, 33f);
        heightContent.add(Core.bundle.get("team-resources.height", "Overlay Height"), Styles.outlineLabel).left().growX();
        heightContent.add(heightValue).padLeft(10f).right();

        heightSlider.changed(() -> {
            TeamResourceConfig.overlayHeight(heightSlider.getValue());
            heightValue.setText(Math.round(TeamResourceConfig.overlayHeight() * 100) + "%");
            overlay.rebuild();
        });
        table.stack(heightSlider, heightContent).width(width).left().padTop(4f).row();

        // Checkboxes with explicit initial checked state and change listeners
        addCheck(table, Core.bundle.get("team-resources.show-items", "Show Items"), TeamResourceConfig.showItems(), b -> {
            TeamResourceConfig.showItems(b);
            overlay.rebuild();
        }).padTop(6).row();

        addCheck(table, Core.bundle.get("team-resources.show-units", "Show Units"), TeamResourceConfig.showUnits(), b -> {
            TeamResourceConfig.showUnits(b);
            overlay.rebuild();
        }).padTop(4).row();

        addCheck(table, Core.bundle.get("team-resources.show-power", "Show Power"), TeamResourceConfig.showPower(), b -> {
            TeamResourceConfig.showPower(b);
            overlay.rebuild();
        }).padTop(4).row();

        addCheck(table, Core.bundle.get("team-resources.show-stored-power", "Show Stored Power"), TeamResourceConfig.showStoredPower(), b -> {
            TeamResourceConfig.showStoredPower(b);
            overlay.rebuild();
        }).padTop(4).row();

        addCheck(table, Core.bundle.get("team-resources.hide-background", "Hide Background"), TeamResourceConfig.hideBackground(), b -> {
            TeamResourceConfig.hideBackground(b);
            overlay.rebuild();
        }).padTop(4).row();

        addCheck(table, Core.bundle.get("team-resources.always-show-flow-rate", "Always Show Flow Rate"), TeamResourceConfig.alwaysShowFlowRate(), b -> {
            TeamResourceConfig.alwaysShowFlowRate(b);
            overlay.rebuild();
        }).padTop(4).row();
    }

    private Cell<CheckBox> addCheck(Table table, String text, boolean checked, Boolc listener) {
        CheckBox cb = new CheckBox(text);
        cb.setChecked(checked);
        cb.changed(() -> listener.get(cb.isChecked()));
        return table.add(cb).left();
    }
}
