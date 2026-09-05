package mindustrytool.features.chat.command.ui;

import arc.math.geom.Vec2;
import arc.scene.event.Touchable;
import arc.scene.ui.Label;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Reflect;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.ui.Styles;
import mindustrytool.features.chat.command.ChatCommand;
import mindustrytool.features.chat.command.CommandHelperFeature;
import mindustrytool.features.chat.command.CommandRegistry;

public class CommandSuggestionOverlay extends Table {
    private static final int MAX_VISIBLE_SUGGESTIONS = 7;
    private static final float MIN_OVERLAY_WIDTH = 420f;

    public static class SuggestionItem {
        public final String completionText;
        public final String displayText;
        public final String paramSyntax;
        public final String description;
        public final boolean isCommand;

        public SuggestionItem(String completionText, String displayText, String paramSyntax, String description, boolean isCommand) {
            this.completionText = completionText;
            this.displayText = displayText;
            this.paramSyntax = paramSyntax == null ? "" : paramSyntax;
            this.description = description == null ? "" : description;
            this.isCommand = isCommand;
        }
    }

    private final Seq<SuggestionItem> items = new Seq<>();
    private int selectedIndex = 0;
    private String lastEvaluatedText = "";
    private TextField targetField = null;

    public CommandSuggestionOverlay() {
        touchable = Touchable.enabled;
        this.visible = false;
    }

    public boolean isShown() {
        return this.visible;
    }

    public void setShown(boolean show) {
        this.visible = show;
    }

    public boolean hasSuggestions() {
        return isShown() && !items.isEmpty();
    }

    public void selectNext() {
        if (items.isEmpty()) return;
        selectedIndex = (selectedIndex + 1) % items.size;
        rebuildUI();
    }

    public void selectPrev() {
        if (items.isEmpty()) return;
        selectedIndex = (selectedIndex - 1 + items.size) % items.size;
        rebuildUI();
    }

    public void applyCurrentSelection() {
        if (items.isEmpty() || selectedIndex < 0 || selectedIndex >= items.size) return;
        applySuggestion(items.get(selectedIndex));
    }

    public void applySuggestion(SuggestionItem item) {
        if (targetField == null || item == null) return;
        targetField.setText(item.completionText);
        targetField.setCursorPosition(targetField.getText().length());
        lastEvaluatedText = item.completionText;
        updateSuggestions(item.completionText);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (!shouldShowOverlay()) {
            hideOverlay();
            return;
        }

        if (targetField == null || targetField.getScene() == null) {
            targetField = resolveChatField();
        }

        if (targetField == null) {
            hideOverlay();
            return;
        }

        String raw = targetField.getText();
        if (!isValidCommandInput(raw)) {
            hideOverlay();
            lastEvaluatedText = "";
            return;
        }

        if (!raw.equals(lastEvaluatedText)) {
            lastEvaluatedText = raw;
            updateSuggestions(raw);
        }

        if (items.isEmpty()) {
            hideOverlay();
            return;
        }

        positionOverlay();
    }

    private boolean shouldShowOverlay() {
        return CommandHelperFeature.isFeatureEnabled()
                && Vars.ui != null
                && Vars.ui.chatfrag != null
                && Vars.ui.chatfrag.shown();
    }

