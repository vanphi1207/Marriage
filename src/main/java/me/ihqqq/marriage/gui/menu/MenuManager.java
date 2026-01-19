package me.ihqqq.marriage.gui.menu;

import me.ihqqq.marriage.MarriagePlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class MenuManager implements Listener {

    private final MarriagePlugin plugin;
    private final Map<UUID, AbstractMenu> openMenus = new ConcurrentHashMap<>();

    public MenuManager(@NotNull MarriagePlugin plugin) {
        this.plugin = plugin;
    }

    
    public void openMenu(@NotNull Player player, @NotNull AbstractMenu menu) {
        openMenus.put(player.getUniqueId(), menu);
        menu.open();
    }

    @EventHandler
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        AbstractMenu menu = openMenus.get(player.getUniqueId());
        if (menu == null) {
            return;
        }
        if (event.getView().getTopInventory().equals(menu.getInventory())) {
            menu.handleClick(event);
        }
    }

    @EventHandler
    public void onInventoryClose(@NotNull InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        AbstractMenu menu = openMenus.remove(player.getUniqueId());
        if (menu != null) {
            menu.handleClose(event);
        }
    }
}
