package mindustrytool.features.chat.command;

import arc.Core;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.gen.Call;
import mindustrytool.features.FeatureManager;
import mindustrytool.features.chat.translation.ChatTranslationConfig;
import mindustrytool.features.chat.translation.ChatTranslationFeature;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandRegistry {
    private static final CommandRegistry instance = new CommandRegistry();

    public static CommandRegistry getInstance() {
        return instance;
    }

    private final Seq<ChatCommand> commands = new Seq<>();
    private final ObjectMap<String, ChatCommand> commandMap = new ObjectMap<>();

    private static final Pattern HELP_PATTERN = Pattern.compile("^\\s*/([a-zA-Z0-9_-]+)(?:\\s+([<\\[][^\\]>]+[\\]>]))?(?:\\s*[-–:]\\s*(.*))?$");

    public CommandRegistry() {
        initDefaultCommands();
    }

    public void register(ChatCommand cmd) {
        commands.add(cmd);
        commandMap.put(cmd.name.toLowerCase(), cmd);
        for (String alias : cmd.aliases) {
            commandMap.put(alias.toLowerCase(), cmd);
        }
    }

    public ChatCommand find(String nameOrAlias) {
        if (nameOrAlias == null) return null;
        return commandMap.get(nameOrAlias.toLowerCase());
    }

    public Seq<ChatCommand> getMatchingCommands(String prefix) {
        Seq<ChatCommand> matches = new Seq<>();
        String clean = prefix.startsWith("/") ? prefix.substring(1) : prefix;
        for (ChatCommand cmd : commands) {
            if (cmd.matches(clean)) {
                matches.add(cmd);
            }
        }
        return matches;
    }

    public void learnFromServerMessage(String message) {
        if (message == null || !message.contains("/")) return;
        String[] lines = message.split("\n");
        for (String line : lines) {
            String cleanLine = arc.util.Strings.stripColors(line).trim();
            if (cleanLine.startsWith("/")) {
                Matcher matcher = HELP_PATTERN.matcher(cleanLine);
                if (matcher.find()) {
                    String name = matcher.group(1);
                    String params = matcher.group(2);
                    String desc = matcher.group(3);
                    if (name != null && !commandMap.containsKey(name.toLowerCase())) {
                        register(new ChatCommand(name, params != null ? params : "", desc != null ? desc : "", false, null, null));
                    }
                }
            }
        }
    }

    private void initDefaultCommands() {
        // === Client-side Utilities ===
        register(new ChatCommand("clear", "", "Xóa toàn bộ tin nhắn chat trên màn hình", true, args -> {
            if (Vars.ui != null && Vars.ui.chatfrag != null) {
                Vars.ui.chatfrag.clearMessages();
                Vars.ui.chatfrag.addMessage("[accent]MindustryTool:[] [lightgray]Đã xóa lịch sử trò chuyện.[]");
            }
        }, null, "cls"));

        register(new ChatCommand("ping", "", "Kiểm tra độ trễ mạng và FPS", true, args -> {
            int ping = (Vars.netClient != null && Vars.net != null && Vars.net.client()) ? Vars.netClient.getPing() : 0;
            int fps = Core.graphics.getFramesPerSecond();
            if (Vars.ui != null && Vars.ui.chatfrag != null) {
                Vars.ui.chatfrag.addMessage("[accent]MindustryTool:[] [white]Ping: [accent]" + ping + "ms[] | FPS: [accent]" + fps + "[]");
            }
        }, null));

        register(new ChatCommand("pos", "", "Hiện và sao chép tọa độ hiện tại vào bộ nhớ tạm", true, args -> {
            if (Vars.player != null) {
                int tx = (int) (Vars.player.x / 8f);
                int ty = (int) (Vars.player.y / 8f);
                String coords = "(" + tx + ", " + ty + ")";
                Core.app.setClipboardText(coords);
                if (Vars.ui != null && Vars.ui.chatfrag != null) {
                    Vars.ui.chatfrag.addMessage("[accent]MindustryTool:[] [white]Tọa độ của bạn: [accent]" + coords + "[] (Đã sao chép)");
                }
            }
        }, null, "coords"));

        register(new ChatCommand("hud", "", "Bật hoặc tắt thanh công cụ truy cập nhanh HUD", true, args -> {
            boolean current = Core.settings.getBool("mindustrytool.quick-access-hud.enabled", true);
            Core.settings.put("mindustrytool.quick-access-hud.enabled", !current);
            if (Vars.ui != null && Vars.ui.chatfrag != null) {
                Vars.ui.chatfrag.addMessage("[accent]MindustryTool:[] [white]Thanh HUD Quick Access: " + (!current ? "[lime]BẬT[]" : "[scarlet]TẮT[]"));
            }
        }, null));

        register(new ChatCommand("tr", "<tin_nhắn>", "Dịch tin nhắn sang ngôn ngữ đích và gửi vào chat (Ctrl+T để dịch trực tiếp)", true, args -> {
            if (args != null && args.length > 0) {
                String textToTranslate = String.join(" ", args).trim();
                ChatTranslationFeature feature = FeatureManager.getInstance().getFeature(ChatTranslationFeature.class);
                if (feature != null && !textToTranslate.isEmpty()) {
                    feature.translateOutgoing(textToTranslate).thenAccept(translated -> {
                        String clean = (translated != null && !translated.trim().isEmpty()) ? translated.trim() : textToTranslate;
                        String toSend = ChatTranslationConfig.isReverseIncludeOriginal()
                                ? clean + " [lightgray](" + textToTranslate + ")"
                                : clean;
                        Call.sendChatMessage(toSend);
                    }).exceptionally(err -> {
                        Call.sendChatMessage(textToTranslate);
                        return null;
                    });
                }
            } else {
                String currentLang = ChatTranslationConfig.getReverseTargetLang();
                if (Vars.ui != null && Vars.ui.chatfrag != null) {
                    Vars.ui.chatfrag.addMessage("[accent]MindustryTool:[] [white]Dùng: [accent]/tr <tin nhắn>[] để dịch và gửi ngay.\nĐổi ngôn ngữ bằng: [accent]/trlang <mã>[] (hiện tại: [accent]" + currentLang + "[]). Phím tắt: [accent]Ctrl + T[]");
                }
            }
        }, null, "translate", "trans"));

        register(new ChatCommand("trlang", "[mã_ngôn_ngữ]", "Xem hoặc đổi ngôn ngữ đích của Chat Translation (VD: /trlang en, /trlang vi)", true, args -> {
            if (args != null && args.length > 0 && !args[0].trim().isEmpty()) {
                String target = args[0].trim().toLowerCase();
                ChatTranslationConfig.setReverseTargetLang(target);
                ChatTranslationConfig.setOutgoingTargetLang(target);
                if (Vars.ui != null && Vars.ui.chatfrag != null) {
                    Vars.ui.chatfrag.addMessage("[accent]MindustryTool:[] [white]Ngôn ngữ dịch đích đổi thành: [accent]" + target + "[]");
                }
            } else {
                String currentLang = ChatTranslationConfig.getReverseTargetLang();
                if (Vars.ui != null && Vars.ui.chatfrag != null) {
                    Vars.ui.chatfrag.addMessage("[accent]MindustryTool:[] [white]Ngôn ngữ dịch hiện tại: [accent]" + currentLang + "[] (Dùng: [accent]/trlang <mã_ngôn_ngữ>[])");
                }
            }
        }, null));

        register(new ChatCommand("zoom", "<tỷ_lệ>", "Đặt tỷ lệ thu phóng camera (VD: /zoom 1.5, /zoom 0.5)", true, args -> {
            if (args != null && args.length > 0) {
                try {
                    float zoom = Float.parseFloat(args[0].trim());
                    Vars.renderer.setScale(zoom);
                    if (Vars.ui != null && Vars.ui.chatfrag != null) {
                        Vars.ui.chatfrag.addMessage("[accent]MindustryTool:[] [white]Đã đặt độ thu phóng camera: [accent]" + zoom + "[]");
                    }
                } catch (Exception e) {
                    if (Vars.ui != null && Vars.ui.chatfrag != null) {
                        Vars.ui.chatfrag.addMessage("[scarlet]Giá trị thu phóng không hợp lệ. Ví dụ: /zoom 1.5[]");
                    }
                }
            } else {
                if (Vars.ui != null && Vars.ui.chatfrag != null) {
                    Vars.ui.chatfrag.addMessage("[accent]MindustryTool:[] [white]Độ thu phóng hiện tại: [accent]" + Vars.renderer.getScale() + "[]");
                }
            }
        }, null));

        // === Common Server Commands ===
        register(new ChatCommand("help", "[lệnh/trang]", "Xem danh sách lệnh trên máy chủ", false, null, null, "?"));
        register(new ChatCommand("sync", "", "Đồng bộ lại trạng thái thế giới với máy chủ", false, null, null));
        register(new ChatCommand("votekick", "<người_chơi> [lý_do]", "Bỏ phiếu đuổi một người chơi khỏi phòng", false, null, ArgumentCompleters.PLAYERS, "vk"));
        register(new ChatCommand("vote", "<y/n>", "Bỏ phiếu đồng ý hoặc từ chối", false, null, ArgumentCompleters.BOOLEAN));
        register(new ChatCommand("rtv", "[bản_đồ]", "Bỏ phiếu chuyển sang bản đồ khác (Rock The Vote)", false, null, null));
        register(new ChatCommand("maps", "[trang]", "Xem danh sách bản đồ có sẵn trên máy chủ", false, null, null));
        register(new ChatCommand("rules", "", "Xem các quy định của máy chủ", false, null, null));
        register(new ChatCommand("discord", "", "Xem liên kết tham gia Discord của máy chủ", false, null, null));
        register(new ChatCommand("info", "", "Xem thông tin máy chủ", false, null, null));
        register(new ChatCommand("t", "<tin_nhắn>", "Gửi tin nhắn riêng trong nội bộ đội", false, null, null, "team"));
        register(new ChatCommand("w", "<người_chơi> <tin_nhắn>", "Nhắn tin riêng tư (thì thầm) tới một người chơi", false, null, ArgumentCompleters.PLAYERS, "whisper", "msg", "tell"));
        register(new ChatCommand("m", "<người_chơi> <tin_nhắn>", "Nhắn tin riêng tư tới một người chơi", false, null, ArgumentCompleters.PLAYERS));
        register(new ChatCommand("pause", "", "Yêu cầu tạm dừng hoặc tiếp tục trận đấu", false, null, null));
        register(new ChatCommand("spectate", "[người_chơi]", "Theo dõi góc nhìn của người chơi khác", false, null, ArgumentCompleters.PLAYERS, "spec"));
        register(new ChatCommand("stats", "", "Xem thông số trận đấu của bạn", false, null, null));
        register(new ChatCommand("kill", "", "Tự hủy đơn vị hiện tại", false, null, null));

        // === Admin / Sandbox Commands ===
        register(new ChatCommand("spawn", "<đơn_vị> [số_lượng] [đội]", "Tạo đơn vị (Chế độ Sandbox / Admin)", false, null, ArgumentCompleters.UNITS));
        register(new ChatCommand("give", "<vật_phẩm> [số_lượng]", "Thêm tài nguyên vào Core (Sandbox / Admin)", false, null, ArgumentCompleters.ITEMS, "item"));
        register(new ChatCommand("team", "<đội>", "Chuyển sang đội khác (Sandbox / Admin)", false, null, ArgumentCompleters.TEAMS));
    }
}
