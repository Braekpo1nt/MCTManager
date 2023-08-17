package org.braekpo1nt.mctmanager.games.event;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;

public class EventManager {
    
    private final Main plugin;
    private final GameManager gameManager;
    
    public EventManager(Main plugin, GameManager gameManager) {
        
        this.plugin = plugin;
        this.gameManager = gameManager;
    }
    
}
