package org.braekpo1nt.mctmanager.listeners;

import org.braekpo1nt.mctmanager.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Scoreboard;

/**
 * All logic that should be run when a player joins the server
 */
public class PlayerJoinListener implements Listener {
    
    private final Scoreboard mctScoreboard;
    
    public PlayerJoinListener(Main plugin, Scoreboard mctScoreboard) {
        this.mctScoreboard = mctScoreboard;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.setScoreboard(mctScoreboard);
    }
    
}
