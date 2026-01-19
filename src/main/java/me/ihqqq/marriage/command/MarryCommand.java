package me.ihqqq.marriage.command;

import me.ihqqq.marriage.MarriagePlugin;
import me.ihqqq.marriage.command.subcommand.SubCommand;
import me.ihqqq.marriage.command.subcommand.player.*;
import me.ihqqq.marriage.command.subcommand.player.*;
import me.ihqqq.marriage.message.MessageKey;
import me.ihqqq.marriage.message.MessageService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MarryCommand implements CommandExecutor, TabCompleter {

    private final MarriagePlugin plugin;
    private final MessageService messages;
    private final Map<String, SubCommand> subCommands;

    public MarryCommand(@NotNull MarriagePlugin plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessageService();
        this.subCommands = new LinkedHashMap<>();

        
        registerSubCommand(new MenuCommand(plugin));
        registerSubCommand(new ProposeCommand(plugin));
        registerSubCommand(new AcceptCommand(plugin));
        registerSubCommand(new DenyCommand(plugin));
        registerSubCommand(new DivorceCommand(plugin));
        registerSubCommand(new InfoCommand(plugin));
        registerSubCommand(new ChatCommand(plugin));
        registerSubCommand(new MsgCommand(plugin));
        registerSubCommand(new TpCommand(plugin));
        registerSubCommand(new SetHomeCommand(plugin));
        registerSubCommand(new HomeCommand(plugin));
        registerSubCommand(new GiftCommand(plugin));
        registerSubCommand(new RingCommand(plugin));
        registerSubCommand(new ReloadCommand(plugin));
    }

    private void registerSubCommand(@NotNull SubCommand cmd) {
        this.subCommands.put(cmd.getName().toLowerCase(), cmd);
        for (String alias : cmd.getAliases()) {
            this.subCommands.put(alias.toLowerCase(), cmd);
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

        
        if (sub.isPlayerOnly() && !(sender instanceof Player)) {
            messages.sendMessage(sender, MessageKey.NOT_A_PLAYER);
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
                if (!partial.isEmpty() && !name.startsWith(partial)) {
                    continue;
                }
                completions.add(name);
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

    
    private void showHelp(@NotNull CommandSender sender) {
        messages.sendMessage(sender, MessageKey.HELP_HEADER);
        
        for (Map.Entry<String, SubCommand> entry : this.subCommands.entrySet()) {
            SubCommand sub = entry.getValue();
            if (!sub.getPermission().isEmpty() && !sender.hasPermission(sub.getPermission())) {
                continue;
            }
            
            String name = sub.getName().toLowerCase();
            MessageKey key;
            switch (name) {
                case "menu" -> key = MessageKey.HELP_MENU;
                case "propose" -> key = MessageKey.HELP_PROPOSE;
                case "accept" -> key = MessageKey.HELP_ACCEPT;
                case "deny" -> key = MessageKey.HELP_DENY;
                case "divorce" -> key = MessageKey.HELP_DIVORCE;
                case "info" -> key = MessageKey.HELP_INFO;
                case "chat" -> key = MessageKey.HELP_CHAT;
                case "msg" -> key = MessageKey.HELP_MSG;
                case "tp" -> key = MessageKey.HELP_TP;
                case "sethome" -> key = MessageKey.HELP_SETHOME;
                case "home" -> key = MessageKey.HELP_HOME;
                case "gift" -> key = MessageKey.HELP_GIFT;
                case "ring" -> key = MessageKey.HELP_RING;
                case "reload" -> key = MessageKey.HELP_RELOAD;
                default -> key = null;
            }
            if (key != null) {
                messages.sendMessage(sender, key);
            }
        }
    }
}