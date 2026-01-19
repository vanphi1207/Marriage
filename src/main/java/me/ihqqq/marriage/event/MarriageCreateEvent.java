package me.ihqqq.marriage.event;

import me.ihqqq.marriage.model.MarriageRecord;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;


public class MarriageCreateEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final MarriageRecord record;

    public MarriageCreateEvent(@NotNull MarriageRecord record) {
        this.record = record;
    }

    
    public MarriageRecord getRecord() {
        return record;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}