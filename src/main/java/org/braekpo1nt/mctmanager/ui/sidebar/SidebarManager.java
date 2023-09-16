package org.braekpo1nt.mctmanager.ui.sidebar;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class SidebarManager {
    
    
    /**
     * Adds a player to this SidebarManager. All existing lines will be shown to the player's FastBoard.
     * @param player the player to add to this manager and give a FastBoard (must not already be in this manager)
     */
    public synchronized void addPlayer(@NotNull Player player) {
        
    }
    
    public synchronized void removePlayer(@NotNull Player player) {
        
    }
    
    /**
     * Adds a line to all FastBoards at the given position (counted from the top). all lines that were after that position are bumped down to make room.
     * @param key the key for the line (must not already exist)
     * @param position the position (or line number) for the line, starting from the top. Must be within the size of the line. 
     * @param contents the contents of the line (null will be empty)
     */
    public synchronized void addLine(@NotNull String key, int position, @Nullable String contents) {
        
    }

    /**
     * Adds a line to all FastBoards at the bottom of existing lines
     * @param key the key for the line (must not already exist)
     * @param contents the contents of the line (null will be empty)
     */
    public synchronized void addLine(@NotNull String key, @Nullable String contents) {
        
    }

    /**
     * Deletes the line from all FastBoards
     * @param key the key for the line (must exist)
     */
    public synchronized void deleteLine(@NotNull String key) {
        
    }

    /**
     * Deletes the lines with the given keys from all FastBoards
     * @param keys the keys for the lines (each key must exist)
     */
    public synchronized void deleteLines(@NotNull String... keys) {
        
    }

    /**
     * Updates the line associated with the key for all FastBoards
     * @param key the key for the line (must exist)
     * @param contents the contents of the line (null will be empty)
     */
    public synchronized void updateLine(@NotNull String key, @Nullable String contents) {
        
    }

    /**
     * Updates the line associate with the key for the player with the given ID's FastBoard.
     * @param playerUUID the player UUID to update the line for (must have a FastBoard)
     * @param key the key for the line (must exist)
     * @param contents the contents of the line (null will be empty)
     */
    public synchronized void updateLine(@NotNull UUID playerUUID, @NotNull String key, @Nullable String contents) {
        
    }
    
}
