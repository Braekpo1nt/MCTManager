package org.braekpo1nt.mctmanager.games.game.enums;

import java.util.Map;

public enum GameType {
    FOOT_RACE,
    MECHA,
    CAPTURE_THE_FLAG,
    SPLEEF,
    PARKOUR_PATHWAY, 
    CLOCKWORK;
    
    public static final Map<String, GameType> GAME_IDS;
    public static final Map<GameType, String> TITLES;
    
    static {
        GAME_IDS = Map.of(
                "foot-race", GameType.FOOT_RACE, 
                "mecha", GameType.MECHA, 
                "capture-the-flag", GameType.CAPTURE_THE_FLAG, 
                "spleef", GameType.SPLEEF, 
                "parkour-pathway", GameType.PARKOUR_PATHWAY, 
                "clockwork", GameType.CLOCKWORK);
        
        TITLES = Map.of(
                GameType.FOOT_RACE, "Foot Race", 
                GameType.MECHA, "MECHA", 
                GameType.CAPTURE_THE_FLAG, "Capture the Flag", 
                GameType.SPLEEF, "Spleef", 
                GameType.PARKOUR_PATHWAY, "Parkour Pathway", 
                GameType.CLOCKWORK, "Clockwork");
    }
    
    /**
     * @param gameType the GameType to get the title of
     * @return the user-readable title of the given GameType, or null if gameType is null
     */
    public static String getTitle(GameType gameType) {
        return TITLES.getOrDefault(gameType, null);
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
