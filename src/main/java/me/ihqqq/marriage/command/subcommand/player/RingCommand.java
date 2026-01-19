package me.ihqqq.marriage.command.subcommand.player;

import me.ihqqq.marriage.MarriagePlugin;
import me.ihqqq.marriage.command.subcommand.SubCommand;
import me.ihqqq.marriage.message.MessageKey;
import me.ihqqq.marriage.message.MessageService;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class RingCommand implements SubCommand {

    private final MessageService messages;

    public RingCommand(@NotNull MarriagePlugin plugin) {
        this.messages = plugin.getMessageService();
    }

    @Override
    public @NotNull String getName() {
        return "ring";
    }

    @Override
    public @NotNull String getPermission() {
        return "marriage.ring";
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        messages.sendMessage(sender, MessageKey.RING_UNIMPLEMENTED);
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return List.of();
    }
}