    private TextField resolveChatField() {
        try {
            return Reflect.get(Vars.ui.chatfrag, "chatfield");
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isValidCommandInput(String raw) {
        if (raw == null) return false;
        String text = raw.trim();
        if (text.startsWith("/t /") || text.startsWith("/a /")) {
            text = text.substring(3).trim();
        }
        return text.startsWith("/") && !text.equals("/t ") && !text.equals("/a ");
    }

    private void hideOverlay() {
        if (isShown()) {
            setShown(false);
        }
    }

    private void positionOverlay() {
        try {
            Vec2 stagePos = targetField.localToStageCoordinates(Tmp.v1.set(0, targetField.getHeight() + 4f));
            setPosition(stagePos.x, stagePos.y);
            setWidth(Math.max(targetField.getWidth(), MIN_OVERLAY_WIDTH));
            toFront();
            if (!isShown()) {
                setShown(true);
            }
        } catch (Exception e) {
            Log.err("Error updating CommandSuggestionOverlay pos", e);
        }
    }

    public void updateSuggestions(String rawInput) {
        items.clear();
        selectedIndex = 0;

        if (rawInput == null) {
            rebuildUI();
            return;
        }

        String prefix = extractChannelPrefix(rawInput);
        String text = rawInput.substring(prefix.length());

        if (!text.startsWith("/")) {
            rebuildUI();
            return;
        }

        int firstSpace = text.indexOf(' ');
        if (firstSpace == -1) {
            suggestCommandNames(text.substring(1), prefix);
        } else {
            suggestCommandArguments(text, prefix, firstSpace);
        }

        rebuildUI();
    }

    private String extractChannelPrefix(String input) {
        if (input.startsWith("/t /")) return "/t ";
        if (input.startsWith("/a /")) return "/a ";
        return "";
    }

    private void suggestCommandNames(String query, String prefix) {
        Seq<ChatCommand> matches = CommandRegistry.getInstance().getMatchingCommands(query);
        for (ChatCommand cmd : matches) {
            String completion = prefix + "/" + cmd.name + (cmd.paramSyntax.isEmpty() ? "" : " ");
            String description = cmd.isClientOnly ? "[stat][Client][] " + cmd.description : cmd.description;
            items.add(new SuggestionItem(completion, "/" + cmd.name, cmd.paramSyntax, description, true));
        }
    }

    private void suggestCommandArguments(String text, String prefix, int firstSpace) {
        String cmdName = text.substring(1, firstSpace).trim();
        ChatCommand cmd = CommandRegistry.getInstance().find(cmdName);
        if (cmd == null) return;

        if (cmd.paramCompleter == null) {
            items.add(new SuggestionItem(prefix + text, "/" + cmd.name, cmd.paramSyntax, cmd.description, true));
            return;
        }

        String argsString = text.substring(firstSpace + 1);
        boolean endsWithSpace = text.endsWith(" ");
        Seq<String> tokens = tokenizeArguments(argsString, endsWithSpace);

        int currentArgIndex = tokens.size - 1;
        String currentArgPrefix = tokens.get(currentArgIndex);
        String[] argsArray = tokens.toArray(String.class);
        Seq<String> suggestions = cmd.paramCompleter.getSuggestions(argsArray, currentArgIndex, currentArgPrefix);

        StringBuilder baseCommand = new StringBuilder(prefix).append("/").append(cmd.name).append(" ");
        for (int i = 0; i < currentArgIndex; i++) {
            baseCommand.append(tokens.get(i)).append(" ");
        }

        for (String sug : suggestions) {
            String comp = baseCommand.toString() + sug + " ";
            items.add(new SuggestionItem(comp, sug, cmd.paramSyntax, cmd.description, false));
        }
    }

    private Seq<String> tokenizeArguments(String argsString, boolean endsWithSpace) {
        Seq<String> tokens = new Seq<>();
        for (String token : argsString.split("\\s+")) {
            if (!token.isEmpty()) tokens.add(token);
        }
        if (endsWithSpace || tokens.isEmpty()) {
            tokens.add("");
        }
        return tokens;
    }

    private void rebuildUI() {
        clear();
        if (items.isEmpty()) return;

        background(Styles.black6);
        margin(6f);

        Table listTable = new Table();
        listTable.left();

        int start = Math.max(0, Math.min(selectedIndex - MAX_VISIBLE_SUGGESTIONS / 2, items.size - MAX_VISIBLE_SUGGESTIONS));
        int end = Math.min(items.size, start + MAX_VISIBLE_SUGGESTIONS);

        for (int i = start; i < end; i++) {
            SuggestionItem item = items.get(i);
            listTable.add(buildSuggestionRow(item, i == selectedIndex)).growX().left().row();
        }

        add(listTable).growX().row();

        Table footer = new Table();
        footer.left();
        footer.add(new Label("[darkgray]Tab: Tự điền  •  ↑↓: Chọn  •  Click: Dùng[]")).left();
        add(footer).growX().left().padTop(4f);

        pack();
    }

    private Table buildSuggestionRow(SuggestionItem item, boolean isSelected) {
        Table row = new Table();
        row.left().margin(4f, 8f, 4f, 8f);
        row.touchable = Touchable.enabled;

        if (isSelected) {
            row.background(Styles.flatDown);
        }

        row.add(new Label(isSelected ? "[accent]> []" : "  ")).left();
        row.add(new Label((isSelected ? "[accent]" : "[white]") + item.displayText + "[]")).left().padRight(8f);

        if (!item.paramSyntax.isEmpty()) {
            row.add(new Label("[lightgray]" + item.paramSyntax + "[]")).left().padRight(8f);
        }

        if (!item.description.isEmpty()) {
            row.add(new Label("[gray]" + item.description + "[]")).left().growX().ellipsis(true);
        }

        row.clicked(() -> applySuggestion(item));
        return row;
    }
}
