package me.ihqqq.marriage.command.subcommand.player;

import me.ihqqq.marriage.MarriagePlugin;
import me.ihqqq.marriage.command.subcommand.SubCommand;
import me.ihqqq.marriage.gui.menu.MarriageMenu;
import me.ihqqq.marriage.message.MessageService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;


public class MenuCommand implements SubCommand {

    private final MarriagePlugin plugin;
    private final MessageService messages;

    public MenuCommand(@NotNull MarriagePlugin plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessageService();
    }

    @Override
    public @NotNull String getName() {
        return "menu";
    }

    @Override
    public @NotNull String getPermission() {
        return "marriage.menu";
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        Player player = (Player) sender;
        plugin.getMenuManager().openMenu(player, new MarriageMenu(plugin, player));
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return Collections.emptyList();
    }
}