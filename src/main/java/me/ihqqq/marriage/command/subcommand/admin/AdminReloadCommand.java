package me.ihqqq.marriage.command.subcommand.admin;

import me.ihqqq.marriage.MarriagePlugin;
import me.ihqqq.marriage.command.subcommand.SubCommand;
import me.ihqqq.marriage.message.MessageKey;
import me.ihqqq.marriage.message.MessageService;
import me.ihqqq.marriage.model.MarriageSettings;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.List;
public class AdminReloadCommand implements SubCommand {

    private final MarriagePlugin plugin;
    private final MessageService messages;

    public AdminReloadCommand(@NotNull MarriagePlugin plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessageService();
    }

    @Override
    public @NotNull String getName() {
        return "reload";
    }

    @Override
    public @NotNull String getPermission() {
        return "marriage.admin.reload";
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        plugin.reloadConfig();
        plugin.reloadGuiConfig();
        messages.reload();
        FileConfiguration config = plugin.getConfig();
        boolean allowTeleport = config.getBoolean("marriage.allowTeleport", true);
        boolean chatEnabled = config.getBoolean("marriage.chatEnabled", true);
        plugin.setMarriageSettings(new MarriageSettings(allowTeleport, chatEnabled, new java.util.HashMap<>()));
        messages.sendMessage(sender, MessageKey.RELOAD_SUCCESS);
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return List.of();
    }
}
