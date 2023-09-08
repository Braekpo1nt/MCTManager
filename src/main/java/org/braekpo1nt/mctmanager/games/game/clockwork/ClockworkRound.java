package org.braekpo1nt.mctmanager.games.game.clockwork;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.clockwork.config.ClockworkStorageUtil;
import org.bukkit.entity.Player;

import java.util.List;

public class ClockworkRound {
    
    
    private final Main plugin;
    private final GameManager gameManager;
    private final ClockworkGame clockworkGame;
    private final ClockworkStorageUtil storageUtil;
    
    public ClockworkRound(Main plugin, GameManager gameManager, ClockworkGame clockworkGame, ClockworkStorageUtil storageUtil) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.clockworkGame = clockworkGame;
        this.storageUtil = storageUtil;
    }
    
    public void start(List<Player> newParticipants) {
        
    }
}
