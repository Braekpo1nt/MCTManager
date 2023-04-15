package org.braekpo1nt.mctmanager.games.capturetheflag2;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.event.Listener;

/**
 * A round is made up of multiple matches. It kicks off the matches it contains, and ends
 * when all the matches are over.
 */
public class CaptureTheFlagRound implements Listener {
    
    private final Main plugin;
    private final GameManager gameManager;
    
    public CaptureTheFlagRound(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.gameManager = gameManager;
    }
    
    public void start() {
        
    }
    
    public void stop() {
        
    }
    
}
