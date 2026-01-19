package me.ihqqq.marriage.command.subcommand.player;

import me.ihqqq.marriage.MarriagePlugin;
import me.ihqqq.marriage.command.subcommand.SubCommand;
import me.ihqqq.marriage.message.MessageService;
import me.ihqqq.marriage.service.RequestService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class AcceptCommand implements SubCommand {

    private final MarriagePlugin plugin;
    private final MessageService messages;
    private final RequestService requestService;

    public AcceptCommand(@NotNull MarriagePlugin plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessageService();
        this.requestService = plugin.getRequestService();
    }

    @Override
    public @NotNull String getName() {
        return "accept";
    }

    @Override
    public @NotNull String getPermission() {
        return "marriage.accept";
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        Player player = (Player) sender;
        requestService.accept(player);
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return List.of();
    }
}