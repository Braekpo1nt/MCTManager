package org.braekpo1nt.mctmanager.games.game.parkourpathway.chat;

import net.kyori.adventure.text.format.NamedTextColor;

public enum ChatMode {
    /**
     * See messages from all players
     */
    TEAM,
    /**
     * See only your own checkpoints
     */
    OFF,
    /**
     * See everyone's checkpoints
     */
    ALL;
    
    public static String getModeName(ChatMode mode) {
        return switch (mode) {
            case TEAM -> "Team";
            case OFF -> "Self Only";
            case ALL -> "All Players";
        };
    }
    
    public static NamedTextColor getModeColor(ChatMode mode) {
        return switch (mode) {
            case TEAM -> NamedTextColor.BLUE;
            case OFF -> NamedTextColor.RED;
            case ALL -> NamedTextColor.GREEN;
        };
    }
    
    /**
     * Cycle to next mode: ALL -> TEAM -> OFF -> ALL
     * modes)
     * @param current the mode to cycle from
     * @return the mode to cycle to
     */
    public static ChatMode cycle(ChatMode current) {
        return switch (current) {
            case ALL -> TEAM;
            case TEAM -> OFF;
            case OFF -> ALL;
        };
    }
}
