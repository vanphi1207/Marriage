package me.ihqqq.marriage.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;


public class MarriageTeleportEvent extends Event implements Cancellable {

    public enum Cause {
        PARTNER, HOME
    }

    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private Location destination;
    private final Cause cause;
    private boolean cancelled;

    public MarriageTeleportEvent(@NotNull Player player, @NotNull Location destination, @NotNull Cause cause) {
        this.player = player;
        this.destination = destination;
        this.cause = cause;
    }

    
    public Player getPlayer() {
        return player;
    }

    
    public Location getDestination() {
        return destination;
    }

    
    public void setDestination(@NotNull Location destination) {
        this.destination = destination;
    }

    
    public Cause getCause() {
        return cause;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}