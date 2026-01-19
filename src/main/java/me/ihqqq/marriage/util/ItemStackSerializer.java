package me.ihqqq.marriage.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;


public final class ItemStackSerializer {

    private ItemStackSerializer() {
    }

    public static String serialize(ItemStack[] items) {
        if (items == null || items.length == 0) {
            return null;
        }
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {
            dataOutput.writeInt(items.length);
            for (ItemStack item : items) {
                dataOutput.writeObject(item);
            }
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException("Unable to serialize items", e);
        }
    }

    public static ItemStack[] deserialize(String data) {
        if (data == null || data.isBlank()) {
            return new ItemStack[0];
        }
        byte[] bytes = Base64.getDecoder().decode(data);
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
             BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {
            int size = dataInput.readInt();
            ItemStack[] items = new ItemStack[size];
            for (int i = 0; i < size; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }
            return items;
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("Unable to deserialize items", e);
        }
    }
}
