package mindustrytool.features.chat.command;

import arc.struct.Seq;
import arc.util.Strings;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Player;

public class ArgumentCompleters {
    public interface Completer {
        Seq<String> getSuggestions(String[] args, int currentArgIndex, String currentArgPrefix);
    }

    public static final Completer PLAYERS = (args, index, prefix) -> {
        Seq<String> list = new Seq<>();
        String low = prefix == null ? "" : prefix.toLowerCase();
        if (Groups.player != null) {
            for (Player p : Groups.player) {
                if (p.name != null) {
                    String clean = Strings.stripColors(p.name).trim();
                    String candidate = clean.contains(" ") ? "\"" + clean + "\"" : clean;
                    if (clean.toLowerCase().startsWith(low)) {
                        list.add(candidate);
                    }
                }
            }
        }
        return list;
    };

    public static final Completer ITEMS = (args, index, prefix) -> {
        Seq<String> list = new Seq<>();
        String low = prefix == null ? "" : prefix.toLowerCase();
        if (Vars.content != null) {
            Vars.content.items().each(item -> {
                if (item.name.toLowerCase().startsWith(low)) {
                    list.add(item.name);
                }
            });
        }
        return list;
    };

    public static final Completer UNITS = (args, index, prefix) -> {
        Seq<String> list = new Seq<>();
        String low = prefix == null ? "" : prefix.toLowerCase();
        if (Vars.content != null) {
            Vars.content.units().each(unit -> {
                if (unit.name.toLowerCase().startsWith(low)) {
                    list.add(unit.name);
                }
            });
        }
        return list;
    };

    public static final Completer BLOCKS = (args, index, prefix) -> {
        Seq<String> list = new Seq<>();
        String low = prefix == null ? "" : prefix.toLowerCase();
        if (Vars.content != null) {
            Vars.content.blocks().each(block -> {
                if (block.name.toLowerCase().startsWith(low)) {
                    list.add(block.name);
                }
            });
        }
        return list;
    };

    public static final Completer TEAMS = (args, index, prefix) -> {
        Seq<String> list = new Seq<>();
        String low = prefix == null ? "" : prefix.toLowerCase();
        for (Team t : Team.all) {
            if (t.name != null && t.name.toLowerCase().startsWith(low)) {
                list.add(t.name);
            }
        }
        return list;
    };

    public static final Completer BOOLEAN = (args, index, prefix) -> {
        Seq<String> list = new Seq<>();
        String low = prefix == null ? "" : prefix.toLowerCase();
        for (String b : new String[]{"true", "false", "y", "n"}) {
            if (b.startsWith(low)) {
                list.add(b);
            }
        }
        return list;
    };
}
