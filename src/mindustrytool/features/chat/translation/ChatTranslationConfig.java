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

    public static final String DEVX_TIMEOUT = "mindustrytool.chat-translation.devx.timeout";
    public static final String DEVX_MAX_HISTORY = "mindustrytool.chat-translation.devx.max-history";

    public static final String MINDUSTRYTOOL_TIMEOUT = "mindustrytool.chat-translation.mindustrytool.timeout";

    public static final String AUTO_TRANSLATE_INCOMING = PREFIX + "auto.incoming";
    public static final String REVERSE_ENABLED = PREFIX + "reverse.enabled";
    public static final String REVERSE_INCLUDE_ORIGINAL = PREFIX + "reverse.include-original";
    public static final String REVERSE_TARGET_LANG = PREFIX + "reverse.target-language";

    public static String getProviderId() {
        return Core.settings.getString(PROVIDER, "devx");
    }

    public static void setProviderId(String id) {
        Core.settings.put(PROVIDER, id);
    }

    public static boolean isAutoTranslateIncoming() {
        return Core.settings.getBool(AUTO_TRANSLATE_INCOMING, true);
    }

    public static void setAutoTranslateIncoming(boolean enabled) {
        Core.settings.put(AUTO_TRANSLATE_INCOMING, enabled);
    }

    public static boolean isReverseEnabled() {
        return Core.settings.getBool(REVERSE_ENABLED, false);
    }

    public static void setReverseEnabled(boolean enabled) {
        Core.settings.put(REVERSE_ENABLED, enabled);
    }

    public static boolean isReverseIncludeOriginal() {
        return Core.settings.getBool(REVERSE_INCLUDE_ORIGINAL, false);
    }

    public static void setReverseIncludeOriginal(boolean include) {
        Core.settings.put(REVERSE_INCLUDE_ORIGINAL, include);
    }

    public static String getReverseTargetLang() {
        return Core.settings.getString(REVERSE_TARGET_LANG, "en");
    }

    public static void setReverseTargetLang(String lang) {
        Core.settings.put(REVERSE_TARGET_LANG, lang);
    }
}
