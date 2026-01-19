package me.ihqqq.marriage.command.subcommand.admin;

import me.ihqqq.marriage.MarriagePlugin;
import me.ihqqq.marriage.command.subcommand.SubCommand;
import me.ihqqq.marriage.message.MessageKey;
import me.ihqqq.marriage.message.MessageService;
import me.ihqqq.marriage.service.MarriageService;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class AdminDivorceCommand implements SubCommand {

    private final MarriageService marriageService;
    private final MessageService messages;

    public AdminDivorceCommand(@NotNull MarriagePlugin plugin) {
        this.marriageService = plugin.getMarriageService();
        this.messages = plugin.getMessageService();
    }

    @Override
    public @NotNull String getName() {
        return "divorce";
    }

    @Override
    public @NotNull String getPermission() {
        return "marriage.admin.divorce";
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 1) {
            messages.sendMessage(sender, MessageKey.ADMIN_HELP_DIVORCE);
            return;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        marriageService.divorceForce(target.getUniqueId(), sender);
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