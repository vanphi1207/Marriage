package me.ihqqq.marriage.command.subcommand.player;

import me.ihqqq.marriage.MarriagePlugin;
import me.ihqqq.marriage.command.subcommand.SubCommand;
import me.ihqqq.marriage.message.MessageKey;
import me.ihqqq.marriage.message.MessageService;
import me.ihqqq.marriage.service.MarriageService;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

public class MsgCommand implements SubCommand {

    private final MarriagePlugin plugin;
    private final MarriageService marriageService;
    private final MessageService messages;

    public MsgCommand(@NotNull MarriagePlugin plugin) {
        this.plugin = plugin;
        this.marriageService = plugin.getMarriageService();
        this.messages = plugin.getMessageService();
    }

    @Override
    public @NotNull String getName() {
        return "msg";
    }

    @Override
    public @NotNull String getPermission() {
        return "marriage.msg";
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        Player player = (Player) sender;
        if (args.length == 0) {
            messages.sendMessage(player, MessageKey.HELP_MSG);
            return;
        }
        String text = String.join(" ", args);
        marriageService.getMarriage(player.getUniqueId()).thenAccept(record -> {
            if (record == null) {
                messages.sendMessage(player, MessageKey.NOT_MARRIED);
                return;
            }
            Player partner = Bukkit.getPlayer(record.getPartner(player.getUniqueId()));
            if (partner == null) {
                messages.sendMessage(player, MessageKey.PARTNER_OFFLINE);
                return;
            }
            Map<String, Object> placeholdersSelf = new HashMap<>();
            placeholdersSelf.put("partner", partner.getName());
            placeholdersSelf.put("text", text);
            messages.sendMessage(player, MessageKey.MSG_FORMAT, placeholdersSelf);
            Map<String, Object> placeholdersPartner = new HashMap<>();
            placeholdersPartner.put("player", player.getName());
            placeholdersPartner.put("text", text);
            messages.sendMessage(partner, MessageKey.MSG_FORMAT_RECEIVED, placeholdersPartner);
            playReceivedSound(partner);
        });
    }

    private void playReceivedSound(@NotNull Player partner) {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("sounds.msg-received");
        if (section == null) {
            return;
        }
        if (!section.getBoolean("enabled", true)) {
            return;
        }
        String soundName = section.getString("sound", "ENTITY_EXPERIENCE_ORB_PICKUP");
        if (soundName == null || soundName.isBlank()) {
            return;
        }
        Sound sound;
        try {
            sound = Sound.valueOf(soundName.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            plugin.getLogger().warning("Invalid sound for sounds.msg-received: " + soundName);
            return;
        }
        float volume = (float) section.getDouble("volume", 1.0);
        float pitch = (float) section.getDouble("pitch", 1.0);
        partner.playSound(partner.getLocation(), sound, volume, pitch);
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return List.of();
    }
}
