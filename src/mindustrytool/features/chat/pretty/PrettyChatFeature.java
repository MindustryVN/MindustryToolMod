package mindustrytool.features.chat.pretty;

import java.util.Optional;

import arc.Core;
import arc.Events;
import arc.input.KeyCode;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.ui.Dialog;
import arc.scene.ui.TextField;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Reflect;
import lombok.AllArgsConstructor;
import lombok.Getter;
import mindustry.Vars;
import mindustry.game.EventType.Trigger;
import mindustry.gen.Icon;
import mindustrytool.features.Feature;
import mindustrytool.features.FeatureMetadata;

public class PrettyChatFeature implements Feature {

    @Getter
    private static final Seq<Prettier> prettiers = new Seq<>();

    private TextField lastHookedField = null;

    static {
        prettiers.add(new Prettier("default", "Default", "<message>"));
        prettiers.add(new Prettier("uwu", "UwUifier", "<message>.replace(/r/g,'w').replace(/l/g,'w') + ' uwu'"));
        prettiers.add(new Prettier("caps", "CAPS LOCK", "<message>.toUpperCase()"));
        prettiers.add(new Prettier("lowercase", "lowercase", "<message>.toLowerCase()"));
        prettiers.add(new Prettier("reverse", "esreveR", "<message>.split('').reverse().join('')"));
        prettiers.add(new Prettier("rainbow", "rainbow", "rainbow(<message>)"));
    }

    @Override
    public FeatureMetadata getMetadata() {
        return FeatureMetadata.builder()
                .name("@feature.prettychat")
                .description("@feature.prettychat.description")
                .icon(Icon.chat)
                .quickAccess(true)
                .build();
    }

    @Override
    public void init() {
        Events.run(Trigger.update, this::hookChatField);
    }

    private void hookChatField() {
        if (!isEnabled() || Vars.ui == null || Vars.ui.chatfrag == null) return;

        try {
            TextField chatfield = Reflect.get(Vars.ui.chatfrag, "chatfield");
            if (chatfield == null || chatfield == lastHookedField) return;

            lastHookedField = chatfield;
            chatfield.addListener(new InputListener() {
                @Override
                public boolean keyDown(InputEvent event, KeyCode keycode) {
                    if (keycode == KeyCode.enter && !Core.input.keyDown(KeyCode.shiftLeft)) {
                        applyPrettifiersToChatField(chatfield);
                    }
                    return false;
                }
            });
        } catch (Exception e) {
            Log.err("Failed to hook chatfield for PrettyChat", e);
        }
    }

    private void applyPrettifiersToChatField(TextField chatfield) {
        if (!isEnabled() || chatfield == null) return;

        String raw = chatfield.getText();
        if (raw == null || raw.trim().isEmpty()) return;

        String formatted = transform(raw);
        if (formatted != null && !formatted.equals(raw)) {
            chatfield.setText(formatted.substring(0, Math.min(Vars.maxTextLength, formatted.length())));
        }
    }

    @Override
    public Optional<Dialog> setting() {
        return Optional.of(new PrettyChatSettingsDialog(this));
    }

    public String transform(String message) {
        if (!isEnabled() || message == null || message.trim().isEmpty()) {
            return message;
        }

        String commandPrefix = "";
        String content = message;

        if (message.startsWith("/")) {
            int spaceIndex = message.indexOf(' ');
            int subIndex = spaceIndex == -1 ? message.length() : spaceIndex;
            String cmd = message.substring(0, subIndex);

            if (!cmd.equals("/t") && !cmd.equals("/a")) {
                return message;
            }
            commandPrefix = cmd + " ";
            content = message.substring(subIndex).trim();
        }

        String result = content;
        for (String id : PrettyChatConfig.getEnabledIds()) {
            Prettier p = prettiers.find(x -> x.id.equals(id));
            if (p != null) {
                result = transform(result, p);
            }
        }

        return (commandPrefix + result).trim();
    }

    public static String transform(String message, Prettier prettier) {
        if (message == null || message.isEmpty()) return message;

        switch (prettier.getId()) {
            case "default":
                return message;
            case "uwu":
                return applyUwU(message);
            case "caps":
                return message.toUpperCase();
            case "lowercase":
                return message.toLowerCase();
            case "reverse":
                return new StringBuilder(message).reverse().toString();
            case "rainbow":
                return applyRainbow(message);
            default:
                return applyCustomScript(message, prettier);
        }
    }

    private static String applyUwU(String message) {
        return message.replaceAll("[rl]", "w")
                .replaceAll("[RL]", "W")
                .replaceAll("ove", "uv")
                .replaceAll("OVE", "UV") + " uwu";
    }

    private static String applyRainbow(String message) {
        String[] colors = {"red", "orange", "yellow", "green", "cyan", "blue", "purple"};
        StringBuilder sb = new StringBuilder();
        String[] words = message.split(" ");
        for (int j = 0; j < words.length; j++) {
            if (j > 0) sb.append(" ");
            String word = words[j];
            for (int i = 0; i < word.length(); i++) {
                sb.append("[").append(colors[(j + i) % colors.length]).append("]").append(word.charAt(i));
            }
        }
        return sb.append("[]").toString();
    }

    private static String applyCustomScript(String message, Prettier prettier) {
        try {
            String script = prettier.getScript();
            if (script == null || script.trim().isEmpty()) return message;

            String escaped = message.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "");
            String result = Vars.mods.getScripts().runConsole(script.replace("<message>", '"' + escaped + '"'));
            return result != null ? result : message;
        } catch (Exception e) {
            Log.err("PrettyChat transform failed for " + prettier.getId(), e);
            return message;
        }
    }

    @Getter
    @AllArgsConstructor
    public static class Prettier {
        private String id;
        private String name;
        private String defaultScript;

        public String getScript() {
            return PrettyChatConfig.getScript(id, defaultScript);
        }
    }
}
