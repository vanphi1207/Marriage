package me.ihqqq.marriage.gui.menu;

import me.ihqqq.marriage.MarriagePlugin;
import me.ihqqq.marriage.gui.component.MenuButton;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;


public abstract class AbstractMenu {

    protected final MarriagePlugin plugin;
    protected final Player viewer;
    private final int size;
    private final String title;
    private final Map<Integer, MenuButton> buttons = new HashMap<>();
    private Inventory inventory;

    public AbstractMenu(@NotNull MarriagePlugin plugin, @NotNull Player viewer, int size, @NotNull String title) {
        this.plugin = plugin;
        this.viewer = viewer;
        this.size = size;
        this.title = title;
    }

    
    protected abstract void setup();

    
    public void open() {
        if (inventory == null) {
            this.inventory = Bukkit.createInventory(null, size, MiniMessage.miniMessage().deserialize(title));
            setup();
            
            for (Map.Entry<Integer, MenuButton> entry : buttons.entrySet()) {
                int slot = entry.getKey();
                MenuButton button = entry.getValue();
                inventory.setItem(slot, button.getItem());
            }
        }
        viewer.openInventory(inventory);
    }

    
    public Inventory getInventory() {
        return inventory;
    }

    
    protected void setButton(int slot, @NotNull MenuButton button) {
        buttons.put(slot, button);
    }

    
    public void handleClick(@NotNull InventoryClickEvent event) {
        int slot = event.getRawSlot();
        if (slot >= 0 && slot < size) {
            MenuButton button = buttons.get(slot);
            if (button != null) {
                button.onClick(event);
            }
            
            event.setCancelled(true);
        } else if (!allowItemMove()) {
            
            event.setCancelled(true);
        }
    }

    
    protected boolean allowItemMove() {
        return false;
    }
}