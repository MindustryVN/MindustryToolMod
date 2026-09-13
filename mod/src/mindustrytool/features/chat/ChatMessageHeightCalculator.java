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
import mindustrytool.features.chat.models.MessageGroup;
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
    public static final float HEADER_HEIGHT = 24f; // unit(6) ellipsis button and title row
    public static final float REPLY_PREVIEW_HEIGHT = 24f;
    public static final float SCHEMATIC_CARD_HEIGHT = 180f;
    public static final float IMAGE_CARD_HEIGHT = 140f;
    public static final float INVITE_CARD_HEIGHT = 80f;
    public static final float TOOL_LINK_CARD_HEIGHT = 70f;
    public static final float HORIZONTAL_PADDINGS = 82f; // 48px avatar + 6px gap + 8px card padding + 4px inner gap + 16px scrollbar & gutter allowance
    public static final float MESSAGE_GAP = 3f; // unit(0.75f): gap between messages in a group
    public static final float HEADER_GAP = 2f; // unit(0.5f): gap between group header and first message
    public static final float MESSAGE_CARD_PADDING = 8f; // vertical padding for message item card
    public static final float FONT_SCALE = 1.0f;

    private static final Map<String, Float> HEIGHT_CACHE = new ConcurrentHashMap<>();

    private ChatMessageHeightCalculator() {
    }

    /**
     * Calculates the height for a message group at the specified container width.
     */
    public static float calculateHeight(MessageGroup group, float containerWidth) {
        if (group == null || group.getMessageCount() == 0) {
            return 24f;
        }

        int widthKey = (int) Math.max(100f, containerWidth);
        String cacheKey = group.getKey() + "_" + widthKey;
        Float cached = HEIGHT_CACHE.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        float height = computeHeight(group, widthKey);
        HEIGHT_CACHE.put(cacheKey, height);
        return height;
    }

    private static float computeHeight(MessageGroup group, float containerWidth) {
        float verticalPadding = UNIT_1 + UNIT_1;
        float availableTextWidth = Math.max(100f, containerWidth - HORIZONTAL_PADDINGS);

        float messagesHeight = 0f;
        for (ParsedChatMessage msg : group.getMessages()) {
            messagesHeight += measureMessageHeight(msg, availableTextWidth);
        }
        if (group.getMessageCount() > 1) {
            messagesHeight += MESSAGE_GAP * (group.getMessageCount() - 1);
        }

        float rightColumnHeight = HEADER_HEIGHT + HEADER_GAP + messagesHeight;
        float total = Math.max(AVATAR_SIZE, rightColumnHeight) + verticalPadding;
        return Math.max(24f, total);
    }

    private static float measureMessageHeight(ParsedChatMessage msg, float availableTextWidth) {
        float height = MESSAGE_CARD_PADDING;

        // Reply preview row
        if (msg.getReplyTo() != null && !msg.getReplyTo().isEmpty()) {
            height += REPLY_PREVIEW_HEIGHT;
        }

        // Body height by type
        if (msg instanceof TextMessage) {
            TextMessage txt = (TextMessage) msg;
            height += measureTextHeight(txt.getText(), availableTextWidth, FONT_SCALE);
        } else if (msg instanceof SchematicMessage) {
            SchematicMessage schem = (SchematicMessage) msg;
            height += SCHEMATIC_CARD_HEIGHT;
            if (schem.getPrefixText() != null && !schem.getPrefixText().isEmpty()) {
                height += measureTextHeight(schem.getPrefixText(), availableTextWidth, FONT_SCALE);
            }
            if (schem.getSuffixText() != null && !schem.getSuffixText().isEmpty()) {
                height += measureTextHeight(schem.getSuffixText(), availableTextWidth, FONT_SCALE);
            }
        } else if (msg instanceof ImageMessage) {
            height += IMAGE_CARD_HEIGHT;
        } else if (msg instanceof RoomInviteMessage) {
            height += INVITE_CARD_HEIGHT;
        } else if (msg instanceof MindustryToolLinkMessage) {
            height += TOOL_LINK_CARD_HEIGHT;
        } else {
            height += 24f;
        }
        return height;
    }

    /**
     * Measures the height of wrapped text using Arc's {@link GlyphLayout}.
     */
    public static float measureTextHeight(@Nullable String text, float wrapWidth, float fontScale) {
        if (text == null || text.trim().isEmpty()) {
            return 18f * fontScale;
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
                // Match Arc's Label.getPrefHeight() which accounts for line descent spacing (-2 * descent)
                float descentCorrection = -font.getDescent() * 2f;

                font.getData().setScale(oldScaleX, oldScaleY);
                Pools.free(layout);
                return Math.max(18f * fontScale, h + descentCorrection);
            } catch (Throwable ignored) {
            }
        }

        // Fallback for headless tests where Fonts.def is not initialized
        float charsPerLine = Math.max(10f, wrapWidth / (9f * fontScale));
        int estimatedLines = (int) Math.ceil(text.length() / charsPerLine);
        return Math.max(18f * fontScale, estimatedLines * (20f * fontScale));
    }

    public static void clearCache() {
        HEIGHT_CACHE.clear();
    }
}
