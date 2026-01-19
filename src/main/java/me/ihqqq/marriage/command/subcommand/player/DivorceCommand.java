package me.ihqqq.marriage.command.subcommand.player;

import me.ihqqq.marriage.MarriagePlugin;
import me.ihqqq.marriage.command.subcommand.SubCommand;
import me.ihqqq.marriage.message.MessageKey;
import me.ihqqq.marriage.message.MessageService;
import me.ihqqq.marriage.service.MarriageService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class DivorceCommand implements SubCommand {

    private final MarriagePlugin plugin;
    private final MessageService messages;
    private final MarriageService marriageService;
    private final Map<UUID, Long> confirmMap = new HashMap<>();

    
    private static final long CONFIRM_TIMEOUT = 30_000L;

    public DivorceCommand(@NotNull MarriagePlugin plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessageService();
        this.marriageService = plugin.getMarriageService();
    }

    @Override
    public @NotNull String getName() {
        return "divorce";
    }

    @Override
    public @NotNull String getPermission() {
        return "marriage.divorce";
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        Player player = (Player) sender;
        
        if (args.length > 0 && args[0].equalsIgnoreCase("confirm")) {
            Long timestamp = confirmMap.get(player.getUniqueId());
            if (timestamp == null || System.currentTimeMillis() - timestamp > CONFIRM_TIMEOUT) {
                
                messages.sendMessage(player, MessageKey.DIVORCE_CONFIRM);
                confirmMap.put(player.getUniqueId(), System.currentTimeMillis());
                return;
            }
            confirmMap.remove(player.getUniqueId());
            
            marriageService.divorce(player);
            return;
        }
        
        marriageService.getMarriage(player.getUniqueId()).thenAccept(record -> {
            if (record == null) {
                messages.sendMessage(player, MessageKey.NOT_MARRIED);
                return;
            }
            
            messages.sendMessage(player, MessageKey.DIVORCE_CONFIRM);
            confirmMap.put(player.getUniqueId(), System.currentTimeMillis());
        });
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) {
            if ("confirm".startsWith(args[0].toLowerCase())) {
                return List.of("confirm");
            }
        }
        return List.of();
    }
}