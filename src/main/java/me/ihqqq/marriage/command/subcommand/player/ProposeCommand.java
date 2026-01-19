package me.ihqqq.marriage.command.subcommand.player;

import me.ihqqq.marriage.MarriagePlugin;
import me.ihqqq.marriage.command.subcommand.SubCommand;
import me.ihqqq.marriage.message.MessageKey;
import me.ihqqq.marriage.message.MessageService;
import me.ihqqq.marriage.service.RequestService;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;


public class ProposeCommand implements SubCommand {

    private final MarriagePlugin plugin;
    private final MessageService messages;
    private final RequestService requestService;

    public ProposeCommand(@NotNull MarriagePlugin plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessageService();
        this.requestService = plugin.getRequestService();
    }

    @Override
    public @NotNull String getName() {
        return "propose";
    }

    @Override
    public @NotNull String getPermission() {
        return "marriage.propose";
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        Player proposer = (Player) sender;
        if (args.length < 1) {
            
            messages.sendMessage(proposer, MessageKey.HELP_PROPOSE);
            return;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            messages.sendMessage(proposer, MessageKey.PLAYER_NOT_FOUND, Map.of("player", args[0]));
            return;
        }
        
        requestService.propose(proposer, target);
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            List<String> names = new java.util.ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.getName().toLowerCase().startsWith(partial)) {
                    continue;
                }
                names.add(p.getName());
            }
            return names;
        }
        return Collections.emptyList();
    }
}