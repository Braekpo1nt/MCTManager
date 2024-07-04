package org.braekpo1nt.mctmanager.ui.sidebar;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Note that a sidebar can only have 15 lines
 */
public class Sidebar {
    
    /**
     * Sidebar should not be instantiated outside the {@link SidebarFactory}. This prevents it from happening outside this package.
     */
    Sidebar() {
    }
    
    /**
     * Maps keys to their index, or their line number (starts at 0). 
     */
    protected final Map<String, Integer> keyToIndex = new HashMap<>();
    /**
     * Holds the lines for the FastBoards for each player. This is kept in parallel to the actual visual lines held in {@link Sidebar#boards} for the purposes of reordering
     */
    protected final Map<UUID, List<Component>> boardsLines = new HashMap<>();
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
    
    public synchronized void updateTitle(String title) {
        this.title = title;
        for (FastBoardWrapper board : boards.values()) {
            board.updateTitle(title);
        }
    }
    
    public synchronized void addPlayers(@NotNull List<@NotNull Player> players) {
        for (Player player : players) {
            addPlayer(player);
        }
    }
    
    /**
     * Adds a player to this Sidebar. The lines will be empty. You'll need to manually update the line contents for the new player using {@link Sidebar#updateLine(UUID, String, Component)}.
     * @param player the player to add to this manager and give a FastBoard
     */
    public synchronized void addPlayer(@NotNull Player player) {
        Preconditions.checkArgument(!boardsLines.containsKey(player.getUniqueId()), "player with UUID \"%s\" already has a board in this manager", player.getUniqueId());
        FastBoardWrapper newBoard = new FastBoardWrapper();
        newBoard.setPlayer(player);
        newBoard.updateTitle(this.title);
        boards.put(player.getUniqueId(), newBoard);
        List<Component> lines = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            lines.add(Component.empty());
        }
        boardsLines.put(player.getUniqueId(), lines);
    }
    
    /**
     * Removes all players from this Sidebar
     */
    public synchronized  void removeAllPlayers() {
        Iterator<Map.Entry<UUID, List<Component>>> iterator = boardsLines.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, List<Component>> entry = iterator.next();
            UUID playerUUID = entry.getKey();
            iterator.remove();
            FastBoardWrapper board = boards.remove(playerUUID);
            if (board != null && !board.isDeleted()) {
                board.delete();
            }
        }
    }
    
    public synchronized void removePlayers(@NotNull List<@NotNull Player> players) {
        for (Player player : players) {
            removePlayer(player);
        }
    }
    
    public synchronized void removePlayer(@NotNull Player player) {
        removePlayer(player.getUniqueId());
    }
    
    /**
     * Removes the player from this Sidebar.
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
     * Check if the sidebar contains a line with the given key
     * @param key the key to check if this sidebar contains
     * @return true if this sidebar has a line with the given key
     */
    public synchronized boolean containsKey(@NotNull String key) {
        return keyToIndex.containsKey(key);
    }
    
    /**
     * String overload of {@link #addLine(String, Component)}
     * @see #addLine(String, Component)
     */
    public synchronized void addLine(@NotNull String key, @NotNull String contents) {
        this.addLine(key, Component.text(contents));
    }
    
    /**
     * Adds a line to all FastBoards at the bottom of existing lines
     * @param key the key for the line (must not already exist)
     * @param contents the contents of the line
     */
    public synchronized void addLine(@NotNull String key, @NotNull Component contents) {
        Preconditions.checkArgument(!keyToIndex.containsKey(key), "attempted to add a line with an existing key (%s)", key);
        int index = size;
        size++;
        keyToIndex.put(key, index);
        for (Map.Entry<UUID, List<Component>> entry : boardsLines.entrySet()) {
            UUID playerUUID = entry.getKey();
            List<Component> lines = entry.getValue();
            lines.add(contents);
            FastBoardWrapper board = boards.get(playerUUID);
            board.updateLine(index, contents);
        }
    }
    
    /**
     * String overload of {@link #addLine(String, int, Component)}
     * @see #addLine(String, int, Component)
     */
    public synchronized void addLine(@NotNull String key, int index, @NotNull String contents) {
        this.addLine(key, index, Component.text(contents));
    }
    
    /**
     * Adds a line to all FastBoards at the given index. all lines that were after that index are bumped down to make room.
     * @param key the key for the line (must not already exist)
     * @param index the index of the new line (starting at 0, higher numbers go down the board, can't be greater than the size of the board).
     * @param contents the contents of the line
     * @throws IllegalArgumentException if the key exists, or the index is out of range ({@code index < 0 || index > size()})
     */
    public synchronized void addLine(@NotNull String key, int index, @NotNull Component contents) {
        Preconditions.checkArgument(!keyToIndex.containsKey(key), "can't add a line with an existing key (%s)", key);
        Preconditions.checkArgument(index >= 0, "index (%s) can't be negative", index);
        Preconditions.checkArgument(index <= size, "index (%s) can't be greater than the size (%s) of the Sidebar", index, size);
        size++;
        for (String existingKey : keyToIndex.keySet()) {
            int oldIndex = keyToIndex.get(existingKey);
            if (oldIndex >= index) {
                keyToIndex.put(existingKey, oldIndex + 1);
            }
        }
        keyToIndex.put(key, index);
        for (Map.Entry<UUID, List<Component>> entry : boardsLines.entrySet()) {
            UUID playerUUID = entry.getKey();
            List<Component> lines = entry.getValue();
            lines.add(index, contents);
            FastBoardWrapper board = boards.get(playerUUID);
            board.updateLines(lines);
        }
    }
    
    /**
     * Bulk add all the lines with the given keys to the end of the Sidebar
     * @param keyLines a list of {@link KeyLine} key-to-content pairs to add all at once to all the FastBoards
     */
    public synchronized void addLines(@NotNull KeyLine @NotNull... keyLines) {
        List<String> keys = new ArrayList<>(keyLines.length);
        List<Component> lineContents = new ArrayList<>(keyLines.length);
        for (KeyLine keyLine : keyLines) {
            Preconditions.checkArgument(!keys.contains(keyLine.getKey()), "duplicate key found in keyLines (%s)", keyLine.getKey());
            Preconditions.checkArgument(!keyToIndex.containsKey(keyLine.getKey()), "can't add a line with an existing key (%s)", keyLine.getKey());
            keys.add(keyLine.getKey());
            lineContents.add(keyLine.getContents());
        }
        for (KeyLine keyLine : keyLines) {
            int index = size;
            size++;
            keyToIndex.put(keyLine.getKey(), index);
        }
        for (Map.Entry<UUID, List<Component>> entry : boardsLines.entrySet()) {
            UUID playerUUID = entry.getKey();
            List<Component> lines = entry.getValue();
            lines.addAll(lineContents);
            FastBoardWrapper board = boards.get(playerUUID);
            board.updateLines(lines);
        }
    }
    
    /**
     * Adds the given lines to all FastBoards at the given index. All lines that were after that index are bumped down to make room.
     * @param index the index of the first new line (starting at 0, higher numbers go down the board, can't be greater than the size of the board)
     * @param keyLines the KeyLine pairs
     */
    public synchronized void addLines(int index, @NotNull KeyLine @NotNull... keyLines) {
        Preconditions.checkArgument(index >= 0, "index (%s) can't be negative", index);
        Preconditions.checkArgument(index <= size, "index (%s) can't be greater than the size (%s) of the Sidebar", index, size);
        List<String> keys = new ArrayList<>(keyLines.length);
        List<Component> lineContents = new ArrayList<>(keyLines.length);
        for (KeyLine keyLine : keyLines) {
            Preconditions.checkArgument(!keys.contains(keyLine.getKey()), "duplicate key found in keyLines (%s)", keyLine.getKey());
            Preconditions.checkArgument(!keyToIndex.containsKey(keyLine.getKey()), "can't add a line with an existing key (%s)", keyLine.getKey());
            keys.add(keyLine.getKey());
            lineContents.add(keyLine.getContents());
        }
        int indexShift = keyLines.length;
        for (String existingKey : keyToIndex.keySet()) {
            int oldIndex = keyToIndex.get(existingKey);
            if (oldIndex >= index) {
                keyToIndex.put(existingKey, oldIndex + indexShift);
            }
        }
        int putIndex = index;
        for (String key : keys) {
            keyToIndex.put(key, putIndex);
            putIndex++;
        }
        size += keyLines.length;
        for (Map.Entry<UUID, List<Component>> entry : boardsLines.entrySet()) {
            UUID playerUUID = entry.getKey();
            List<Component> lines = entry.getValue();
            lines.addAll(index, lineContents);
            FastBoardWrapper board = boards.get(playerUUID);
            board.updateLines(lines);
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
        for (Map.Entry<UUID, List<Component>> entry : boardsLines.entrySet()) {
            UUID playerUUID = entry.getKey();
            List<Component> lines = entry.getValue();
            lines.remove(removeIndex);
            FastBoardWrapper board = boards.get(playerUUID);
            board.updateLines(lines);
        }
    }
    
    /**
     * Delete the lines associated with the given keys
     * @param keys the keys for the lines to delete
     */
    public synchronized void deleteLines(@NotNull String... keys) {
        List<String> keysToDelete = new ArrayList<>(keys.length);
        for (String key : keys) {
            Preconditions.checkArgument(keyToIndex.containsKey(key), "can't delete line with nonexistent key (%s)", key);
            Preconditions.checkArgument(!keysToDelete.contains(key), "duplicate key (%S) found in keys", key);
            keysToDelete.add(key);
        }
        List<Integer> removeIndexes = new ArrayList<>(keys.length);
        for (String key : keys) {
            int removeIndex = keyToIndex.remove(key);
            size--;
            removeIndexes.add(removeIndex);
        }
        adjustValues(keyToIndex);
        removeIndexes.sort(Comparator.reverseOrder());
        for (List<Component> lines : boardsLines.values()) {
            removeIndexes(lines, removeIndexes);
        }
        for (Map.Entry<UUID, List<Component>> entry : boardsLines.entrySet()) {
            UUID playerUUID = entry.getKey();
            List<Component> lines = entry.getValue();
            FastBoardWrapper board = boards.get(playerUUID);
            board.updateLines(lines);
        }
    }
    
    /**
     * Removes the indexes in the provided list of removeIndexes from the given list of lines
     * @param lines the lines from which to remove the indexes
     * @param removeIndexes the indexes to remove from lines. Must be sorted in reverse order (higher indexes first) or this will throw {@link IndexOutOfBoundsException}
     * @throws IndexOutOfBoundsException if {@code removeIndexes} is not sorted in order from greatest to least (i.e. {@code removeIndexes[n] > removeIndexes[n+1]}
     */
    private void removeIndexes(List<Component> lines, List<Integer> removeIndexes) {
        List<Integer> reverseSortedIndexesToRemove = removeIndexes.stream().sorted(Comparator.reverseOrder()).toList();
        for (int indexToRemove : reverseSortedIndexesToRemove) {
            if (indexToRemove >= 0 && indexToRemove < lines.size()) {
                lines.remove(indexToRemove);
            }
        }
    }
    
    /**
     * Adjusts the values in a given keyToIndex by reassigning them based on their order in ascending order.
     * The keys in the map remain unchanged.
     * keyToIndex will have new values, adjusted to start from 0 and increase by 1 in ascending order according to the original values.
     * @param keyToIndex a {@link java.util.Map} where keys are strings and values are integers.
     */
    private synchronized void adjustValues(Map<String, Integer> keyToIndex) {
        List<Map.Entry<String, Integer>> sortedEntries = keyToIndex.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .toList();
        int index = 0;
        for (Map.Entry<String, Integer> entry : sortedEntries) {
            keyToIndex.put(entry.getKey(), index);
            index++;
        }
    }
    
    /**
     * Deletes all the lines from all FastBoards
     */
    public synchronized void deleteAllLines() {
        size = 0;
        keyToIndex.clear();
        for (Map.Entry<UUID, List<Component>> entry : boardsLines.entrySet()) {
            UUID playerUUID = entry.getKey();
            List<Component> lines = entry.getValue();
            lines.clear();
            FastBoardWrapper board = boards.get(playerUUID);
            board.updateLines(Collections.emptyList());
        }
    }
    
    /**
     * String overload of {@link #updateLine(String, Component)}
     * @see #updateLine(String, Component)
     */
    public synchronized void updateLine(@NotNull String key, @NotNull String contents) {
        updateLine(key, Component.text(contents));
    }
    
    /**
     * Updates the line associated with the key for all FastBoards
     * @param key the key for the line (must exist)
     * @param contents the contents of the line
     */
    public synchronized void updateLine(@NotNull String key, @NotNull Component contents) {
        Preconditions.checkArgument(keyToIndex.containsKey(key), "can't update a line with nonexistent key (%s)", key);
        int index = keyToIndex.get(key);
        for (Map.Entry<UUID, List<Component>> entry : boardsLines.entrySet()) {
            UUID playerUUID = entry.getKey();
            List<Component> lines = entry.getValue();
            lines.set(index, contents);
            FastBoardWrapper board = boards.get(playerUUID);
            board.updateLine(index, contents);
        }
    }
    
    /**
     * Updates the line associated with the KeyLine pair for all FastBoards
     * @param keyLines the KeyLine pair (all keys must exist)
     */
    public synchronized  void updateLines(@NotNull KeyLine @NotNull... keyLines) {
        for (KeyLine keyLine : keyLines) {
            Preconditions.checkArgument(keyToIndex.containsKey(keyLine.getKey()), "can't update a line with nonexistent key (%s)", keyLine.getKey());
        }
        for (Map.Entry<UUID, List<Component>> entry : boardsLines.entrySet()) {
            UUID playerUUID = entry.getKey();
            List<Component> lines = entry.getValue();
            FastBoardWrapper board = boards.get(playerUUID);
            for (KeyLine keyLine : keyLines) {
                int index = keyToIndex.get(keyLine.getKey());
                lines.set(index, keyLine.getContents());
                board.updateLine(index, keyLine.getContents());
            }
        }
    }
    
    /**
     * String overload of {@link #updateLine(UUID, String, Component)}
     * @see #updateLine(UUID, String, Component)
     */
    public synchronized void updateLine(@NotNull UUID playerUUID, @NotNull String key, @NotNull String contents) {
        updateLine(playerUUID, key, Component.text(contents));
    }
    
    /**
     * Updates the line associate with the key for the player with the given ID's FastBoard.
     * @param playerUUID the player UUID to update the line for (must have a FastBoard)
     * @param key the key for the line (must exist)
     * @param contents the contents of the line
     */
    public synchronized void updateLine(@NotNull UUID playerUUID, @NotNull String key, @NotNull Component contents) {
        Preconditions.checkArgument(keyToIndex.containsKey(key), "can't update a line with nonexistent key (%s)", key);
        Preconditions.checkArgument(boardsLines.containsKey(playerUUID), "player with UUID \"%s\" does not have a board in this manager", playerUUID);
        int index = keyToIndex.get(key);
        List<Component> lines = boardsLines.get(playerUUID);
        lines.set(index, contents);
        FastBoardWrapper board = boards.get(playerUUID);
        board.updateLine(index, contents);
    }
    
    /**
     * Updates the lines associated with the KeyLine pairs for the given player's FastBoard.
     * @param playerUUID THe player UUID to update the lines for (must have a FastBoard)
     * @param keyLines the KeyLine pairs to update (each key must exist)
     */
    public synchronized void updateLines(@NotNull UUID playerUUID, @NotNull KeyLine @NotNull... keyLines) {
        for (KeyLine keyLine : keyLines) {
            Preconditions.checkArgument(keyToIndex.containsKey(keyLine.getKey()), "can't update a line with nonexistent key (%s)", keyLine.getKey());
        }
        List<Component> lines = boardsLines.get(playerUUID);
        FastBoardWrapper board = boards.get(playerUUID);
        for (KeyLine keyLine : keyLines) {
            int index = keyToIndex.get(keyLine.getKey());
            lines.set(index, keyLine.getContents());
            board.updateLine(index, keyLine.getContents());
        }
    }
}
