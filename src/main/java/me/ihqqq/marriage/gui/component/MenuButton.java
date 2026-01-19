package me.ihqqq.marriage.gui.component;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;


public class MenuButton {

    private final ItemStack item;
    private final BiConsumer<InventoryClickEvent, MenuButton> action;

    
    public MenuButton(@NotNull ItemStack item, @NotNull BiConsumer<InventoryClickEvent, MenuButton> action) {
        this.item = item;
        this.action = action;
    }

    
    public ItemStack getItem() {
        return item;
    }

    
    public void onClick(@NotNull InventoryClickEvent event) {
        action.accept(event, this);
    }
}