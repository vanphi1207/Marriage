package me.ihqqq.marriage.service;

import me.ihqqq.marriage.MarriagePlugin;
import me.ihqqq.marriage.event.MarriageCreateEvent;
import me.ihqqq.marriage.event.MarriageDivorceEvent;
import me.ihqqq.marriage.message.MessageKey;
import me.ihqqq.marriage.message.MessageService;
import me.ihqqq.marriage.model.MarriageRecord;
import me.ihqqq.marriage.storage.Storage;
import me.ihqqq.marriage.util.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;


public class MarriageService implements Listener {

    private final MarriagePlugin plugin;
    private final Storage storage;
    private final MessageService messages;
    private final SchedulerUtil scheduler;
    
    private final Map<UUID, MarriageRecord> cache = new ConcurrentHashMap<>();
    
    private final Set<UUID> chatToggled = ConcurrentHashMap.newKeySet();

    
    public MarriageService(@NotNull MarriagePlugin plugin, @NotNull Storage storage, @NotNull MessageService messages) {
        this.plugin = plugin;
        this.storage = storage;
        this.messages = messages;
        this.scheduler = plugin.getSchedulerUtil();
    }

    
    public CompletableFuture<Boolean> isMarried(@NotNull UUID uuid) {
        MarriageRecord record = cache.get(uuid);
        if (record != null) {
            return CompletableFuture.completedFuture(true);
        }
        return storage.findByPlayer(uuid).thenApply(Objects::nonNull);
    }

    
    public CompletableFuture<MarriageRecord> getMarriage(@NotNull UUID uuid) {
        MarriageRecord cached = cache.get(uuid);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }
        return storage.findByPlayer(uuid).thenApply(record -> {
            if (record != null) {
                cache.put(record.getUuidA(), record);
                cache.put(record.getUuidB(), record);
            }
            return record;
        });
    }

    
    public void createMarriage(@NotNull Player a, @NotNull Player b) {
        
        CompletableFuture.allOf(getMarriage(a.getUniqueId()), getMarriage(b.getUniqueId())).thenAccept(v -> {
            
            if (cache.containsKey(a.getUniqueId()) || cache.containsKey(b.getUniqueId())) {
                
                scheduler.runSync(() -> {
                    messages.sendMessage(a, MessageKey.ALREADY_MARRIED, Map.of("partner", b.getName()));
                    messages.sendMessage(b, MessageKey.ALREADY_MARRIED, Map.of("partner", a.getName()));
                });
                return;
            }
            
            MarriageRecord record = new MarriageRecord(a.getUniqueId(), b.getUniqueId(), System.currentTimeMillis(), null, plugin.getMarriageSettings());
            
            storage.saveMarriage(record).thenRun(() -> {
                
                cache.put(a.getUniqueId(), record);
                cache.put(b.getUniqueId(), record);
                
                scheduler.runSync(() -> {
                    MarriageCreateEvent event = new MarriageCreateEvent(record);
                    Bukkit.getPluginManager().callEvent(event);
                    
                    messages.sendMessage(a, MessageKey.ACCEPT_SUCCESS, Map.of("player", b.getName()));
                    messages.sendMessage(b, MessageKey.ACCEPT_SUCCESS, Map.of("player", a.getName()));
                });
            });
        });
    }

    
    public void divorce(@NotNull Player player) {
        UUID uuid = player.getUniqueId();
        getMarriage(uuid).thenAccept(record -> {
            if (record == null) {
                scheduler.runSync(() -> messages.sendMessage(player, MessageKey.NOT_MARRIED));
                return;
            }
            UUID partnerId = record.getPartner(uuid);
            
            storage.deleteMarriage(uuid).thenRun(() -> {
                cache.remove(uuid);
                cache.remove(partnerId);
                scheduler.runSync(() -> {
                    MarriageDivorceEvent event = new MarriageDivorceEvent(record);
                    Bukkit.getPluginManager().callEvent(event);
                    
                    messages.sendMessage(player, MessageKey.DIVORCE_SUCCESS, Map.of("player", Bukkit.getOfflinePlayer(partnerId).getName()));
                    Player partner = Bukkit.getPlayer(partnerId);
                    if (partner != null) {
                        messages.sendMessage(partner, MessageKey.DIVORCE_SUCCESS, Map.of("player", player.getName()));
                    }
                });
            });
        });
    }

    
    public void divorceForce(@NotNull UUID target, @NotNull CommandSender sender) {
        getMarriage(target).thenAccept(record -> {
            if (record == null) {
                scheduler.runSync(() -> messages.sendMessage(sender, MessageKey.NOT_MARRIED));
                return;
            }
            UUID partner = record.getPartner(target);
            storage.deleteMarriage(target).thenRun(() -> {
                cache.remove(record.getUuidA());
                cache.remove(record.getUuidB());
                scheduler.runSync(() -> {
                    MarriageDivorceEvent event = new MarriageDivorceEvent(record);
                    Bukkit.getPluginManager().callEvent(event);
                    messages.sendMessage(sender, MessageKey.ADMIN_DIVORCE_SUCCESS, Map.of("player", Bukkit.getOfflinePlayer(target).getName()));
                    Player player = Bukkit.getPlayer(target);
                    if (player != null) {
                        messages.sendMessage(player, MessageKey.DIVORCE_SUCCESS, Map.of("player", Bukkit.getOfflinePlayer(partner).getName()));
                    }
                    Player partnerOnline = Bukkit.getPlayer(partner);
                    if (partnerOnline != null) {
                        messages.sendMessage(partnerOnline, MessageKey.DIVORCE_SUCCESS, Map.of("player", Bukkit.getOfflinePlayer(target).getName()));
                    }
                });
            });
        });
    }

    
    public void forceMarriage(@NotNull UUID a, @NotNull UUID b, @NotNull CommandSender sender) {
        
        CompletableFuture<Void> removeExisting = CompletableFuture.allOf(
                storage.deleteMarriage(a),
                storage.deleteMarriage(b)
        );
        removeExisting.thenRun(() -> {
            
            MarriageRecord record = new MarriageRecord(a, b, System.currentTimeMillis(), null, plugin.getMarriageSettings());
            storage.saveMarriage(record).thenRun(() -> {
                cache.put(a, record);
                cache.put(b, record);
                scheduler.runSync(() -> {
                    MarriageCreateEvent event = new MarriageCreateEvent(record);
                    Bukkit.getPluginManager().callEvent(event);
                    messages.sendMessage(sender, MessageKey.ADMIN_SET_SUCCESS, Map.of("player", Bukkit.getOfflinePlayer(a).getName(), "player2", Bukkit.getOfflinePlayer(b).getName()));
                    
                    Player pa = Bukkit.getPlayer(a);
                    Player pb = Bukkit.getPlayer(b);
                    if (pa != null) {
                        messages.sendMessage(pa, MessageKey.ACCEPT_SUCCESS, Map.of("player", Bukkit.getOfflinePlayer(b).getName()));
                    }
                    if (pb != null) {
                        messages.sendMessage(pb, MessageKey.ACCEPT_SUCCESS, Map.of("player", Bukkit.getOfflinePlayer(a).getName()));
                    }
                });
            });
        });
    }

    
    public void setHome(@NotNull Player player) {
        UUID uuid = player.getUniqueId();
        getMarriage(uuid).thenAccept(record -> {
            if (record == null) {
                scheduler.runSync(() -> messages.sendMessage(player, MessageKey.NOT_MARRIED));
                return;
            }
            
            MarriageRecord updated = new MarriageRecord(record.getUuidA(), record.getUuidB(), record.getSince(), player.getLocation(), record.getSettings());
            storage.saveMarriage(updated).thenRun(() -> {
                cache.put(updated.getUuidA(), updated);
                cache.put(updated.getUuidB(), updated);
                scheduler.runSync(() -> messages.sendMessage(player, MessageKey.SETHOME_SUCCESS));
            });
        });
    }

    
    public boolean toggleChat(@NotNull UUID uuid) {
        if (chatToggled.contains(uuid)) {
            chatToggled.remove(uuid);
            return false;
        } else {
            chatToggled.add(uuid);
            return true;
        }
    }

    
    public boolean isChatGloballyEnabled() {
        return plugin.getMarriageSettings().isChatEnabled();
    }

    
    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        chatToggled.remove(event.getPlayer().getUniqueId());
    }

    
    @EventHandler
    public void onAsyncPlayerChat(@NotNull AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!isChatGloballyEnabled() || !chatToggled.contains(uuid)) {
            return;
        }
        getMarriage(uuid).thenAccept(record -> {
            if (record == null) {
                
                chatToggled.remove(uuid);
                return;
            }
            UUID partnerId = record.getPartner(uuid);
            Player partner = Bukkit.getPlayer(partnerId);
            if (partner == null) {
                scheduler.runSync(() -> messages.sendMessage(player, MessageKey.PARTNER_OFFLINE));
                return;
            }
            String text = event.getMessage();
            
            event.setCancelled(true);
            scheduler.runSync(() -> {
                
                Map<String, Object> placeholdersSelf = new HashMap<>();
                placeholdersSelf.put("partner", partner.getName());
                placeholdersSelf.put("text", text);
                messages.sendMessage(player, MessageKey.MSG_FORMAT, placeholdersSelf);
                Map<String, Object> placeholdersPartner = new HashMap<>();
                placeholdersPartner.put("player", player.getName());
                placeholdersPartner.put("text", text);
                messages.sendMessage(partner, MessageKey.MSG_FORMAT_RECEIVED, placeholdersPartner);
            });
        });
    }
}