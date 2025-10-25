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
    OFF;

    public static String getModeName(ChatMode mode) {
        return switch (mode) {
            case LOCAL -> "Local";
            case TEAM -> "Team";
            case OFF -> "Off";
        };
    }

    public static NamedTextColor getModeColor(ChatMode mode) {
        return switch (mode) {
            case LOCAL -> NamedTextColor.GREEN;
            case TEAM -> NamedTextColor.BLUE;
            case OFF -> NamedTextColor.RED;
        };
    }

    /**
     * Cycle to next mode: LOCAL -> TEAM -> OFF -> LOCAL
     * @param current the mode to cycle from
     * @return the mode to cycle to
     */
    public static ChatMode cycle(ChatMode current) {
        return switch (current) {
            case LOCAL -> TEAM;
            case TEAM -> OFF;
            case OFF -> LOCAL;
        };
    }
}
