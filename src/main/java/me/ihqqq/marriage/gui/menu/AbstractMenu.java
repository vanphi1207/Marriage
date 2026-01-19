package me.ihqqq.marriage.gui.menu;

import me.ihqqq.marriage.MarriagePlugin;
import me.ihqqq.marriage.gui.component.MenuButton;
import me.ihqqq.marriage.util.ItemBuilder;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
            if (!allowItemMove()) {
                applyFillers();
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

    private void applyFillers() {
        ConfigurationSection fillersSection = plugin.getGuiConfig().getConfigurationSection("gui.fillers");
        if (fillersSection == null) {
            return;
        }
        for (String key : fillersSection.getKeys(false)) {
            ConfigurationSection fillerSection = fillersSection.getConfigurationSection(key);
            if (fillerSection == null) {
                continue;
            }
            ItemStack fillerItem = buildFillerItem(fillerSection);
            if (fillerItem == null) {
                continue;
            }
            List<Integer> slots = fillerSection.getIntegerList("slots");
            if (slots.isEmpty()) {
                for (int slot = 0; slot < size; slot++) {
                    setFillerIfEmpty(slot, fillerItem);
                }
            } else {
                for (int slot : slots) {
                    setFillerIfEmpty(slot, fillerItem);
                }
            }
        }
    }

    private void setFillerIfEmpty(int slot, @NotNull ItemStack fillerItem) {
        if (slot < 0 || slot >= size || buttons.containsKey(slot)) {
            return;
        }
        ItemStack current = inventory.getItem(slot);
        if (current == null || current.getType() == Material.AIR) {
            inventory.setItem(slot, fillerItem);
        }
    }

    private ItemStack buildFillerItem(@NotNull ConfigurationSection section) {
        String materialName = section.getString("material", "GRAY_STAINED_GLASS_PANE");
        Material material = Material.matchMaterial(materialName.trim().toUpperCase(Locale.ROOT));
        if (material == null) {
            material = Material.GRAY_STAINED_GLASS_PANE;
        }
        ItemBuilder builder = new ItemBuilder(material);
        int amount = section.getInt("amount", 1);
        if (amount > 0) {
            builder.amount(amount);
        }
        String name = section.getString("name");
        if (name != null && !name.isBlank()) {
            builder.name(name);
        }
        List<String> lore = section.getStringList("lore");
        if (!lore.isEmpty()) {
            builder.lore(lore);
        }
        return builder.build();
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
