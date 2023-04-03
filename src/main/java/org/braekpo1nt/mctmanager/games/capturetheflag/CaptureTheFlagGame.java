package org.braekpo1nt.mctmanager.games.capturetheflag;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.interfaces.MCTGame;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class CaptureTheFlagGame implements MCTGame {
    
    private final Main plugin;
    private final GameManager gameManager;
    
    public CaptureTheFlagGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }
    
    @Override
    public void start(List<Player> newParticipants) {
        Bukkit.getLogger().info("Started Capture the Flag");
    }
    
    @Override
    public void stop() {
        Bukkit.getLogger().info("Stopped Capture the Flag");
    }
    
    @Override
    public void onParticipantJoin(Player participant) {
        
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        
    }
}
