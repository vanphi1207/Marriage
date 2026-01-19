package me.ihqqq.marriage.command.subcommand;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public interface SubCommand {

    
    @NotNull
    String getName();

    
    @NotNull
    default List<String> getAliases() {
        return List.of();
    }

    
    @NotNull
    String getPermission();

    
    default boolean isPlayerOnly() {
        return true;
    }

    
    void execute(@NotNull CommandSender sender, @NotNull String[] args);

    
    @NotNull
    default List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return List.of();
    }
}