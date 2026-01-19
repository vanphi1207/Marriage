package me.ihqqq.marriage.service;

import me.ihqqq.marriage.MarriagePlugin;
import me.ihqqq.marriage.gui.menu.GiftMenu;
import me.ihqqq.marriage.message.MessageKey;
import me.ihqqq.marriage.message.MessageService;
import me.ihqqq.marriage.model.MarriageRecord;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public class GiftService {

    private final MarriagePlugin plugin;
    private final MarriageService marriageService;
    private final MessageService messages;
    private final SharedInventoryService sharedInventoryService;

    public GiftService(@NotNull MarriagePlugin plugin) {
        this.plugin = plugin;
        this.marriageService = plugin.getMarriageService();
        this.messages = plugin.getMessageService();
        this.sharedInventoryService = plugin.getSharedInventoryService();
    }

    
    public void openGiftMenu(@NotNull Player player) {
        marriageService.getMarriage(player.getUniqueId()).thenAccept(record -> {
            if (record == null) {
                plugin.getSchedulerUtil().runSync(() -> messages.sendMessage(player, MessageKey.NOT_MARRIED));
                return;
            }
            openSharedInventory(player, record);
        });
    }

    private void openSharedInventory(@NotNull Player player, @NotNull MarriageRecord record) {
        sharedInventoryService.getInventory(record, GiftMenu.TITLE).thenAccept(inventory -> {
            plugin.getSchedulerUtil().runSync(() -> plugin.getMenuManager()
                    .openMenu(player, new GiftMenu(plugin, player, record, inventory)));
        });
    }
}
