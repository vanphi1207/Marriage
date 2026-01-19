package me.ihqqq.marriage.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public final class MarriageSettings {
    private final boolean allowTeleport;
    private final boolean chatEnabled;
    private final Map<String, Object> flags;

    
    public MarriageSettings(boolean allowTeleport, boolean chatEnabled, Map<String, Object> flags) {
        this.allowTeleport = allowTeleport;
        this.chatEnabled = chatEnabled;
        this.flags = flags == null ? Collections.emptyMap() : Collections.unmodifiableMap(new HashMap<>(flags));
    }

    
    public boolean isAllowTeleport() {
        return allowTeleport;
    }

    
    public boolean isChatEnabled() {
        return chatEnabled;
    }

    
    public Map<String, Object> getFlags() {
        return flags;
    }
}