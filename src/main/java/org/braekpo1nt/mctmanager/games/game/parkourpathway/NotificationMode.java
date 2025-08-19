package org.braekpo1nt.mctmanager.games.game.parkourpathway;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;

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
    
    public static List<Component> getToggleLore(NotificationMode mode) {
        return switch (mode) {
            case ALL -> List.of(
                    Component.text("Checkpoint Notifications: ").color(NamedTextColor.GRAY)
                            .append(Component.text("All Players").color(NamedTextColor.GREEN)),
                    Component.text("You see when anyone").color(NamedTextColor.GRAY),
                    Component.text("reaches checkpoints").color(NamedTextColor.GRAY),
                    Component.text("Right click to change").color(NamedTextColor.YELLOW)
            );
            case TEAM -> List.of(
                    Component.text("Checkpoint Notifications: ").color(NamedTextColor.GRAY)
                            .append(Component.text("Team Only").color(NamedTextColor.BLUE)),
                    Component.text("You see when you or your").color(NamedTextColor.GRAY),
                    Component.text("teammates reach checkpoints").color(NamedTextColor.GRAY),
                    Component.text("Right click to change").color(NamedTextColor.YELLOW)
            );
            case DISABLED -> List.of(
                    Component.text("Checkpoint Notifications: ").color(NamedTextColor.GRAY)
                            .append(Component.text("Self Only").color(NamedTextColor.RED)),
                    Component.text("You only see your own").color(NamedTextColor.GRAY),
                    Component.text("checkpoint progress").color(NamedTextColor.GRAY),
                    Component.text("Right click to change").color(NamedTextColor.YELLOW)
            );
            default -> List.of(Component.text("Right click to change").color(NamedTextColor.YELLOW));
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
            default -> ALL;
        };
    }
}
