package org.braekpo1nt.mctmanager.ui;

import fr.mrmicky.fastboard.FastBoard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

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
    private final GameManager gameManager;
    
    public FastBoardManager(Main plugin, GameManager gameManager) {
        this.gameManager = gameManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        giveBoardsToAllOnlinePlayers();
        new BukkitRunnable() {
            @Override
            public void run() {
                updateBoards();
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
    
    private void updateBoards() {
        Iterator<Map.Entry<UUID, FastBoard>> iterator = boards.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, FastBoard> entry = iterator.next();
            UUID playerUniqueId = entry.getKey();
            FastBoard board = entry.getValue();
            String teamName = gameManager.getTeamName(playerUniqueId);
            String teamDisplayName = gameManager.getTeamDisplayName(teamName);
            ChatColor teamChatColor = gameManager.getTeamChatColor(teamName);
            int score = gameManager.getPlayerScore(playerUniqueId);
            board.updateLines(
                    teamChatColor+teamDisplayName,
                    ChatColor.GOLD+"Score: "+score
            );
        }
    }
    
    private void giveBoardsToAllOnlinePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (gameManager.hasPlayer(player.getUniqueId())) {
                addBoard(player);
            }
        }
    }
    
    private void addBoard(Player player) {
        FastBoard newBoard = new FastBoard(player);
        newBoard.updateTitle(ChatColor.BOLD+""+ChatColor.DARK_RED+"MCT Alpha");
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
        if (!gameManager.hasPlayer(player.getUniqueId())) {
            return;
        }
        addBoard(player);
    }
    
}
