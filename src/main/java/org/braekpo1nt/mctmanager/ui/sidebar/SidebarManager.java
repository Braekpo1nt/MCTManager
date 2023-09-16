package org.braekpo1nt.mctmanager.ui.sidebar;

import org.braekpo1nt.mctmanager.ui.FastBoardWrapper;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SidebarManager {
    
    protected final List<String> orderedKeys = new ArrayList<>();
    protected final Map<String, String> lineContents = new HashMap<>();
    
    protected final Map<UUID, FastBoardWrapper> boards = new ConcurrentHashMap<>();

    public static final String DEFAULT_TITLE = ChatColor.BOLD + "" + ChatColor.DARK_RED + "MCT";
    /**
     * The title to be used at the top of the FastBoard
     */
    protected String title = DEFAULT_TITLE;
    
    /**
     * Adds a player to this SidebarManager. All existing lines will be shown to the player's FastBoard. If the player is already present, nothing happens. 
     * @param player the player to add to this manager and give a FastBoard
     */
    public synchronized void addPlayer(@NotNull Player player) {
        if (boards.containsKey(player.getUniqueId())) {
            return;
        }
        FastBoardWrapper newBoard = new FastBoardWrapper();
        newBoard.setPlayer(player);
        newBoard.updateTitle(this.title);
        boards.put(player.getUniqueId(), newBoard);
    }

    /**
     * Removes the player from this SidebarManager. If the player is not already present, nothing happens.
     * @param playerUUID the player to remove from this manager.
     */
    public synchronized void removePlayer(@NotNull UUID playerUUID) {
        if (!boards.containsKey(playerUUID)) {
            return;
        }
        FastBoardWrapper board = boards.remove(playerUUID);
        if (board != null && !board.isDeleted()) {
            board.delete();
        }
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
