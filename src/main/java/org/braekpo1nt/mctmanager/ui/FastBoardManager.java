package org.braekpo1nt.mctmanager.ui;

import fr.mrmicky.fastboard.FastBoard;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.gamestate.GameStateStorageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Responsible for displaying the MCT sidebar FastBoard
 * Utilizes the <a href="https://github.com/MrMicky-FR/FastBoard">FastBoard api</a>
 */
public class FastBoardManager {
    
    private final ConcurrentHashMap<UUID, FastBoard> boards = new ConcurrentHashMap<>();
    private final GameStateStorageUtil gameStateStorageUtil;
    
    public FastBoardManager(GameStateStorageUtil gameStateStorageUtil) {
        this.gameStateStorageUtil = gameStateStorageUtil;
    }
    
    public synchronized void updateMainBoard() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!givePlayerBoardIfAbsent(player)) {
                return;
            }
            UUID playerUniqueId = player.getUniqueId();
            FastBoard board = boards.get(playerUniqueId);
            String[] mainLines = getMainLines(playerUniqueId);
            String teamLine = mainLines[0];
            String scoreLine = mainLines[1];
            board.updateLine(0, teamLine);
            board.updateLine(1, scoreLine);
        }
    }
    
    /**
     * Gives the player a FastBoard if the player doesn't already have one,
     * and the player is in the gameStateStorageUtil
     * @param player The player
     * @return true if the player got a board, false if not
     */
    public synchronized boolean givePlayerBoardIfAbsent(Player player) {
        UUID playerUniqueId = player.getUniqueId();
        if (!boards.containsKey(playerUniqueId)) {
            if (gameStateStorageUtil.containsPlayer(playerUniqueId)) {
                addBoard(player);
                return true;
            }
        }
        return false;
    }
    
    private String[] getMainLines(UUID playerUniqueId) {
        String teamName = gameStateStorageUtil.getPlayerTeamName(playerUniqueId);
        String teamDisplayName = gameStateStorageUtil.getTeamDisplayName(teamName);
        ChatColor teamChatColor = gameStateStorageUtil.getTeamChatColor(teamName);
        int score = gameStateStorageUtil.getPlayerScore(playerUniqueId);
        String teamLine = teamChatColor+teamDisplayName;
        String scoreLine = ChatColor.GOLD+"Score: "+score;
        return new String[]{teamLine, scoreLine};
    }
    
    /**
     * Updates the sub-board lines (after the main lines) for the given player
     * using the given lines. Provide no lines arguments to clear. 
     * @param player The player to update the sub-board for
     * @param lines The lines to update the sub-board to
     */
    public synchronized void updateLines(Player player, String... lines) {
        if (!givePlayerBoardIfAbsent(player)) {
            return;
        }
        UUID playerUniqueId = player.getUniqueId();
        FastBoard board = boards.get(playerUniqueId);
        String[] mainLines = getMainLines(playerUniqueId);
        String[] linesPlusMainLines = new String[lines.length + 2];
        linesPlusMainLines[0] = mainLines[0];
        linesPlusMainLines[1] = mainLines[1];
        System.arraycopy(lines, 0, linesPlusMainLines, 2, lines.length);
        board.updateLines(linesPlusMainLines);
    }
    
    /**
     * Updates the sub-board line (after the main lines) for the given player
     * using the given text.
     * @param player The player to update the sub-line for
     * @param line The line index of the sub-line (0 being the first)
     * @param text The text to update the line with
     */
    public synchronized void updateLine(Player player, int line, String text) {
        if (!givePlayerBoardIfAbsent(player)) {
            return;
        }
        UUID playerUniqueId = player.getUniqueId();
        FastBoard board = boards.get(playerUniqueId);
        int subLine = line + 2;
        board.updateLine(subLine, text);
    }
    
    private synchronized void addBoard(Player player) {
        FastBoard newBoard = new FastBoard(player);
        newBoard.updateTitle(ChatColor.BOLD+""+ChatColor.DARK_RED+"MCT Alpha");
        newBoard.updateLines(
                "",
                ""
        );
        boards.put(player.getUniqueId(), newBoard);
    }
    
    public synchronized void removeBoard(UUID playerUniqueId) {
        FastBoard board = boards.remove(playerUniqueId);
        if (board != null && !board.isDeleted()) {
            board.delete();
        }
    }
    
    public synchronized void removeAllBoards() {
        Iterator<Map.Entry<UUID, FastBoard>> iterator = boards.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, FastBoard> entry = iterator.next();
            FastBoard board = entry.getValue();
            if (!board.isDeleted()) {
                board.delete();
            }
            iterator.remove();
        }
    }
    
}
