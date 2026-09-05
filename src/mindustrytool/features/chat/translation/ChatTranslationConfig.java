package mindustrytool.features.chat.translation;

import arc.Core;

public class ChatTranslationConfig {
    private static final String PREFIX = "mindustrytool.chat-translation.";
    public static final String SHOW_ORIGINAL = PREFIX + "show.original";
    public static final String PROVIDER = PREFIX + "provider";

    public static final String GEMINI_API_KEY = "mindustrytool.chat-translation.gemini.api-key";
    public static final String GEMINI_MODEL = "mindustrytool.chat-translation.gemini.model";
    public static final String GEMINI_TIMEOUT = "mindustrytool.chat-translation.gemini.timeout";
    public static final String GEMINI_MAX_HISTORY = "mindustrytool.chat-translation.gemini.max-history";

    public static final String DEEPL_API_KEY = "mindustrytool.chat-translation.deepl.api-key";
    public static final String DEEPL_TIMEOUT = "mindustrytool.chat-translation.deepl.timeout";

    public static final String DEVX_API_KEY = "mindustrytool.chat-translation.devx.api-key";
    public static final String DEVX_TIMEOUT = "mindustrytool.chat-translation.devx.timeout";

    public static final String MINDUSTRYTOOL_TIMEOUT = "mindustrytool.chat-translation.mindustrytool.timeout";

    public static final String TRANSLATE_OUTGOING = PREFIX + "outgoing.enabled";
    public static final String OUTGOING_TARGET_LANG = PREFIX + "outgoing.target-lang";
    public static final String OUTGOING_FORMAT = PREFIX + "outgoing.format";

    public static String getProviderId() {
        return Core.settings.getString(PROVIDER, "devx");
    }

    public static void setProviderId(String id) {
        Core.settings.put(PROVIDER, id);
    }

    public static boolean isTranslateOutgoing() {
        return Core.settings.getBool(TRANSLATE_OUTGOING, false);
    }

    public static void setTranslateOutgoing(boolean enabled) {
        Core.settings.put(TRANSLATE_OUTGOING, enabled);
    }

    public static String getOutgoingTargetLang() {
        return Core.settings.getString(OUTGOING_TARGET_LANG, "English");
    }

    public static void setOutgoingTargetLang(String lang) {
        Core.settings.put(OUTGOING_TARGET_LANG, lang);
    }

    public static String getOutgoingFormat() {
        return Core.settings.getString(OUTGOING_FORMAT, "translated_only");
    }

    public static void setOutgoingFormat(String format) {
        Core.settings.put(OUTGOING_FORMAT, format);
    }
}
