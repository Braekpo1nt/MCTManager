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
public class FastBoardManager implements Listener {
    
    private final ConcurrentHashMap<UUID, FastBoard> boards = new ConcurrentHashMap<>();
    private final GameStateStorageUtil gameStateStorageUtil;
    
    public FastBoardManager(Main plugin, GameStateStorageUtil gameStateStorageUtil) {
        this.gameStateStorageUtil = gameStateStorageUtil;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        giveBoardsToAllOnlinePlayers();
    }
    
    public void updateMainBoard(String... mainLines) {
        Iterator<Map.Entry<UUID, FastBoard>> iterator = boards.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, FastBoard> entry = iterator.next();
            UUID playerUniqueId = entry.getKey();
            FastBoard board = entry.getValue();
            String teamName = gameStateStorageUtil.getPlayerTeamName(playerUniqueId);
            String teamDisplayName = gameStateStorageUtil.getTeamDisplayName(teamName);
            ChatColor teamChatColor = gameStateStorageUtil.getTeamChatColor(teamName);
            int score = gameStateStorageUtil.getPlayerScore(playerUniqueId);
            board.updateLine(0, teamChatColor+teamDisplayName);
            board.updateLine(1, ChatColor.GOLD+"Score: "+score);
        }
    }
    
    private void giveBoardsToAllOnlinePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (gameStateStorageUtil.containsPlayer(player.getUniqueId())) {
                addBoard(player);
            }
        }
    }
    
    private void addBoard(Player player) {
        FastBoard newBoard = new FastBoard(player);
        newBoard.updateTitle(ChatColor.BOLD+""+ChatColor.DARK_RED+"MCT Alpha");
        newBoard.updateLines(
                "",
                ""
        );
        boards.put(player.getUniqueId(), newBoard);
    }
    
    private void removeBoard(UUID playerUniqueId) {
        FastBoard board = boards.remove(playerUniqueId);
        if (board != null && !board.isDeleted()) {
            board.delete();
        }
    }
    
    @EventHandler
    public void playerQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        removeBoard(player.getUniqueId());
    }
    
    @EventHandler
    public void playerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!gameStateStorageUtil.containsPlayer(player.getUniqueId())) {
            return;
        }
        addBoard(player);
    }
    
}
