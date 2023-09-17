package org.braekpo1nt.mctmanager.ui.sidebar;

import com.google.common.base.Preconditions;
import org.braekpo1nt.mctmanager.ui.FastBoardWrapper;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SidebarManager {

    /**
     * Maps keys to their index, or their line number (starts at 0). 
     */
    protected final Map<String, Integer> keyToIndex = new HashMap<>();
    /**
     * Holds the lines for the FastBoards for each player. This is kept in parallel to the actual visual lines held in {@link SidebarManager#boards} for the purposes of reordering
     */
    protected final Map<UUID, List<String>> boardsLines = new HashMap<>();
    /**
     * The size of the Sidebar, or the number of lines in it
     */
    protected int size = 0;
    /**
     * The actual FastBoards, which handle the display of information to the player
     */
    protected final Map<UUID, FastBoardWrapper> boards = new HashMap<>();
    public static final String DEFAULT_TITLE = ChatColor.BOLD + "" + ChatColor.DARK_RED + "MCT";
    /**
     * The title to be used at the top of the FastBoard
     */
    protected String title = DEFAULT_TITLE;
    
    /**
     * Adds a player to this SidebarManager. The lines will be empty. You'll need to manually update the line contents for the new player using {@link SidebarManager#updateLine(UUID, String, String)}.
     * @param player the player to add to this manager and give a FastBoard
     */
    public synchronized void addPlayer(@NotNull Player player) {
        Preconditions.checkArgument(!boardsLines.containsKey(player.getUniqueId()), "player with UUID \"%s\" already has a board in this manager", player.getUniqueId());
        FastBoardWrapper newBoard = new FastBoardWrapper();
        newBoard.setPlayer(player);
        newBoard.updateTitle(this.title);
        boards.put(player.getUniqueId(), newBoard);
        List<String> lines = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            lines.add("");
        }
        boardsLines.put(player.getUniqueId(), lines);
    }
    
    /**
     * Removes the player from this SidebarManager.
     * @param playerUUID the player to remove from this manager (must be present in the manager)
     * @throws IllegalArgumentException if the playerUUID doesn't exist in this manager
     */
    public synchronized void removePlayer(@NotNull UUID playerUUID) {
        Preconditions.checkArgument(boardsLines.containsKey(playerUUID), "player with UUID \"%s\" does not have a board in this manager", playerUUID);
        boardsLines.remove(playerUUID);
        FastBoardWrapper board = boards.remove(playerUUID);
        if (board != null && !board.isDeleted()) {
            board.delete();
        }
    }
    
    /**
     * Adds a line to all FastBoards at the bottom of existing lines
     * @param key the key for the line (must not already exist)
     * @param contents the contents of the line
     */
    public synchronized void addLine(@NotNull String key, @NotNull String contents) {
        Preconditions.checkArgument(!keyToIndex.containsKey(key), "attempted to add a line with an existing key (%s)", key);
        int index = size;
        size++;
        keyToIndex.put(key, index);
        for (Map.Entry<UUID, List<String>> entry : boardsLines.entrySet()) {
            UUID playerUUID = entry.getKey();
            List<String> lines = entry.getValue();
            lines.add(contents);
            FastBoardWrapper board = boards.get(playerUUID);
            board.updateLine(index, contents);
        }
    }
    
    /**
     * Adds a line to all FastBoards at the given index. all lines that were after that index are bumped down to make room.
     * @param key the key for the line (must not already exist)
     * @param index the index of the line (starting at 0, higher numbers go down the board).
     * @param contents the contents of the line
     * @throws IllegalArgumentException if the key exists, or the index is out of range ({@code index < 0 || index > size()})
     */
    public synchronized void addLine(@NotNull String key, int index, @NotNull String contents) {
        Preconditions.checkArgument(!keyToIndex.containsKey(key), "can't add a line with an existing key (%s)", key);
        Preconditions.checkArgument(index >= 0, "index (%s) can't be negative", index);
        Preconditions.checkArgument(index < size, "index (%s) can't be greater than or equal to the size (%s) of the Sidebar", index, size);
        size++;
        for (String existingKey : keyToIndex.keySet()) {
            int oldIndex = keyToIndex.get(existingKey);
            if (oldIndex >= index) {
                keyToIndex.put(existingKey, oldIndex + 1);
            }
        }
        keyToIndex.put(key, index);
        for (Map.Entry<UUID, List<String>> entry : boardsLines.entrySet()) {
            UUID playerUUID = entry.getKey();
            List<String> lines = entry.getValue();
            lines.add(index, contents);
            FastBoardWrapper board = boards.get(playerUUID);
            board.updateLines(lines.toArray(new String[0]));
        }
    }
    
    /**
     * Deletes the line from all FastBoards
     * @param key the key for the line (must exist)
     */
    public synchronized void deleteLine(@NotNull String key) {
        Preconditions.checkArgument(keyToIndex.containsKey(key), "can't delete line with nonexistent key (%s)", key);
        int removeIndex = keyToIndex.remove(key);
        size--;
        for (String existingKey : keyToIndex.keySet()) {
            int oldIndex = keyToIndex.get(existingKey);
            if (oldIndex >= removeIndex) {
                keyToIndex.put(existingKey, oldIndex - 1);
            }
        }
        for (Map.Entry<UUID, List<String>> entry : boardsLines.entrySet()) {
            UUID playerUUID = entry.getKey();
            List<String> lines = entry.getValue();
            lines.remove(removeIndex);
            FastBoardWrapper board = boards.get(playerUUID);
            board.updateLines(lines.toArray(new String[0]));
        }
    }

    /**
     * Deletes all the lines from all FastBoards
     */
    public synchronized void deleteAllLines() {
        size = 0;
        keyToIndex.clear();
        boardsLines.clear();
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
        for (Map.Entry<UUID, List<String>> entry : boardsLines.entrySet()) {
            UUID playerUUID = entry.getKey();
            List<String> lines = entry.getValue();
            lines.set(index, contents);
            FastBoardWrapper board = boards.get(playerUUID);
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
        Preconditions.checkArgument(boardsLines.containsKey(playerUUID), "player with UUID \"%s\" does not have a board in this manager", playerUUID);
        int index = keyToIndex.get(key);
        List<String> lines = boardsLines.get(playerUUID);
        lines.set(index, contents);
        FastBoardWrapper board = boards.get(playerUUID);
        board.updateLine(index, contents);
    }
    
}
