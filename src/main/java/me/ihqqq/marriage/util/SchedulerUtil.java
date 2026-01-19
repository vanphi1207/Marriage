package me.ihqqq.marriage.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;


public class SchedulerUtil {

    private final JavaPlugin plugin;

    public SchedulerUtil(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
    }

    
    public void runSync(@NotNull Runnable task) {
        Bukkit.getScheduler().runTask(plugin, task);
    }

    
    public void runAsync(@NotNull Runnable task) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }

    
    public void runSyncLater(@NotNull Runnable task, long delay) {
        Bukkit.getScheduler().runTaskLater(plugin, task, delay);
    }

    
    public void runAsyncLater(@NotNull Runnable task, long delay) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delay);
    }
}