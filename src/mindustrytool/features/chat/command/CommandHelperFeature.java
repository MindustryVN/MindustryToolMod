package mindustrytool.features.chat.command;

import arc.Core;
import arc.Events;
import arc.input.KeyCode;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.ui.Button;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Table;
import arc.util.Log;
import arc.util.Reflect;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.game.EventType.Trigger;
import mindustry.gen.Icon;
import mindustrytool.features.Feature;
import mindustrytool.features.FeatureMetadata;
import mindustrytool.features.chat.command.ui.CommandSuggestionOverlay;

public class CommandHelperFeature implements Feature {
    private static CommandHelperFeature instance;

    public static boolean isFeatureEnabled() {
        return instance != null && instance.isEnabled();
    }

    private CommandSuggestionOverlay overlay;
    private TextField lastHookedField = null;

    public CommandHelperFeature() {
        instance = this;
    }

    @Override
    public FeatureMetadata getMetadata() {
        return FeatureMetadata.builder()
                .name("@feature.command-helper")
                .description("@feature.command-helper.description")
                .icon(Icon.terminal)
                .order(5)
                .enabledByDefault(true)
                .quickAccess(true)
                .build();
    }

    @Override
    public String getSettingKey() {
        return "mindustrytool.command-helper.enabled";
    }

    @Override
    public boolean isEnabled() {
        if (Core.settings.has("mindustrytool.command-helper.enabled")) {
            return Core.settings.getBool("mindustrytool.command-helper.enabled", true);
        }
        if (Core.settings.has("mindustrytool.@feature.command-helper.enabled")) {
            return Core.settings.getBool("mindustrytool.@feature.command-helper.enabled", true);
        }
        return true;
    }

    @Override
    public void init() {
        overlay = new CommandSuggestionOverlay();
        Events.run(Trigger.update, this::update);
        Events.on(EventType.PlayerChatEvent.class, e -> {
            if (e.message != null) {
                CommandRegistry.getInstance().learnFromServerMessage(e.message);
            }
        });
    }

    @Override
    public void onEnable() {
        ensureOverlayMounted();
    }

    @Override
    public void onDisable() {
        if (overlay != null) {
            overlay.setShown(false);
        }
    }

    private void update() {
        if (!isEnabled()) {
            if (overlay != null && overlay.isShown()) {
                overlay.setShown(false);
            }
            return;
        }

        ensureOverlayMounted();
        hookChatFragment();
    }

    private void ensureOverlayMounted() {
        if (overlay != null && Core.scene != null && overlay.getScene() == null) {
            Core.scene.add(overlay);
            overlay.toFront();
        }
    }

    private void hookChatFragment() {
        if (Vars.ui == null || Vars.ui.chatfrag == null) return;

        try {
            TextField chatfield = Reflect.get(Vars.ui.chatfrag, "chatfield");
            if (chatfield == null || chatfield == lastHookedField) return;

            lastHookedField = chatfield;
            chatfield.addCaptureListener(createChatfieldListener(chatfield));

            Table container = Reflect.get(Vars.ui.chatfrag, "container");
            if (container != null) {
                container.addCaptureListener(createContainerListener(chatfield));
            }
        } catch (Exception e) {
            Log.err("Failed to hook chatfield for CommandHelperFeature", e);
        }
    }

    private InputListener createChatfieldListener(TextField chatfield) {
        return new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, KeyCode keycode) {
                return handleChatKeyDown(event, keycode, chatfield);
            }
        };
    }

    private boolean handleChatKeyDown(InputEvent event, KeyCode keycode, TextField chatfield) {
        if (!isEnabled()) return false;

        if (overlay != null && overlay.hasSuggestions()) {
            if (handleOverlayKey(event, keycode)) return true;
        }

        if (keycode == KeyCode.enter && handlePossibleClientCommand(chatfield)) {
            event.stop();
            event.cancel();
            return true;
        }

        return false;
    }

    private boolean handleOverlayKey(InputEvent event, KeyCode keycode) {
        if (keycode == KeyCode.tab) {
            overlay.applyCurrentSelection();
            event.stop();
            event.cancel();
            return true;
        }
        if (keycode == KeyCode.down) {
            overlay.selectNext();
            event.stop();
            event.cancel();
            return true;
        }
        if (keycode == KeyCode.up) {
            overlay.selectPrev();
            event.stop();
            event.cancel();
            return true;
        }
        if (keycode == KeyCode.escape) {
            overlay.setShown(false);
        }
        return false;
    }

    private InputListener createContainerListener(TextField chatfield) {
        return new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
                if (!isEnabled()) return false;
                boolean isButtonClicked = event.targetActor instanceof Button
                        || (event.targetActor != null && event.targetActor.parent instanceof Button);

                if (isButtonClicked && handlePossibleClientCommand(chatfield)) {
                    event.stop();
                    event.cancel();
                    return true;
                }
                return false;
            }
        };
    }

    private boolean handlePossibleClientCommand(TextField chatfield) {
        String commandText = extractCommandText(chatfield.getText());
        if (commandText == null) return false;

        String[] tokens = commandText.substring(1).split("\\s+", 2);
        String cmdName = tokens[0];
        String[] args = tokens.length > 1 && !tokens[1].trim().isEmpty() ? tokens[1].trim().split("\\s+") : new String[0];

        ChatCommand cmd = CommandRegistry.getInstance().find(cmdName);
        if (cmd == null || !cmd.isClientOnly || cmd.action == null) return false;

        executeClientCommand(cmd, cmdName, args);
        resetChatState(chatfield);
        return true;
    }

    private String extractCommandText(String raw) {
        if (raw == null) return null;
        String text = raw.trim();
        if (text.startsWith("/t /") || text.startsWith("/a /")) {
            text = text.substring(3).trim();
        }
        return text.startsWith("/") ? text : null;
    }

    private void executeClientCommand(ChatCommand cmd, String cmdName, String[] args) {
        try {
            cmd.action.get(args);
        } catch (Exception e) {
            Log.err("Error executing client command /" + cmdName, e);
            if (Vars.ui != null && Vars.ui.chatfrag != null) {
                Vars.ui.chatfrag.addMessage("[scarlet]Lỗi thực thi lệnh: " + e.getMessage() + "[]");
            }
        }
    }

    private void resetChatState(TextField chatfield) {
        chatfield.setText("");
        if (Vars.ui != null && Vars.ui.chatfrag != null) {
            Vars.ui.chatfrag.clearChatInput();
            Vars.ui.chatfrag.hide();
        }
        if (overlay != null) {
            overlay.setShown(false);
        }
    }
}
