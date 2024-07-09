package org.braekpo1nt.mctmanager.games.game.footrace;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.interfaces.Configurable;
import org.braekpo1nt.mctmanager.games.game.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.ui.sidebar.Headerable;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FootRaceGame implements Listener, MCTGame, Configurable, Headerable {
    
    public FootRaceGame(Main plugin, GameManager gameManager) {
        
    }
    
    @Override
    public void loadConfig() throws ConfigIOException, ConfigInvalidException {
        
    }
    
    @Override
    public GameType getType() {
        return null;
    }
    
    @Override
    public void start(List<Player> newParticipants, List<Player> newAdmins) {
        
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
    
    @Override
    public void onAdminJoin(Player admin) {
        
    }
    
    @Override
    public void onAdminQuit(Player admin) {
        
    }
    
    @Override
    public @NotNull String getBaseTitle() {
        return "";
    }
    
    @Override
    public void setTitle(@NotNull String title) {
        
    }
    
    @Override
    public void updatePersonalScore(Player participant, String contents) {
        
    }
    
    @Override
    public void updateTeamScore(Player participant, String contents) {
        
    }
}
