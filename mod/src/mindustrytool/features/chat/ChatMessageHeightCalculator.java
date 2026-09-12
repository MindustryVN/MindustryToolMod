package mindustrytool.features.chat;

import arc.graphics.Color;
import arc.graphics.g2d.Font;
import arc.graphics.g2d.GlyphLayout;
import arc.util.Align;
import arc.util.Nullable;
import arc.util.pooling.Pools;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import mindustry.ui.Fonts;
import mindustrytool.features.chat.models.GroupedMessageItem;
import mindustrytool.features.chat.models.ParsedChatMessage;
import mindustrytool.features.chat.models.ParsedChatMessage.ImageMessage;
import mindustrytool.features.chat.models.ParsedChatMessage.MindustryToolLinkMessage;
import mindustrytool.features.chat.models.ParsedChatMessage.RoomInviteMessage;
import mindustrytool.features.chat.models.ParsedChatMessage.SchematicMessage;
import mindustrytool.features.chat.models.ParsedChatMessage.TextMessage;

/**
 * Calculates and caches layout heights for chat message items given a container width.
 *
 * <p>Uses static heights for fixed layout structures (headers, avatars, cards, buttons)
 * and calculates wrapped text height using {@link GlyphLayout}.
 */
public final class ChatMessageHeightCalculator {

    // Fixed layout unit constants (matching UI.unit(1) = 4f)
    public static final float UNIT_1 = 4f;
    public static final float AVATAR_SIZE = 48f; // unit(12)
    public static final float AVATAR_GAP = 6f;   // unit(1.5f)
    public static final float HEADER_HEIGHT = 22f;
    public static final float REPLY_PREVIEW_HEIGHT = 24f;
    public static final float SCHEMATIC_CARD_HEIGHT = 180f;
    public static final float IMAGE_CARD_HEIGHT = 140f;
    public static final float INVITE_CARD_HEIGHT = 80f;
    public static final float TOOL_LINK_CARD_HEIGHT = 70f;
    public static final float HORIZONTAL_PADDINGS = 64f; // Left avatar gutter + right margin

    private static final Map<String, Float> HEIGHT_CACHE = new ConcurrentHashMap<>();

    private ChatMessageHeightCalculator() {
    }

    /**
     * Calculates the height for a grouped message item at the specified container width.
     */
    public static float calculateHeight(GroupedMessageItem item, float containerWidth) {
        if (item == null || item.getMessage() == null) {
            return 24f;
        }

        int widthKey = (int) Math.max(100f, containerWidth);
        String cacheKey = item.getId() + "_" + item.isFirstInGroup() + "_" + widthKey;
        Float cached = HEIGHT_CACHE.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        float height = computeHeight(item, widthKey);
        HEIGHT_CACHE.put(cacheKey, height);
        return height;
    }

    private static float computeHeight(GroupedMessageItem item, float containerWidth) {
        ParsedChatMessage msg = item.getMessage();
        boolean isFirst = item.isFirstInGroup();

        float verticalPadding = isFirst ? (UNIT_1 + UNIT_1) : (UNIT_1 * 1.5f);
        float contentHeight = 0f;

        // 1. Author and timestamp header
        if (isFirst) {
            contentHeight += HEADER_HEIGHT;
        }

        // 2. Reply preview row
        if (msg.getReplyTo() != null && !msg.getReplyTo().isEmpty()) {
            contentHeight += REPLY_PREVIEW_HEIGHT;
        }

        // 3. Body height by type
        float availableTextWidth = Math.max(100f, containerWidth - HORIZONTAL_PADDINGS);

        if (msg instanceof TextMessage) {
            TextMessage txt = (TextMessage) msg;
            contentHeight += measureTextHeight(txt.getText(), availableTextWidth, 0.95f);
        } else if (msg instanceof SchematicMessage) {
            SchematicMessage schem = (SchematicMessage) msg;
            contentHeight += SCHEMATIC_CARD_HEIGHT;
            if (schem.getPrefixText() != null && !schem.getPrefixText().isEmpty()) {
                contentHeight += measureTextHeight(schem.getPrefixText(), availableTextWidth, 0.95f);
            }
            if (schem.getSuffixText() != null && !schem.getSuffixText().isEmpty()) {
                contentHeight += measureTextHeight(schem.getSuffixText(), availableTextWidth, 0.95f);
            }
        } else if (msg instanceof ImageMessage) {
            contentHeight += IMAGE_CARD_HEIGHT;
        } else if (msg instanceof RoomInviteMessage) {
            contentHeight += INVITE_CARD_HEIGHT;
        } else if (msg instanceof MindustryToolLinkMessage) {
            contentHeight += TOOL_LINK_CARD_HEIGHT;
        } else {
            contentHeight += 24f;
        }

        // Total height must account for avatar height if it's the first message in the group
        float total = verticalPadding + contentHeight;
        if (isFirst) {
            total = Math.max(AVATAR_SIZE + verticalPadding, total);
        }
        return Math.max(24f, total);
    }

    /**
     * Measures the height of wrapped text using Arc's {@link GlyphLayout}.
     */
    public static float measureTextHeight(@Nullable String text, float wrapWidth, float fontScale) {
        if (text == null || text.trim().isEmpty()) {
            return 16f;
        }

        Font font = null;
        try {
            font = Fonts.def;
        } catch (Throwable ignored) {
        }

        if (font != null) {
            try {
                GlyphLayout layout = Pools.obtain(GlyphLayout.class, GlyphLayout::new);
                float oldScaleX = font.getScaleX();
                float oldScaleY = font.getScaleY();
                font.getData().setScale(oldScaleX * fontScale, oldScaleY * fontScale);

                layout.setText(font, text, Color.white, wrapWidth, Align.left, true);
                float h = layout.height;

                font.getData().setScale(oldScaleX, oldScaleY);
                Pools.free(layout);
                return Math.max(16f, h + 4f);
            } catch (Throwable ignored) {
            }
        }

        // Fallback for headless tests where Fonts.def is not initialized
        float charsPerLine = Math.max(10f, wrapWidth / (8f * fontScale));
        int estimatedLines = (int) Math.ceil(text.length() / charsPerLine);
        return Math.max(16f, estimatedLines * (16f * fontScale));
    }

    public static void clearCache() {
        HEIGHT_CACHE.clear();
    }
}
