package me.ihqqq.marriage.command.subcommand.player;

import me.ihqqq.marriage.MarriagePlugin;
import me.ihqqq.marriage.command.subcommand.SubCommand;
import me.ihqqq.marriage.service.RequestService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class DenyCommand implements SubCommand {

    private final RequestService requestService;

    public DenyCommand(@NotNull MarriagePlugin plugin) {
        this.requestService = plugin.getRequestService();
    }

    @Override
    public @NotNull String getName() {
        return "deny";
    }

    @Override
    public @NotNull String getPermission() {
        return "marriage.deny";
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        Player player = (Player) sender;
        requestService.deny(player);
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return List.of();
    }
}