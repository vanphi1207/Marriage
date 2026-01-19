package me.ihqqq.marriage.service;

import me.ihqqq.marriage.MarriagePlugin;
import me.ihqqq.marriage.message.MessageKey;
import me.ihqqq.marriage.message.MessageService;
import me.ihqqq.marriage.model.MarriageRecord;
import me.ihqqq.marriage.util.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;


public class RequestService implements Listener {

    private final MarriagePlugin plugin;
    private final MarriageService marriageService;
    private final MessageService messages;
    private final SchedulerUtil scheduler;
    private final long timeoutMillis;
    private final long cooldownMillis;

    private static class PendingRequest {
        final UUID proposer;
        final UUID target;
        final long expiresAt;

        PendingRequest(UUID proposer, UUID target, long expiresAt) {
            this.proposer = proposer;
            this.target = target;
            this.expiresAt = expiresAt;
        }
    }

    
    private final Map<UUID, PendingRequest> byTarget = new ConcurrentHashMap<>();
    
    private final Map<UUID, PendingRequest> byProposer = new ConcurrentHashMap<>();
    
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    public RequestService(@NotNull MarriagePlugin plugin, @NotNull MarriageService marriageService, @NotNull MessageService messages) {
        this.plugin = plugin;
        this.marriageService = marriageService;
        this.messages = messages;
        this.scheduler = plugin.getSchedulerUtil();
        long timeoutSeconds = plugin.getConfig().getLong("request.timeoutSeconds", 60);
        long cooldownSeconds = plugin.getConfig().getLong("request.cooldownSeconds", 300);
        this.timeoutMillis = timeoutSeconds * 1000L;
        this.cooldownMillis = cooldownSeconds * 1000L;
    }

    
    public void propose(@NotNull Player proposer, @NotNull Player target) {
        UUID pId = proposer.getUniqueId();
        UUID tId = target.getUniqueId();
        if (pId.equals(tId)) {
            messages.sendMessage(proposer, MessageKey.PROPOSE_SELF);
            return;
        }
        
        Long last = cooldowns.get(pId);
        if (last != null && System.currentTimeMillis() - last < cooldownMillis) {
            messages.sendMessage(proposer, MessageKey.PROPOSE_COOLDOWN);
            return;
        }
        
        if (byProposer.containsKey(pId) || byTarget.containsKey(tId)) {
            messages.sendMessage(proposer, MessageKey.PROPOSE_ALREADY_SENT, Map.of("player", target.getName()));
            return;
        }
        
        CompletableFuture.allOf(marriageService.getMarriage(pId), marriageService.getMarriage(tId)).thenAccept(v -> {
            boolean proposerMarried = marriageService.isMarried(pId).join();
            boolean targetMarried = marriageService.isMarried(tId).join();
            if (proposerMarried) {
                
                MarriageRecord rec = marriageService.getMarriage(pId).join();
                String partnerName = rec != null ? Bukkit.getOfflinePlayer(rec.getPartner(pId)).getName() : "";
                scheduler.runSync(() -> messages.sendMessage(proposer, MessageKey.ALREADY_MARRIED, Map.of("partner", partnerName)));
                return;
            }
            if (targetMarried) {
                scheduler.runSync(() -> messages.sendMessage(proposer, MessageKey.TARGET_ALREADY_MARRIED, Map.of("player", target.getName())));
                return;
            }
            long expires = System.currentTimeMillis() + timeoutMillis;
            PendingRequest req = new PendingRequest(pId, tId, expires);
            byProposer.put(pId, req);
            byTarget.put(tId, req);
            cooldowns.put(pId, System.currentTimeMillis());
            
            scheduler.runSync(() -> {
                messages.sendMessage(proposer, MessageKey.PROPOSE_SENT, Map.of("player", target.getName()));
                messages.sendMessage(target, MessageKey.PROPOSE_RECEIVED, Map.of("player", proposer.getName()));
            });
            
            scheduler.runSyncLater(() -> {
                PendingRequest pending = byTarget.get(tId);
                if (pending != null && pending.expiresAt <= System.currentTimeMillis()) {
                    byTarget.remove(tId);
                    byProposer.remove(pId);
                }
            }, (int) (timeoutMillis / 50L));
        });
    }

    
    public void accept(@NotNull Player target) {
        UUID tId = target.getUniqueId();
        PendingRequest req = byTarget.remove(tId);
        if (req == null) {
            messages.sendMessage(target, MessageKey.NO_PROPOSAL);
            return;
        }
        byProposer.remove(req.proposer);
        Player proposer = Bukkit.getPlayer(req.proposer);
        if (proposer == null) {
            
            messages.sendMessage(target, MessageKey.PLAYER_NOT_FOUND, Map.of("player", Bukkit.getOfflinePlayer(req.proposer).getName()));
            return;
        }
        
        marriageService.createMarriage(proposer, target);
    }

    
    public void deny(@NotNull Player target) {
        UUID tId = target.getUniqueId();
        PendingRequest req = byTarget.remove(tId);
        if (req == null) {
            messages.sendMessage(target, MessageKey.NO_PROPOSAL);
            return;
        }
        byProposer.remove(req.proposer);
        Player proposer = Bukkit.getPlayer(req.proposer);
        
        messages.sendMessage(target, MessageKey.DENY_SUCCESS, Map.of("player", proposer != null ? proposer.getName() : Bukkit.getOfflinePlayer(req.proposer).getName()));
        if (proposer != null) {
            messages.sendMessage(proposer, MessageKey.PROPOSE_DENIED, Map.of("player", target.getName()));
        }
    }

    
    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        
        PendingRequest reqProposer = byProposer.remove(uuid);
        if (reqProposer != null) {
            byTarget.remove(reqProposer.target);
        }
        
        PendingRequest reqTarget = byTarget.remove(uuid);
        if (reqTarget != null) {
            byProposer.remove(reqTarget.proposer);
        }
    }
}