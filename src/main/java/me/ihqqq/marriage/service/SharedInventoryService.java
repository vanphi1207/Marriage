package me.ihqqq.marriage.service;

import me.ihqqq.marriage.MarriagePlugin;
import me.ihqqq.marriage.model.MarriageRecord;
import me.ihqqq.marriage.storage.Storage;
import me.ihqqq.marriage.util.ItemStackSerializer;
import me.ihqqq.marriage.util.SchedulerUtil;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;


public class SharedInventoryService {

    private final MarriagePlugin plugin;
    private final Storage storage;
    private final SchedulerUtil scheduler;
    private final ConcurrentHashMap<String, Inventory> inventoryCache = new ConcurrentHashMap<>();

    public SharedInventoryService(@NotNull MarriagePlugin plugin) {
        this.plugin = plugin;
        this.storage = plugin.getStorage();
        this.scheduler = plugin.getSchedulerUtil();
    }

    public CompletableFuture<Inventory> getInventory(@NotNull MarriageRecord record, @NotNull String title) {
        String key = cacheKey(record.getUuidA(), record.getUuidB());
        Inventory cached = inventoryCache.get(key);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }
        return storage.loadSharedInventory(record.getUuidA(), record.getUuidB())
                .thenCompose(data -> {
                    CompletableFuture<Inventory> future = new CompletableFuture<>();
                    scheduler.runSync(() -> {
                        Inventory inventory = Bukkit.createInventory(null, 54,
                                MiniMessage.miniMessage().deserialize(title));
                        if (data != null && !data.isBlank()) {
                            try {
                                ItemStack[] items = ItemStackSerializer.deserialize(data);
                                int limit = Math.min(items.length, inventory.getSize());
                                for (int i = 0; i < limit; i++) {
                                    inventory.setItem(i, items[i]);
                                }
                            } catch (IllegalStateException ex) {
                                plugin.getLogger().warning("Failed to load shared inventory: " + ex.getMessage());
                            }
                        }
                        inventoryCache.put(key, inventory);
                        future.complete(inventory);
                    });
                    return future;
                });
    }

    public CompletableFuture<Void> saveInventory(@NotNull MarriageRecord record, @NotNull Inventory inventory,
                                                 @NotNull Set<Integer> reservedSlots) {
        ItemStack[] contents = Arrays.copyOf(inventory.getContents(), inventory.getSize());
        for (int slot : reservedSlots) {
            if (slot >= 0 && slot < contents.length) {
                contents[slot] = null;
            }
        }
        boolean empty = true;
        for (ItemStack item : contents) {
            if (item != null && item.getType() != Material.AIR) {
                empty = false;
                break;
            }
        }
        if (empty) {
            return storage.saveSharedInventory(record.getUuidA(), record.getUuidB(), null);
        }
        try {
            String data = ItemStackSerializer.serialize(contents);
            return storage.saveSharedInventory(record.getUuidA(), record.getUuidB(), data);
        } catch (IllegalStateException ex) {
            plugin.getLogger().warning("Failed to save shared inventory: " + ex.getMessage());
            return CompletableFuture.completedFuture(null);
        }
    }

    private String cacheKey(@NotNull UUID uuidA, @NotNull UUID uuidB) {
        return uuidA + ":" + uuidB;
    }
}
