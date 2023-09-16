package org.braekpo1nt.mctmanager.ui.sidebar;

import com.google.common.base.Preconditions;
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
     * @param position the position of the line from the top (starting at 0).
     * @param contents the contents of the line
     * @throws IllegalArgumentException if the key exists, or the position is out of range ({@code index < 0 || index > size()})
     */
    public synchronized void addLine(@NotNull String key, int position, @NotNull String contents) {
        Preconditions.checkArgument(!orderedKeys.contains(key), "can't add a line with an existing key (%s)", key);
        Preconditions.checkArgument(position >= 0, "position (%s) can't be negative", position);
        Preconditions.checkArgument(position < orderedKeys.size(), "position (%s) can't be greater than the size of the sidebar", position);
        orderedKeys.add(position, key);
        lineContents.put(key, contents);
        for (FastBoardWrapper board : boards.values()) {
            for (int i = position; i < orderedKeys.size(); i++) {
                board.updateLine(i, lineContents.get(orderedKeys.get(i)));
            }
        }
    }
    
    /**
     * Adds a line to all FastBoards at the bottom of existing lines
     * @param key the key for the line (must not already exist)
     * @param contents the contents of the line
     */
    public synchronized void addLine(@NotNull String key, @NotNull String contents) {
        Preconditions.checkArgument(!orderedKeys.contains(key), "attempted to add a line with an existing key (%s)", key);
        orderedKeys.add(key);
        int index = orderedKeys.size() - 1;
        lineContents.put(key, contents);
        for (FastBoardWrapper board : boards.values()) {
            board.updateLine(index, contents);
        }
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
     * @param contents the contents of the line
     */
    public synchronized void updateLine(@NotNull String key, @NotNull String contents) {
        Preconditions.checkArgument(orderedKeys.contains(key), "can't update a line with nonexistent key (%s)", key);
        int index = orderedKeys.indexOf(key);
        for (FastBoardWrapper board : boards.values()) {
            board.updateLine(index, contents);
        }
    }

    /**
     * Updates the line associate with the key for the player with the given ID's FastBoard.
     * @param playerUUID the player UUID to update the line for (must have a FastBoard)
     * @param key the key for the line (must exist)
     * @param contents the contents of the line
     */
    public synchronized void updateLine(@NotNull UUID playerUUID, @NotNull String key, @NotNull String contents) {
        Preconditions.checkArgument(lineContents.containsKey(key), "can't update a line with nonexistent key (%s)", key);
        Preconditions.checkArgument(boards.containsKey(playerUUID),  "can't find board for player with UUID (%s)", playerUUID);
        int index = orderedKeys.indexOf(key);
        FastBoardWrapper board = boards.get(playerUUID);
        board.updateLine(index, contents);
    }
    
}
