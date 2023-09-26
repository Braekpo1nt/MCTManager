package org.braekpo1nt.mctmanager.ui;

import org.braekpo1nt.mctmanager.games.gamestate.GameStateStorageUtil;
import org.braekpo1nt.mctmanager.ui.sidebar.FastBoardWrapper;
import org.braekpo1nt.mctmanager.ui.sidebar.SidebarFactory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Responsible for displaying the MCT sidebar FastBoard
 * Utilizes the <a href="https://github.com/MrMicky-FR/FastBoard">FastBoard api</a>
 * @deprecated This is deprecated in favor of {@link SidebarFactory}
 */
@Deprecated
public class FastBoardManager {
    
    public static final String DEFAULT_TITLE = ChatColor.BOLD + "" + ChatColor.DARK_RED + "MCT";
    /**
     * The event title to be used at the top of the sidebar
     */
    protected String title = DEFAULT_TITLE;
    protected final ConcurrentHashMap<UUID, FastBoardWrapper> boards = new ConcurrentHashMap<>();
    /**
     * Used to store for each user which header type to show. See {@link HeaderType} 
     */
    protected final ConcurrentHashMap<UUID, HeaderType> headerTypes = new ConcurrentHashMap<>();
    protected GameStateStorageUtil gameStateStorageUtil;
    
    public FastBoardManager(GameStateStorageUtil gameStateStorageUtil) {
        this.gameStateStorageUtil = gameStateStorageUtil;
    }
    
    /**
     * Set the title for all the FastBoards 
     * @param title the new title
     */
    public synchronized void updateTitle(String title) {
        this.title = title;
        for (FastBoardWrapper board : boards.values()) {
            board.updateTitle(title);
        }
    }
    
    public synchronized void updateHeaders() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateHeaderForPlayer(player);
        }
    }
    
    protected synchronized void updateHeaderForPlayer(Player player) {
        boolean playerHasBoard = givePlayerBoardIfAbsent(player);
        if (!playerHasBoard) {
            return;
        }
        
        UUID playerUniqueId = player.getUniqueId();
        FastBoardWrapper board = boards.get(playerUniqueId);
        HeaderType headerType = headerTypes.get(playerUniqueId);
        String[] header = getHeader(playerUniqueId, headerType);
        
        for (int i = 0; i < header.length; i++) {
            board.updateLine(i, header[i]);
        }
    }
    
    /**
     * Retrieve the header 
     * @param playerUniqueId the player
     * @param headerType the header type
     * @return the header for the given player with the given type
     */
    protected String[] getHeader(UUID playerUniqueId, HeaderType headerType) {
        if (headerType == HeaderType.ALL) {
            List<String> header = new ArrayList<>();
            Set<String> teamNames = gameStateStorageUtil.getTeamNames();
            List<String> sortedTeamNames = sortTeamNames(teamNames);
            for (String teamName : sortedTeamNames) {
                String teamDisplayName = gameStateStorageUtil.getTeamDisplayName(teamName);
                ChatColor teamChatColor = gameStateStorageUtil.getTeamChatColor(teamName);
                int teamScore = gameStateStorageUtil.getTeamScore(teamName);
                String teamLine = teamChatColor+teamDisplayName+": "+teamScore;
                header.add(teamLine);
            }
            header.add("");
            int playerScore = gameStateStorageUtil.getPlayerScore(playerUniqueId);
            String scoreLine = ChatColor.GOLD+"Points: "+playerScore;
            header.add(scoreLine);
            return header.toArray(String[]::new);
        }
        
        String teamName = gameStateStorageUtil.getPlayerTeamName(playerUniqueId);
        String teamDisplayName = gameStateStorageUtil.getTeamDisplayName(teamName);
        ChatColor teamChatColor = gameStateStorageUtil.getTeamChatColor(teamName);
        int teamScore = gameStateStorageUtil.getTeamScore(teamName);
        int playerScore = gameStateStorageUtil.getPlayerScore(playerUniqueId);
        String teamLine = teamChatColor+teamDisplayName+": "+teamScore;
        String scoreLine = ChatColor.GOLD+"Points: "+playerScore;
        return new String[]{teamLine, scoreLine};
    }
    
    protected List<String> sortTeamNames(Set<String> teamNames) {
        List<String> sortedTeamNames = new ArrayList<>(teamNames);
        sortedTeamNames.sort(Comparator.comparing(teamName -> gameStateStorageUtil.getTeamScore(teamName), Comparator.reverseOrder()));
        return sortedTeamNames;
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
        newBoard.updateTitle(this.title);
        String[] header = getHeader(player.getUniqueId(), HeaderType.PERSONAL);
        newBoard.updateLines(header);
        boards.put(player.getUniqueId(), newBoard);
        headerTypes.put(player.getUniqueId(), HeaderType.PERSONAL);
    }
    
    /**
     * Set the header type for the player's FastBoard
     * @param playerUniqueId The player
     * @param headerType the header type
     */
    public synchronized void setHeaderType(UUID playerUniqueId, HeaderType headerType) {
        if (!headerTypes.containsKey(playerUniqueId)) {
            return;
        }
        headerTypes.put(playerUniqueId, headerType);
    }
    
    /**
     * Updates the sub-board lines (after the header) for the given player
     * using the given lines. Provide no lines arguments to clear. 
     * @param playerUniqueId The player UUID to update the sub-board for
     * @param lines The lines to update the sub-board to
     */
    public synchronized void updateLines(UUID playerUniqueId, String... lines) {
        if (!boards.containsKey(playerUniqueId)) {
            return;
        }
        FastBoardWrapper board = boards.get(playerUniqueId);
        HeaderType headerType = headerTypes.get(playerUniqueId);
        String[] header = getHeader(playerUniqueId, headerType);
        String[] linesPlusHeader = new String[lines.length + header.length];
        System.arraycopy(header, 0, linesPlusHeader, 0, header.length);
        System.arraycopy(lines, 0, linesPlusHeader, header.length, lines.length);
        board.updateLines(linesPlusHeader);
    }
    
    /**
     * Updates the sub-board line (after the header) for the given player
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
        HeaderType headerType = headerTypes.get(playerUniqueId);
        String[] header = getHeader(playerUniqueId, headerType);
        int subLine = line + header.length;
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
        headerTypes.remove(playerUniqueId);
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
