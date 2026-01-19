package me.ihqqq.marriage.command.subcommand.player;

import me.ihqqq.marriage.MarriagePlugin;
import me.ihqqq.marriage.command.subcommand.SubCommand;
import me.ihqqq.marriage.message.MessageKey;
import me.ihqqq.marriage.message.MessageService;
import me.ihqqq.marriage.service.MarriageService;
import me.ihqqq.marriage.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class InfoCommand implements SubCommand {

    private final MarriagePlugin plugin;
    private final MessageService messages;
    private final MarriageService marriageService;

    public InfoCommand(@NotNull MarriagePlugin plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessageService();
        this.marriageService = plugin.getMarriageService();
    }

    @Override
    public @NotNull String getName() {
        return "info";
    }

    @Override
    public @NotNull String getPermission() {
        return "marriage.info";
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        Player player = (Player) sender;
        UUID targetUuid;
        String displayName;
        if (args.length >= 1) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            targetUuid = target.getUniqueId();
            displayName = target.getName() == null ? args[0] : target.getName();
        } else {
            targetUuid = player.getUniqueId();
            displayName = player.getName();
        }

        final String finalDisplayName = displayName;
        marriageService.getMarriage(targetUuid).thenAccept(record -> {
            if (record == null) {
                if (targetUuid.equals(player.getUniqueId())) {
                    messages.sendMessage(player, MessageKey.INFO_SINGLE);
                } else {
                    
                    messages.sendMessage(player, MessageKey.INFO_SINGLE_OTHER, Map.of("player", finalDisplayName));
                }
                return;
            }
            UUID partnerUuid = record.getPartner(targetUuid);
            OfflinePlayer partnerPlayer = Bukkit.getOfflinePlayer(partnerUuid);
            String partnerName = partnerPlayer.getName() == null ? partnerUuid.toString() : partnerPlayer.getName();
            Map<String, Object> placeholders = new HashMap<>();
            placeholders.put("partner", partnerName);
            placeholders.put("since", TimeUtil.formatDate(record.getSince()));
            placeholders.put("days", TimeUtil.daysBetween(record.getSince()));
            
            if (targetUuid.equals(player.getUniqueId())) {
                messages.sendMessage(player, MessageKey.INFO_MARRIED, placeholders);
            } else {
                placeholders.put("player", finalDisplayName);
                messages.sendMessage(player, MessageKey.INFO_MARRIED_OTHER, placeholders);
            }
        });
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            List<String> suggestions = new java.util.ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(partial)) {
                    suggestions.add(p.getName());
                }
            }
            return suggestions;
        }
        return List.of();
    }
}