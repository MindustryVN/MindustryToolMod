package solim.style;

import arc.graphics.Color;

/**
 * Immutable style definition. No CSS, no diffing.
 * Widgets apply the whole Style on change.
 */
public final class Style {
    public static final Style DEFAULT = new Style("default", Color.white, Color.darkGray, 4f, false, true);

    private final String name;
    private final Color foreground;
    private final Color background;
    private final float padding;
    private final boolean ghost;
    private final boolean primary;

    private Style(String name, Color foreground, Color background, float padding, boolean ghost, boolean primary) {
        this.name = name;
        this.foreground = foreground;
        this.background = background;
        this.padding = padding;
        this.ghost = ghost;
        this.primary = primary;
    }

    public String name() { return name; }
    public Color foreground() { return foreground; }
    public Color background() { return background; }
    public float padding() { return padding; }
    public boolean ghost() { return ghost; }
    public boolean primary() { return primary; }

    public Style withPad(float pad) {
        return new Style(name, foreground, background, pad, ghost, primary);
    }

    public Style withForeground(Color c) {
        return new Style(name, c, background, padding, ghost, primary);
    }

    public Style withBackground(Color c) {
        return new Style(name, foreground, c, padding, ghost, primary);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String name = "custom";
        private Color fg = Color.white;
        private Color bg = Color.darkGray;
        private float pad = 4f;
        private boolean ghost = false;
        private boolean primary = false;

        public Builder name(String n) { this.name = n; return this; }
        public Builder foreground(Color c) { this.fg = c; return this; }
        public Builder background(Color c) { this.bg = c; return this; }
        public Builder pad(float p) { this.pad = p; return this; }
        public Builder ghost() { this.ghost = true; this.primary = false; return this; }
        public Builder primary() { this.primary = true; this.ghost = false; return this; }
        public Builder from(Style s) {
            this.name = s.name;
            this.fg = s.foreground;
            this.bg = s.background;
            this.pad = s.padding;
            this.ghost = s.ghost;
            this.primary = s.primary;
            return this;
        }
        public Style build() {
            return new Style(name, fg, bg, pad, ghost, primary);
        }
    }
}
