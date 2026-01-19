package me.ihqqq.marriage.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class ItemBuilder {

    private final ItemStack item;
    private final MiniMessage mini = MiniMessage.miniMessage();

    public ItemBuilder(@NotNull Material material) {
        this.item = new ItemStack(material);
    }

    public ItemBuilder amount(int amount) {
        item.setAmount(amount);
        return this;
    }

    public ItemBuilder name(@NotNull String miniMessage) {
        ItemMeta meta = item.getItemMeta();
        meta.displayName(mini.deserialize(miniMessage));
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder lore(@NotNull List<String> lines) {
        ItemMeta meta = item.getItemMeta();
        List<Component> lore = new ArrayList<>();
        for (String line : lines) {
            lore.add(mini.deserialize(line));
        }
        meta.lore(lore);
        item.setItemMeta(meta);
        return this;
    }

    public ItemStack build() {
        return item;
    }
}