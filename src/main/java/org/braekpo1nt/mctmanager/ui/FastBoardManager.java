package org.braekpo1nt.mctmanager.ui;

import org.braekpo1nt.mctmanager.games.gamestate.GameStateStorageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Responsible for displaying the MCT sidebar FastBoard
 * Utilizes the <a href="https://github.com/MrMicky-FR/FastBoard">FastBoard api</a>
 */
public class FastBoardManager {
    
    protected final String EVENT_TITLE = ChatColor.BOLD + "" + ChatColor.DARK_RED + "MCT #3";
    protected final ConcurrentHashMap<UUID, FastBoardWrapper> boards = new ConcurrentHashMap<>();
    protected GameStateStorageUtil gameStateStorageUtil;
    
    public FastBoardManager(GameStateStorageUtil gameStateStorageUtil) {
        this.gameStateStorageUtil = gameStateStorageUtil;
    }
    
    public synchronized void updateMainBoards() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateMainBoardForPlayer(player);
        }
    }
    
    protected synchronized void updateMainBoardForPlayer(Player player) {
        boolean playerHasBoard = givePlayerBoardIfAbsent(player);
        if (!playerHasBoard) {
            return;
        }

        UUID playerUniqueId = player.getUniqueId();
        FastBoardWrapper board = boards.get(playerUniqueId);
        String[] mainLines = getMainLines(playerUniqueId);

        for (int i = 0; i < mainLines.length; i++) {
            board.updateLine(i, mainLines[i]);
        }
    }
    
    protected String[] getMainLines(UUID playerUniqueId) {
        String teamName = gameStateStorageUtil.getPlayerTeamName(playerUniqueId);
        String teamDisplayName = gameStateStorageUtil.getTeamDisplayName(teamName);
        ChatColor teamChatColor = gameStateStorageUtil.getTeamChatColor(teamName);
        int teamScore = gameStateStorageUtil.getTeamScore(teamName);
        int playerScore = gameStateStorageUtil.getPlayerScore(playerUniqueId);
        String teamLine = teamChatColor+teamDisplayName+": "+teamScore;
        String scoreLine = ChatColor.GOLD+"Points: "+playerScore;
        return new String[]{teamLine, scoreLine};
    }

    private String[] combineFastBoardLines(String[]... arrays) {
        int totalLength = 0;
        for (String[] array : arrays) {
            totalLength += array.length;
        }

        String[] newArray = new String[totalLength];
        int currentIndex = 0;

        for (String[] array : arrays) {
            System.arraycopy(array, 0, newArray, currentIndex, array.length);
            currentIndex += array.length;
        }

        return newArray;
    }
    
    /**
     * Gives the player a FastBoard if the player doesn't already have one,
     * and the player is in the gameStateStorageUtil
     * @param player The player
     * @return true if the player got a board or already has a board, false if not
     */
    protected synchronized boolean givePlayerBoardIfAbsent(Player player) {
        UUID playerUniqueId = player.getUniqueId();
        if (boards.containsKey(playerUniqueId)) {
            return true;
        }
        if (gameStateStorageUtil.containsPlayer(playerUniqueId)) {
            addBoard(player);
            return true;
        }
        return false;
    }
    
    protected synchronized void addBoard(Player player) {
        FastBoardWrapper newBoard = new FastBoardWrapper();
        newBoard.setPlayer(player);
        newBoard.updateTitle(this.EVENT_TITLE);
        String[] mainLines = getMainLines(player.getUniqueId());
        newBoard.updateLines(mainLines);
        boards.put(player.getUniqueId(), newBoard);
    }
    
    /**
     * Updates the sub-board lines (after the main lines) for the given player
     * using the given lines. Provide no lines arguments to clear. 
     * @param playerUniqueId The player UUID to update the sub-board for
     * @param lines The lines to update the sub-board to
     */
    public synchronized void updateLines(UUID playerUniqueId, String... lines) {
        if (!boards.containsKey(playerUniqueId)) {
            return;
        }
        FastBoardWrapper board = boards.get(playerUniqueId);
        String[] mainLines = getMainLines(playerUniqueId);
        String[] linesPlusMainLines = new String[lines.length + mainLines.length];
        System.arraycopy(mainLines, 0, linesPlusMainLines, 0, mainLines.length);
        System.arraycopy(lines, 0, linesPlusMainLines, mainLines.length, lines.length);
        board.updateLines(linesPlusMainLines);
    }
    
    /**
     * Updates the sub-board line (after the main lines) for the given player
     * using the given text.
     * @param playerUniqueId The player UUID to update the sub-line for
     * @param line The line index of the sub-line (0 being the first)
     * @param text The text to update the line with
     */
    public synchronized void updateLine(UUID playerUniqueId, int line, String text) {
        if (!boards.containsKey(playerUniqueId)) {
            return;
        }
        FastBoardWrapper board = boards.get(playerUniqueId);
        String[] mainLines = getMainLines(playerUniqueId);
        int subLine = line + mainLines.length;
        board.updateLine(subLine, text);
    }
    
    public synchronized void removeBoard(UUID playerUniqueId) {
        if (!boards.containsKey(playerUniqueId)) {
            return;
        }
        FastBoardWrapper board = boards.remove(playerUniqueId);
        if (board != null && !board.isDeleted()) {
            board.delete();
        }
    }
    
    public synchronized void removeAllBoards() {
        Iterator<Map.Entry<UUID, FastBoardWrapper>> iterator = boards.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, FastBoardWrapper> entry = iterator.next();
            FastBoardWrapper board = entry.getValue();
            if (!board.isDeleted()) {
                board.delete();
            }
            iterator.remove();
        }
    }
    
    // Test methods
    
    public void setGameStateStorageUtil(GameStateStorageUtil gameStateStorageUtil) {
        this.gameStateStorageUtil = gameStateStorageUtil;
    }
    
}
