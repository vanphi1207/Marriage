package me.ihqqq.marriage.command.subcommand.player;

import me.ihqqq.marriage.MarriagePlugin;
import me.ihqqq.marriage.command.subcommand.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class GiftCommand implements SubCommand {

    private final MarriagePlugin plugin;

    public GiftCommand(@NotNull MarriagePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getName() {
        return "gift";
    }

    @Override
    public @NotNull String getPermission() {
        return "marriage.gift";
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        Player player = (Player) sender;
        plugin.getGiftService().openGiftMenu(player);
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return List.of();
    }
}