package me.ihqqq.marriage.command.subcommand.player;

import me.ihqqq.marriage.MarriagePlugin;
import me.ihqqq.marriage.command.subcommand.SubCommand;
import me.ihqqq.marriage.service.TeleportService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class TpCommand implements SubCommand {

    private final TeleportService teleportService;

    public TpCommand(@NotNull MarriagePlugin plugin) {
        this.teleportService = plugin.getTeleportService();
    }

    @Override
    public @NotNull String getName() {
        return "tp";
    }

    @Override
    public @NotNull String getPermission() {
        return "marriage.tp";
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        Player player = (Player) sender;
        teleportService.teleportToPartner(player);
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return List.of();
    }
}