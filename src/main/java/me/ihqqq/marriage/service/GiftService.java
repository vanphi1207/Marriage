package me.ihqqq.marriage.service;

import me.ihqqq.marriage.MarriagePlugin;
import me.ihqqq.marriage.gui.menu.GiftMenu;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public class GiftService {

    private final MarriagePlugin plugin;

    public GiftService(@NotNull MarriagePlugin plugin) {
        this.plugin = plugin;
    }

    
    public void openGiftMenu(@NotNull Player player) {
        plugin.getMenuManager().openMenu(player, new GiftMenu(plugin, player));
    }
}