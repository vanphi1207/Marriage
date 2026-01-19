package me.ihqqq.marriage.service;

import me.ihqqq.marriage.MarriagePlugin;
import me.ihqqq.marriage.event.MarriageTeleportEvent;
import me.ihqqq.marriage.message.MessageKey;
import me.ihqqq.marriage.message.MessageService;
import me.ihqqq.marriage.model.MarriageSettings;
import me.ihqqq.marriage.util.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;


public class TeleportService {

    private final MarriagePlugin plugin;
    private final MarriageService marriageService;
    private final MessageService messages;
    private final SchedulerUtil scheduler;

    public TeleportService(@NotNull MarriagePlugin plugin, @NotNull MarriageService marriageService, @NotNull MessageService messages) {
        this.plugin = plugin;
        this.marriageService = marriageService;
        this.messages = messages;
        this.scheduler = plugin.getSchedulerUtil();
    }

    
    public void teleportToPartner(@NotNull Player player) {
        UUID uuid = player.getUniqueId();
        marriageService.getMarriage(uuid).thenAccept(record -> {
            if (record == null) {
                scheduler.runSync(() -> messages.sendMessage(player, MessageKey.NOT_MARRIED));
                return;
            }
            
            MarriageSettings settings = record.getSettings();
            if (!plugin.getMarriageSettings().isAllowTeleport() || !settings.isAllowTeleport()) {
                scheduler.runSync(() -> messages.sendMessage(player, MessageKey.TP_DISABLED));
                return;
            }
            Player partner = Bukkit.getPlayer(record.getPartner(uuid));
            if (partner == null) {
                scheduler.runSync(() -> messages.sendMessage(player, MessageKey.PARTNER_OFFLINE));
                return;
            }
            
            scheduler.runSync(() -> {
                Location dest = partner.getLocation();
                MarriageTeleportEvent event = new MarriageTeleportEvent(player, dest, MarriageTeleportEvent.Cause.PARTNER);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    return;
                }
                player.teleport(dest);
                messages.sendMessage(player, MessageKey.TP_SUCCESS);
            });
        });
    }

    
    public void setHome(@NotNull Player player) {
        marriageService.setHome(player);
    }

    
    public void teleportHome(@NotNull Player player) {
        UUID uuid = player.getUniqueId();
        marriageService.getMarriage(uuid).thenAccept(record -> {
            if (record == null) {
                scheduler.runSync(() -> messages.sendMessage(player, MessageKey.NOT_MARRIED));
                return;
            }
            
            if (!plugin.getMarriageSettings().isAllowTeleport() || !record.getSettings().isAllowTeleport()) {
                scheduler.runSync(() -> messages.sendMessage(player, MessageKey.TP_DISABLED));
                return;
            }
            Location home = record.getHome();
            if (home == null) {
                scheduler.runSync(() -> messages.sendMessage(player, MessageKey.HOME_NOT_SET));
                return;
            }
            scheduler.runSync(() -> {
                MarriageTeleportEvent event = new MarriageTeleportEvent(player, home, MarriageTeleportEvent.Cause.HOME);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    return;
                }
                player.teleport(home);
                messages.sendMessage(player, MessageKey.HOME_SUCCESS);
            });
        });
    }
}