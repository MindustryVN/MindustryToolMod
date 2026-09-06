package solim.style;

import arc.graphics.Color;

/**
 * Predefined style constants.
 */
public final class Styles {
    public static final Style PRIMARY = Style.builder()
        .name("primary")
        .foreground(Color.white)
        .background(Color.valueOf("4757e6"))
        .pad(8f)
        .primary()
        .build();

    public static final Style GHOST = Style.builder()
        .name("ghost")
        .foreground(Color.white)
        .background(Color.clear)
        .pad(8f)
        .ghost()
        .build();

    public static final Style BLACK6 = Style.builder()
        .name("black6")
        .foreground(Color.white)
        .background(new Color(0f, 0f, 0f, 0.6f))
        .pad(8f)
        .build();

    private Styles() {}
}
