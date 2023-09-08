package org.braekpo1nt.mctmanager.games.game.clockwork;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.interfaces.Configurable;
import org.braekpo1nt.mctmanager.games.game.interfaces.MCTGame;
import org.bukkit.entity.Player;

import java.util.List;

public class ClockworkGame implements MCTGame, Configurable {
    private final Main plugin;
    private final GameManager gameManager;
    
    public ClockworkGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }
    
    @Override
    public GameType getType() {
        return null;
    }
    
    @Override
    public boolean loadConfig() throws IllegalArgumentException {
        return false;
    }
    
    @Override
    public void start(List<Player> newParticipants) {
        
    }
    
    @Override
    public void stop() {
        
    }
    
    @Override
    public void onParticipantJoin(Player participant) {
        
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        
    }
}
