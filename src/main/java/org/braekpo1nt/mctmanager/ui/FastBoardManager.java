package org.braekpo1nt.mctmanager.ui;

import fr.mrmicky.fastboard.FastBoard;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

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
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    private void updateBoards() {
        for (FastBoard board : boards.values()) {
            
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
        Component teamDisplayName = gameManager.getFormattedTeamDisplayName(player.getUniqueId());
        FastBoard newBoard = new FastBoard(player);
        newBoard.updateTitle("MCT #1");
        newBoard.updateLines(
                teamDisplayName.toString()
        );
    }
    
    private void removeBoard(UUID playerUniqueId) {
        
    }
    
    @EventHandler
    public void playerQuitEvent(PlayerQuitEvent event) {
        
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
