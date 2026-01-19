package me.ihqqq.marriage.command;

import me.ihqqq.marriage.MarriagePlugin;
import me.ihqqq.marriage.command.subcommand.SubCommand;
import me.ihqqq.marriage.command.subcommand.admin.AdminDivorceCommand;
import me.ihqqq.marriage.command.subcommand.admin.AdminInfoCommand;
import me.ihqqq.marriage.command.subcommand.admin.AdminReloadCommand;
import me.ihqqq.marriage.command.subcommand.admin.AdminSetCommand;
import me.ihqqq.marriage.command.subcommand.admin.*;
import me.ihqqq.marriage.message.MessageKey;
import me.ihqqq.marriage.message.MessageService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;


public class MarryAdminCommand implements CommandExecutor, TabCompleter {

    private final MarriagePlugin plugin;
    private final MessageService messages;
    private final Map<String, SubCommand> subCommands = new LinkedHashMap<>();

    public MarryAdminCommand(@NotNull MarriagePlugin plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessageService();
        
        registerSubCommand(new AdminSetCommand(plugin));
        registerSubCommand(new AdminDivorceCommand(plugin));
        registerSubCommand(new AdminInfoCommand(plugin));
        registerSubCommand(new AdminReloadCommand(plugin));
    }

    private void registerSubCommand(@NotNull SubCommand cmd) {
        subCommands.put(cmd.getName().toLowerCase(), cmd);
        for (String alias : cmd.getAliases()) {
            subCommands.put(alias.toLowerCase(), cmd);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        String name = args[0].toLowerCase();
        SubCommand sub = subCommands.get(name);
        if (sub == null) {
            showHelp(sender);
            return true;
        }
        String perm = sub.getPermission();
        if (!perm.isEmpty() && !sender.hasPermission(perm)) {
            messages.sendMessage(sender, MessageKey.NO_PERMISSION);
            return true;
        }
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        sub.execute(sender, subArgs);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length <= 1) {
            String partial = args.length == 0 ? "" : args[0].toLowerCase();
            List<String> completions = new ArrayList<>();
            for (Map.Entry<String, SubCommand> entry : subCommands.entrySet()) {
                String name = entry.getKey();
                SubCommand cmd = entry.getValue();
                if (!cmd.getPermission().isEmpty() && !sender.hasPermission(cmd.getPermission())) {
                    continue;
                }
                if (partial.isEmpty() || name.startsWith(partial)) {
                    completions.add(name);
                }
            }
            return completions;
        }
        SubCommand sub = subCommands.get(args[0].toLowerCase());
        if (sub != null) {
            String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
            return sub.tabComplete(sender, subArgs);
        }
        return Collections.emptyList();
    }

    private void showHelp(CommandSender sender) {
        
        messages.sendMessage(sender, MessageKey.ADMIN_HELP);
        if (sender.hasPermission("marriage.admin.set")) {
            messages.sendMessage(sender, MessageKey.ADMIN_HELP_SET);
        }
        if (sender.hasPermission("marriage.admin.divorce")) {
            messages.sendMessage(sender, MessageKey.ADMIN_HELP_DIVORCE);
        }
        if (sender.hasPermission("marriage.admin.info")) {
            messages.sendMessage(sender, MessageKey.ADMIN_HELP_INFO);
        }
        if (sender.hasPermission("marriage.admin.reload")) {
            messages.sendMessage(sender, MessageKey.ADMIN_HELP_RELOAD);
        }
    }
}