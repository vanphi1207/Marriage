package me.ihqqq.marriage.command.subcommand.player;

import me.ihqqq.marriage.MarriagePlugin;
import me.ihqqq.marriage.command.subcommand.SubCommand;
import me.ihqqq.marriage.message.MessageKey;
import me.ihqqq.marriage.message.MessageService;
import me.ihqqq.marriage.service.MarriageService;
import me.ihqqq.marriage.util.SchedulerUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class ChatCommand implements SubCommand {

    private final MarriageService marriageService;
    private final MessageService messages;
    private final SchedulerUtil scheduler;

    public ChatCommand(@NotNull MarriagePlugin plugin) {
        this.marriageService = plugin.getMarriageService();
        this.messages = plugin.getMessageService();
        this.scheduler = plugin.getSchedulerUtil();
    }

    @Override
    public @NotNull String getName() {
        return "chat";
    }

    @Override
    public @NotNull String getPermission() {
        return "marriage.chat";
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        Player player = (Player) sender;
        if (!marriageService.isChatGloballyEnabled()) {
            messages.sendMessage(player, MessageKey.CHAT_DISABLED);
            return;
        }
        marriageService.getMarriage(player.getUniqueId()).thenAccept(record -> {
            if (record == null) {
                scheduler.runSync(() -> messages.sendMessage(player, MessageKey.NOT_MARRIED));
                return;
            }
            boolean state = marriageService.toggleChat(player.getUniqueId());
            Map<String, Object> placeholders = new HashMap<>();
            placeholders.put("state", state ? "<green>enabled</green>" : "<red>disabled</red>");
            scheduler.runSync(() -> messages.sendMessage(player, MessageKey.CHAT_TOGGLED, placeholders));
        });
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return List.of();
    }
}
