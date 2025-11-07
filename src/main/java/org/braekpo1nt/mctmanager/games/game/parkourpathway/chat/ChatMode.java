package org.braekpo1nt.mctmanager.games.game.parkourpathway.chat;

import net.kyori.adventure.text.format.NamedTextColor;

public enum ChatMode {
    /**
     * Only see messages from players on your team
     */
    LOCAL,
    /**
     * See messages from all players
     */
    TEAM,
    /**
     * Don't see any player messages
     */
    OFF,
    /**
     * See everyone's checkpoints
     */
    ALL,
    /**
     * See only your own checkpoints
     */
    DISABLED;
    
    public static String getModeName(ChatMode mode) {
        return switch (mode) {
            case LOCAL -> "All";
            case TEAM -> "Team";
            case OFF -> "Off";
            case ALL -> "All Players";
            case DISABLED -> "Self Only";
        };
    }
    
    public static NamedTextColor getModeColor(ChatMode mode) {
        return switch (mode) {
            case LOCAL -> NamedTextColor.GREEN;
            case TEAM -> NamedTextColor.BLUE;
            case OFF -> NamedTextColor.RED;
            case ALL -> NamedTextColor.GREEN;
            case DISABLED -> NamedTextColor.RED;
        };
    }
    
    /**
     * Cycle to next mode: LOCAL -> TEAM -> OFF -> LOCAL (for chat modes) or ALL -> DISABLED -> ALL (for notification
     * modes)
     * @param current the mode to cycle from
     * @return the mode to cycle to
     */
    public static ChatMode cycle(ChatMode current) {
        return switch (current) {
            case LOCAL -> TEAM;
            case TEAM -> OFF;
            case OFF -> LOCAL;
            case ALL -> DISABLED;
            case DISABLED -> ALL;
        };
    }
}
