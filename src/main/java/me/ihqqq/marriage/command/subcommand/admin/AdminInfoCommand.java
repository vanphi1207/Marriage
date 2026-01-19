package me.ihqqq.marriage.command.subcommand.admin;

import me.ihqqq.marriage.MarriagePlugin;
import me.ihqqq.marriage.command.subcommand.SubCommand;
import me.ihqqq.marriage.message.MessageKey;
import me.ihqqq.marriage.message.MessageService;
import me.ihqqq.marriage.service.MarriageService;
import me.ihqqq.marriage.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AdminInfoCommand implements SubCommand {

    private final MarriageService marriageService;
    private final MessageService messages;

    public AdminInfoCommand(@NotNull MarriagePlugin plugin) {
        this.marriageService = plugin.getMarriageService();
        this.messages = plugin.getMessageService();
    }

    @Override
    public @NotNull String getName() {
        return "info";
    }

    @Override
    public @NotNull String getPermission() {
        return "marriage.admin.info";
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 1) {
            messages.sendMessage(sender, MessageKey.ADMIN_HELP_INFO);
            return;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        String display = target.getName() == null ? args[0] : target.getName();
        marriageService.getMarriage(target.getUniqueId()).thenAccept(record -> {
            if (record == null) {
                messages.sendMessage(sender, MessageKey.ADMIN_INFO_SINGLE, Map.of("player", display));
                return;
            }
            String partnerName = Bukkit.getOfflinePlayer(record.getPartner(target.getUniqueId())).getName();
            Map<String, Object> placeholders = new HashMap<>();
            placeholders.put("player", display);
            placeholders.put("partner", partnerName == null ? "Unknown" : partnerName);
            placeholders.put("since", TimeUtil.formatDate(record.getSince()));
            placeholders.put("days", TimeUtil.daysBetween(record.getSince()));
            messages.sendMessage(sender, MessageKey.ADMIN_INFO_MARRIED, placeholders);
        });
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            List<String> names = new ArrayList<>();
            Bukkit.getOnlinePlayers().forEach(p -> {
                if (p.getName().toLowerCase().startsWith(partial)) names.add(p.getName());
            });
            return names;
        }
        return List.of();
    }
}