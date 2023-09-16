package org.braekpo1nt.mctmanager.ui.sidebar;

import com.google.common.base.Preconditions;
import org.braekpo1nt.mctmanager.ui.FastBoardWrapper;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SidebarManager {
    
    protected final Map<String, Integer> keyToIndex = new HashMap<>();
    protected final List<String> lines = new ArrayList<>();
    protected final Map<UUID, Map<String, String>> personalLines = new HashMap<>();
    
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
        Preconditions.checkArgument(!keyToIndex.containsKey(key), "can't add a line with an existing key (%s)", key);
        Preconditions.checkArgument(position >= 0, "position (%s) can't be negative", position);
        Preconditions.checkArgument(position < lines.size(), "position (%s) can't be greater than the size of the sidebar", position);
        lines.add(contents);
        keyToIndex.put(key, lines.size() - 1);
        String[] linesArray = lines.toArray(new String[0]);
        for (FastBoardWrapper board : boards.values()) {
            board.updateLines(linesArray);
        }
    }
    
    /**
     * Adds a line to all FastBoards at the bottom of existing lines
     * @param key the key for the line (must not already exist)
     * @param contents the contents of the line
     */
    public synchronized void addLine(@NotNull String key, @NotNull String contents) {
        Preconditions.checkArgument(!keyToIndex.containsKey(key), "attempted to add a line with an existing key (%s)", key);
        lines.add(contents);
        int index = lines.size() - 1;
        keyToIndex.put(key, index);
        for (FastBoardWrapper board : boards.values()) {
            board.updateLine(index, contents);
        }
    }
    
    /**
     * Deletes the line from all FastBoards
     * @param key the key for the line (must exist)
     */
    public synchronized void deleteLine(@NotNull String key) {
        Preconditions.checkArgument(keyToIndex.containsKey(key), "can't delete line with nonexistent key (%s)", key);
        int index = keyToIndex.remove(key);
        lines.remove(index);
        String[] linesArray = lines.toArray(new String[0]);
        for (FastBoardWrapper board : boards.values()) {
            board.updateLines(linesArray);
        }
    }

    /**
     * Deletes the lines with the given keys from all FastBoards
     * @param keys the keys for the lines (each key must exist)
     */
    public synchronized void deleteLines(@NotNull String... keys) {
        for (String key : keys) {
            Preconditions.checkArgument(keyToIndex.containsKey(key), "can't delete line with nonexistent key (%s)", key);
        }
        for (String key : keys) {
            int index = keyToIndex.remove(key);
            lines.remove(index);
        }
        String[] linesArray = lines.toArray(new String[0]);
        for (FastBoardWrapper board : boards.values()) {
            board.updateLines(linesArray);
        }
    }

    /**
     * Deletes all the lines from all FastBoards
     */
    public synchronized void deleteAllLines() {
        lines.clear();
        keyToIndex.clear();
        for (FastBoardWrapper board : boards.values()) {
            board.updateLines();
        }
    }

    /**
     * Updates the line associated with the key for all FastBoards
     * @param key the key for the line (must exist)
     * @param contents the contents of the line
     */
    public synchronized void updateLine(@NotNull String key, @NotNull String contents) {
        Preconditions.checkArgument(keyToIndex.containsKey(key), "can't update a line with nonexistent key (%s)", key);
        int index = keyToIndex.get(key);
        lines.set(index, contents);
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
        Preconditions.checkArgument(keyToIndex.containsKey(key), "can't update a line with nonexistent key (%s)", key);
        Preconditions.checkArgument(boards.containsKey(playerUUID), "player with UUID \"%s\" does not have a board in this manager", playerUUID);
        Map<String, String> personalContents = personalLines.get(playerUUID);
        personalContents.put(key, contents);
        FastBoardWrapper board = boards.get(playerUUID);
        int index = keyToIndex.get(key);
        board.updateLine(index, contents);
    }
    
}
