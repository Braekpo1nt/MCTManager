package org.braekpo1nt.mctmanager.games.game.parkourpathway;

import net.kyori.adventure.text.format.NamedTextColor;

public enum NotificationMode {
    /**
     * See everyone's checkpoints
     */
    ALL,
    /**
     * See only your team's checkpoints
     */
    TEAM,
    /**
     * See only your own checkpoints
     */
    DISABLED;
    
    public static String getModeName(NotificationMode mode) {
        return switch (mode) {
            case ALL -> "All Players";
            case TEAM -> "Team Only";
            case DISABLED -> "Self Only";
        };
    }
    
    public static NamedTextColor getModeColor(NotificationMode mode) {
        return switch (mode) {
            case ALL -> NamedTextColor.GREEN;
            case TEAM -> NamedTextColor.BLUE;
            case DISABLED -> NamedTextColor.RED;
        };
    }
    
    /**
     * Cycle to next mode: ALL -> TEAM -> DISABLED -> ALL
     * @param current the mode to cycle from
     * @return the mode to cycle to
     */
    public static NotificationMode cycle(NotificationMode current) {
        return switch (current) {
            case ALL -> TEAM;
            case TEAM -> DISABLED;
            case DISABLED -> ALL;
        };
    }
}
