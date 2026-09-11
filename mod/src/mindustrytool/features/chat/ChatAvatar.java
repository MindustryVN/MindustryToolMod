package mindustrytool.features.chat;

import static solim.UI.*;

import arc.graphics.Color;
import arc.scene.Element;
import arc.util.Nullable;
import solim.core.BaseComponent;
import solim.display.Badge;
import solim.layout.SolimStack;
import solim.signal.Readable;

/**
 * Fixed-size user avatar: network photo on top of a deterministic initial-letter fallback.
 * The fallback square keeps a stable per-user color so a user is recognizable before (or without)
 * their photo loading.
 */
public class ChatAvatar extends BaseComponent {

    private static final Color[] PALETTE = {
            Color.valueOf("ef5350"),
            Color.valueOf("ff9800"),
            Color.valueOf("9ccc65"),
            Color.valueOf("26a69a"),
            Color.valueOf("29b6f6"),
            Color.valueOf("7e57c2"),
            Color.valueOf("ec407a"),
            Color.valueOf("9e9d24")
    };

    private final Readable<String> displayName;
    private final Readable<String> avatarUrl;
    private final @Nullable String colorKey;
    private final float size;

    public ChatAvatar(String displayName, String avatarUrl, @Nullable String colorKey, float size) {
        this(Readable.of(displayName), Readable.of(avatarUrl), colorKey, size);
    }

    public ChatAvatar(Readable<String> displayName, Readable<String> avatarUrl, @Nullable String colorKey,
            float size) {
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
        this.colorKey = colorKey;
        this.size = size;
    }

    @Override
    protected Element build() {
        Readable<String> initial = displayName.map(ChatAvatar::initialOf);
        Color background = colorFor(colorKey);

        SolimStack stack = new SolimStack()
                .layer(() -> {
                    Badge fallback = badge(initial);
                    fallback.color(background);
                    fallback.textColor(Color.white);
                    fallback.text().fontScale(size >= 32f ? 1.2f : 0.9f);
                    fallback.size(size, size);
                })
                .layer(() -> networkImage(avatarUrl).size(size, size).top().left());
        component(stack);
        return stack.element();
    }

    static String initialOf(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "?";
        }
        String trimmed = name.trim();
        int codePoint = trimmed.codePointAt(0);
        return new String(Character.toChars(codePoint)).toUpperCase();
    }

    static Color colorFor(@Nullable String key) {
        int hash = (key != null ? key.hashCode() : 0) & 0x7fffffff;
        return PALETTE[hash % PALETTE.length];
    }
}
