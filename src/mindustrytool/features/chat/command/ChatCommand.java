package mindustrytool.features.chat.command;

import arc.func.Cons;
import arc.struct.Seq;

public class ChatCommand {
    public final String name;
    public final Seq<String> aliases;
    public final String paramSyntax;
    public final String description;
    public final boolean isClientOnly;
    public final Cons<String[]> action;
    public final ArgumentCompleters.Completer paramCompleter;

    public ChatCommand(String name, String paramSyntax, String description, boolean isClientOnly,
            Cons<String[]> action, ArgumentCompleters.Completer paramCompleter, String... aliases) {
        this.name = name;
        this.paramSyntax = paramSyntax == null ? "" : paramSyntax;
        this.description = description == null ? "" : description;
        this.isClientOnly = isClientOnly;
        this.action = action;
        this.paramCompleter = paramCompleter;
        this.aliases = Seq.with(aliases);
    }

    public boolean matches(String query) {
        if (name.toLowerCase().startsWith(query.toLowerCase())) {
            return true;
        }
        for (String alias : aliases) {
            if (alias.toLowerCase().startsWith(query.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public boolean isMatchExact(String query) {
        if (name.equalsIgnoreCase(query)) {
            return true;
        }
        for (String alias : aliases) {
            if (alias.equalsIgnoreCase(query)) {
                return true;
            }
        }
        return false;
    }
}
