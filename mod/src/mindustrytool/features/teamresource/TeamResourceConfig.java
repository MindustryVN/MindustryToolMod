package mindustrytool.features.teamresource;

import arc.Core;
import arc.scene.ui.layout.Scl;

public class TeamResourceConfig {
    private static final String PREFIX = "mindustrytool.team-resource.";

    public static float x() {
        float sw = Core.scene != null ? Core.scene.getWidth() : Core.graphics.getWidth() / Scl.scl();
        return Core.settings.getFloat(PREFIX + "x", sw / 2f - sw * overlayWidth() / 2f);
    }

    public static void x(float value) {
        Core.settings.put(PREFIX + "x", value);
        Core.settings.forceSave();
    }

    public static float y() {
        float sh = Core.scene != null ? Core.scene.getHeight() : Core.graphics.getHeight() / Scl.scl();
        return Core.settings.getFloat(PREFIX + "y", sh / 2f);
    }

    public static void y(float value) {
        Core.settings.put(PREFIX + "y", value);
        Core.settings.forceSave();
    }

    public static float opacity() {
        return Core.settings.getFloat(PREFIX + "opacity", 1f);
    }

    public static void opacity(float value) {
        Core.settings.put(PREFIX + "opacity", value);
        Core.settings.forceSave();
    }

    public static float scale() {
        return Core.settings.getFloat(PREFIX + "scale", 1f);
    }

    public static void scale(float value) {
        Core.settings.put(PREFIX + "scale", value);
        Core.settings.forceSave();
    }

    public static float overlayWidth() {
        return Core.settings.getFloat(PREFIX + "overlay-width", 0.28f);
    }

    public static void overlayWidth(float value) {
        Core.settings.put(PREFIX + "overlay-width", value);
        Core.settings.forceSave();
    }

    public static float overlayHeight() {
        return Core.settings.getFloat(PREFIX + "overlay-height", 0.60f);
    }

    public static void overlayHeight(float value) {
        Core.settings.put(PREFIX + "overlay-height", value);
        Core.settings.forceSave();
    }

    public static boolean showItems() {
        return Core.settings.getBool(PREFIX + "show-items", true);
    }

    public static void showItems(boolean value) {
        Core.settings.put(PREFIX + "show-items", value);
        Core.settings.forceSave();
    }

    public static boolean showUnits() {
        return Core.settings.getBool(PREFIX + "show-units", false);
    }

    public static void showUnits(boolean value) {
        Core.settings.put(PREFIX + "show-units", value);
        Core.settings.forceSave();
    }

    public static boolean showPower() {
        return Core.settings.getBool(PREFIX + "show-power", true);
    }

    public static void showPower(boolean value) {
        Core.settings.put(PREFIX + "show-power", value);
        Core.settings.forceSave();
    }

    public static boolean showStoredPower() {
        return Core.settings.getBool(PREFIX + "show-stored-power", false);
    }

    public static void showStoredPower(boolean value) {
        Core.settings.put(PREFIX + "show-stored-power", value);
        Core.settings.forceSave();
    }

    public static boolean hideBackground() {
        return Core.settings.getBool(PREFIX + "hide-background", false);
    }

    public static void hideBackground(boolean value) {
        Core.settings.put(PREFIX + "hide-background", value);
        Core.settings.forceSave();
    }

    public static boolean alwaysShowFlowRate() {
        return Core.settings.getBool(PREFIX + "always-show-flow-rate", true);
    }

    public static void alwaysShowFlowRate(boolean value) {
        Core.settings.put(PREFIX + "always-show-flow-rate", value);
        Core.settings.forceSave();
    }

    public static boolean isExpanded() {
        return Core.settings.getBool(PREFIX + "expanded", true);
    }

    public static void isExpanded(boolean value) {
        Core.settings.put(PREFIX + "expanded", value);
        Core.settings.forceSave();
    }

    public static void resetToDefaults() {
        opacity(1f);
        scale(1f);
        overlayWidth(0.28f);
        overlayHeight(0.60f);
        showItems(true);
        showUnits(false);
        showPower(true);
        showStoredPower(false);
        hideBackground(false);
        alwaysShowFlowRate(true);
        isExpanded(true);
        Core.settings.remove(PREFIX + "x");
        Core.settings.remove(PREFIX + "y");
        Core.settings.forceSave();
    }
}
