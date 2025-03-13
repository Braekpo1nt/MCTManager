package org.braekpo1nt.mctmanager.utils;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public enum LogType {
    CANCEL_ENTITY_DAMAGE_EVENT("cancelEntityDamageEvent"), 
    CANCEL_PLAYER_DEATH_EVENT("cancelPlayerDeathEvent"),
    EVENT_UPDATE_SCORES("eventUpdateScores");
    
    private final String id;
    
    private static final Map<String, LogType> BY_ID = new HashMap<>();
    private static final List<String> ALL_IDS = new ArrayList<>();
    
    static {
        for (LogType logType : LogType.values()) {
            BY_ID.put(logType.id, logType);
            ALL_IDS.add(logType.id);
        }
    }
    
    LogType(String id) {
        this.id = id;
    }
    
    /**
     * @param id the ID to get the {@link LogType} associated with
     * @return the {@link LogType} associated with the given ID, or null if no such {@link LogType} exists. 
     */
    public static @Nullable LogType byId(@NotNull String id) {
        return BY_ID.get(id);
    }
    
    public static List<String> getIDs() {
        return ALL_IDS;
    }
}
