package org.braekpo1nt.mctmanager.games.game.enums;

import java.util.HashMap;
import java.util.Map;

public enum GameType {
    FOOT_RACE("Foot Race", "foot-race"),
    MECHA("MECHA", "mecha"),
    CAPTURE_THE_FLAG("Capture the Flag", "capture-the-flag"),
    SPLEEF("Spleef", "spleef"),
    PARKOUR_PATHWAY("Parkour Pathway", "parkour-pathway"),
    CLOCKWORK("Clockwork", "clockwork");
    
    GameType(String title, String id) {
        this.title = title;
        this.id = id;
    }
    
    private final String title;
    private final String id;
    
    public String getTitle() {
        return title;
    }
    
    public static final Map<String, GameType> GAME_IDS;
    
    static {
        Map<String, GameType> gameIds = new HashMap<>();
        for (GameType gameType : GameType.values()) {
            gameIds.put(gameType.id, gameType);
        }
        GAME_IDS = Map.copyOf(gameIds);
    }
    
    /**
     * A convenience method for translating command-writable strings into the given game type.
     * This is used in multiple cases, but most importantly when a user is writing a command
     * which needs to specify a game type, the user-written string must be a valid key in this
     * map in order to retrieve a valid GameType. 
     * <p> 
     * For example, if the ID is "foot-race", this will return {@link GameType#FOOT_RACE}.
     * 
     * @param id The string ID of the game type
     * @return The game type matching the given Command ID, or null if the given
     * string is not a recognized ID or is null
     */
    public static GameType fromID(String id) {
        return GAME_IDS.getOrDefault(id, null);
    }
}
