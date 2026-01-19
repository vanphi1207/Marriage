package me.ihqqq.marriage.model;

import org.bukkit.Location;

import java.util.UUID;


public class MarriageRecord {
    private final UUID uuidA;
    private final UUID uuidB;
    private final long since;
    private final Location home;
    private final MarriageSettings settings;

    
    public MarriageRecord(UUID uuidA, UUID uuidB, long since, Location home, MarriageSettings settings) {
        this.uuidA = uuidA;
        this.uuidB = uuidB;
        this.since = since;
        this.home = home;
        this.settings = settings;
    }

    public UUID getUuidA() {
        return uuidA;
    }

    public UUID getUuidB() {
        return uuidB;
    }

    public long getSince() {
        return since;
    }

    public Location getHome() {
        return home;
    }

    public MarriageSettings getSettings() {
        return settings;
    }

    
    public UUID getPartner(UUID uuid) {
        if (uuid == null) return null;
        if (uuid.equals(uuidA)) return uuidB;
        if (uuid.equals(uuidB)) return uuidA;
        return null;
    }
}