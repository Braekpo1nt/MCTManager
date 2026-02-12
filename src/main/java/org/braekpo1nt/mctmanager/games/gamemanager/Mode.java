package org.braekpo1nt.mctmanager.games.gamemanager;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents different modes of the {@link GameManager}
 */
public enum Mode {
    MAINTENANCE("maintenance", Component.text("Maintenance")),
    PRACTICE("practice", Component.text("Practice")),
    EVENT("event", Component.text("Event"));
    
    private final String name;
    private final Component title;
    
    Mode(@NotNull String name, @NotNull Component title) {
        this.name = name;
        this.title = title;
    }
    
    public String getName() {
        return name;
    }
    
    public Component getTitle() {
        return title;
    }
    
    /**
     * @param name the {@link #name} of a {@link Mode}
     * @return the {@link Mode} corresponding to the given name string, or null
     * if no such {@link Mode} exists or the input name is null.
     */
    public static @Nullable Mode fromName(@Nullable String name) {
        return switch (name) {
            case "maintenance" -> MAINTENANCE;
            case "practice" -> PRACTICE;
            case "event" -> EVENT;
            case null, default -> null;
        };
    }
}
